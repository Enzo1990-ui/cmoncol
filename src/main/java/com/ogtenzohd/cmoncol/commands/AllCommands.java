package com.ogtenzohd.cmoncol.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.blocks.custom.gym.GymBlockEntity;
import com.ogtenzohd.cmoncol.events.CmoncolBadgeManager;
import com.ogtenzohd.cmoncol.network.CmoncolPackets;
import com.ogtenzohd.cmoncol.network.SyncBadgesPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = CobblemonColonies.MODID, bus = EventBusSubscriber.Bus.GAME)
public class AllCommands {

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
                String[] badges = {"boulder", "cascade", "thunder", "rainbow", "soul", "marsh", "volcano", "earth"};
                int count = 0;
                
                for (String b : badges) {
                    if (data.getBoolean("has_" + b + "_badge")) {
                        sb.append("§e- ").append(b.substring(0, 1).toUpperCase()).append(b.substring(1)).append(" Badge\n");
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
                        
                        CmoncolBadgeManager.awardBadge(p, badge);
                        
                        context.getSource().sendSuccess(() -> Component.literal("§aGranted the " + badge + " badge to " + p.getName().getString() + "!"), true);
                        return 1;
                    })
                )
            )
        );
    }
}