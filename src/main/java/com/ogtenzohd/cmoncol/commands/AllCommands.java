package com.ogtenzohd.cmoncol.commands;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.blocks.custom.gym.GymBlockEntity;
import com.ogtenzohd.cmoncol.blocks.custom.wondertrade.WonderTradeCentreBlockEntity;
import com.ogtenzohd.cmoncol.events.CmoncolBadgeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = CobblemonColonies.MODID)
public class AllCommands {
	
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_POKEMON = (context, builder) -> {
        java.util.List<String> allPokemon = new java.util.ArrayList<>();
        PokemonSpecies.getImplemented().forEach(species -> allPokemon.add(species.getName().toLowerCase()));
        
        return SharedSuggestionProvider.suggest(allPokemon, builder);
    };

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_TOOLS = (context, builder) -> {
        SharedSuggestionProvider.suggest(new String[]{"none"}, builder);
        return SharedSuggestionProvider.suggestResource(BuiltInRegistries.ITEM.keySet(), builder);
    };

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_DROPS = (context, builder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ITEM.keySet(), builder);
	
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        //reset command so i dont have to wait the cooldown to test!
        dispatcher.register(Commands.literal("gym")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("reset")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .executes(context -> {
                        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
                        ServerLevel level = context.getSource().getLevel();
                        BlockEntity be = level.getBlockEntity(pos);

                        if (be instanceof GymBlockEntity gym) {
                            gym.forceResetArena();
                            context.getSource().sendSuccess(() -> Component.literal("§aGym successfully reset at " + pos.toShortString() + "!"), false);
                            return 1;
                        } else {
                            context.getSource().sendFailure(Component.literal("§cNo Gym block found at those coordinates."));
                            return 0;
                        }
                    })
                )
            )
        );

        //Badge read command -- ill add items eventually!
        dispatcher.register(Commands.literal("badges")
                .executes(context -> {
                    ServerPlayer p = context.getSource().getPlayerOrException();
                    CompoundTag data = p.getPersistentData();

                    StringBuilder sb = new StringBuilder("§6§lYour Gym Badges:§r\n");
                    int count = 0;
                    for (String key : data.getAllKeys()) {
                        if (key.startsWith("has_") && key.endsWith("_badge") && data.getBoolean(key)) {
                            String rawBadge = key.substring(4, key.length() - 6);
                            String displayBadge = java.util.Arrays.stream(rawBadge.split("_"))
                                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                                    .collect(java.util.stream.Collectors.joining(" "));

                            sb.append("§e- ").append(displayBadge).append(" Badge\n");
                            count++;
                        }
                    }

                    if (count == 0) {
                        sb.append("§7You don't have any badges yet. Go challenge a Gym!");
                    }

                    p.sendSystemMessage(Component.literal(sb.toString()));
                    return 1;
                })
        );

        //Badge grant command for the Gym leaders
        dispatcher.register(Commands.literal("cmoncol_grant_badge")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("badge", StringArgumentType.word())
                                .executes(context -> {
                                    ServerPlayer p = EntityArgument.getPlayer(context, "player");
                                    String badge = StringArgumentType.getString(context, "badge");
                                    CmoncolBadgeManager.awardBadge(p, "admin", badge);

                                    context.getSource().sendSuccess(() -> Component.literal("§aGranted the " + badge + " badge to " + p.getName().getString() + "!"), true);
                                    return 1;
                                })
                        )
                )
        );
        
        //Pasture Recipe Command
        dispatcher.register(Commands.literal("cmoncol")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("addpasture")
                .then(Commands.argument("pokemon", StringArgumentType.word())
                    .suggests(SUGGEST_POKEMON)
                    .then(Commands.argument("tool", StringArgumentType.word())
                        .suggests(SUGGEST_TOOLS)
                        .then(Commands.argument("drop", ResourceLocationArgument.id())
                            .suggests(SUGGEST_DROPS)
                            .then(Commands.argument("min", IntegerArgumentType.integer(1))
                                .then(Commands.argument("max", IntegerArgumentType.integer(1))
                                    .then(Commands.argument("chance", FloatArgumentType.floatArg(0.0f, 1.0f))
                                        .executes(context -> {
                                            
                                            String pokemon = StringArgumentType.getString(context, "pokemon").toLowerCase();
                                            String toolId = StringArgumentType.getString(context, "tool");
                                            ResourceLocation dropId = ResourceLocationArgument.getId(context, "drop");
                                            int min = IntegerArgumentType.getInteger(context, "min");
                                            int max = IntegerArgumentType.getInteger(context, "max");
                                            float chance = FloatArgumentType.getFloat(context, "chance");

                                            boolean success = addRecipeToConfig(pokemon, toolId, dropId.toString(), min, max, chance);

                                            if (success) {
                                                context.getSource().sendSuccess(() -> Component.literal("Successfully added " + pokemon + " recipe to the pasture config!"), true);
                                            } else {
                                                context.getSource().sendFailure(Component.literal("Failed to add recipe. Check server logs."));
                                            }

                                            return 1;
                                        })
                                    )
                                )
                            )
                        )
                    )
                )
            )
        );
	
		dispatcher.register(Commands.literal("cmoncol")
				.requires(source -> source.hasPermission(2))
				.then(Commands.literal("boost")
					.executes(context -> {
						CommandSourceStack source = context.getSource();
						net.minecraft.server.level.ServerPlayer player = source.getPlayerOrException();

						HitResult hit = player.pick(5.0D, 0.0F, false);
                    
						if (hit.getType() == HitResult.Type.BLOCK) {
							BlockPos pos = ((BlockHitResult) hit).getBlockPos();
							BlockEntity be = player.level().getBlockEntity(pos);

							if (be instanceof WonderTradeCentreBlockEntity wonderTrade) {
                            
								wonderTrade.setBoosted(true); 
								wonderTrade.setChanged();
                            
								player.level().sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), 3);

								source.sendSuccess(() -> Component.literal("§d§l[!]§r Wonder Trade block at " + pos.toShortString() + " is now BOOSTED!"), true);
								return 1;
							}
						}

						source.sendFailure(Component.literal("§cYou must be looking at a Wonder Trade Centre to boost it!"));
						return 0;
					})
				)
			);
		}
    private static boolean addRecipeToConfig(String pokemon, String tool, String drop, int min, int max, float chance) {
        return com.ogtenzohd.cmoncol.util.RancherRecipeManager.addDynamicRecipe(pokemon, tool, drop, min, max, chance);
    }
}