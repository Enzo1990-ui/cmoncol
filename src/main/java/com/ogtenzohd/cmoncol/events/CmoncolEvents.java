package com.ogtenzohd.cmoncol.events;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.colony.buildings.PokemartTradingUtils;
import com.ogtenzohd.cmoncol.colony.job.NurseJob;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = CobblemonColonies.MODID)
public class CmoncolEvents {
	
	private static final java.util.Map<java.util.UUID, net.minecraft.world.item.trading.MerchantOffers> POKEMART_TRADES_CACHE = new java.util.HashMap<>();
	private static final java.util.Map<java.util.UUID, Integer> POKEMART_LEVEL_CACHE = new java.util.HashMap<>();

    @SubscribeEvent
    public static void onNurseInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof AbstractEntityCitizen citizenEntity) {
            ICitizenData citizen = citizenEntity.getCitizenData();
            
            if (citizen == null) return;

            IJob<?> job = citizen.getJob();
            
            if (job instanceof NurseJob nurseJob) {
                
                if (!event.getLevel().isClientSide) {
                    Player player = event.getEntity();
                    
                    if (player instanceof ServerPlayer serverPlayer) {
                        PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(serverPlayer);
                        
                        // Heal the player - maybe i should add a ghost over nurse joy just like the assistant?
                        party.heal(); 
                        player.sendSystemMessage(Component.literal("§aYour party has been healed by the Nurse!"));
                        citizenEntity.swing(InteractionHand.MAIN_HAND);

                        if (nurseJob.getWorkBuilding() != null) {
                            Tuple<BlockPos, BlockPos> corners = nurseJob.getWorkBuilding().getCorners();
                            if (corners != null) {
                                BlockPos min = new BlockPos(
                                    Math.min(corners.getA().getX(), corners.getB().getX()),
                                    Math.min(corners.getA().getY(), corners.getB().getY()),
                                    Math.min(corners.getA().getZ(), corners.getB().getZ())
                                );
                                BlockPos max = new BlockPos(
                                    Math.max(corners.getA().getX(), corners.getB().getX()),
                                    Math.max(corners.getA().getY(), corners.getB().getY()),
                                    Math.max(corners.getA().getZ(), corners.getB().getZ())
                                );

                                for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
                                    BlockState state = event.getLevel().getBlockState(pos);
                                    ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                                    
                                    if (id.getNamespace().equals("cobblemon") && id.getPath().contains("healing_machine")) {
                                        BlockEntity be = event.getLevel().getBlockEntity(pos);
                                        if (be instanceof HealingMachineBlockEntity healingMachine) {
                                            healingMachine.activate(player.getUUID(), party);
                                            break; 
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

	//ill just block everything, as i can catch, battle and do all sorts with the ghost pokemon!
    @SubscribeEvent
    public static void onEntityInteractDummy(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget().getTags().contains("cmoncol_dummy") || 
            event.getTarget().getTags().stream().anyMatch(tag -> tag.startsWith("guard_partner_"))) {
            
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getTarget().getTags().contains("cmoncol_dummy") || 
            event.getTarget().getTags().stream().anyMatch(tag -> tag.startsWith("guard_partner_"))) {
            
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onEntityAttack(AttackEntityEvent event) {
        if (event.getTarget().getTags().contains("cmoncol_dummy") || 
            event.getTarget().getTags().stream().anyMatch(tag -> tag.startsWith("guard_partner_"))) {
            event.setCanceled(true);
        }
    }
	
	//didnt work for battling!

	// Stop catching interactions for dummy pokemon hopefully this stops me from being able to catch them!!
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof EntityHitResult entityHit) {
            if (entityHit.getEntity().getTags().contains("cmoncol_dummy")) {
                event.setCanceled(true);
                if (event.getProjectile().getOwner() instanceof Player player) {
                    if (!player.level().isClientSide) {
                        player.hurt(player.damageSources().magic(), 4.0F); 
                        player.sendSystemMessage(Component.literal("§cStop that Thief!"));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCitizenInteract(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide) return;

        if (event.getTarget() instanceof AbstractEntityCitizen citizen) {

            if (citizen.getCitizenData() != null && citizen.getCitizenData().getJob() instanceof com.ogtenzohd.cmoncol.colony.job.PokeMerchantJob merchantJob) {

                if (net.neoforged.fml.ModList.get().isLoaded("cobbledollars")) {
                    if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel && merchantJob.getWorkBuilding() != null) {
                        com.ogtenzohd.cmoncol.economy.providers.CobbleMerchantHelper.forwardInteraction(
                                event.getEntity(),
                                merchantJob.getWorkBuilding().getPosition(),
                                serverLevel
                        );
                    }
                    event.setCanceled(true);
                    event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
                    return;
                }

                int bldLevel = merchantJob.getWorkBuilding() != null ? merchantJob.getWorkBuilding().getBuildingLevel() : 1;
                java.util.UUID citizenId = citizen.getUUID();

                if (!POKEMART_TRADES_CACHE.containsKey(citizenId) || POKEMART_LEVEL_CACHE.getOrDefault(citizenId, -1) != bldLevel) {
                    POKEMART_TRADES_CACHE.put(citizenId, PokemartTradingUtils.getTradesForLevel(bldLevel));
                    POKEMART_LEVEL_CACHE.put(citizenId, bldLevel);
                }

                net.minecraft.world.item.trading.MerchantOffers lockedOffers = POKEMART_TRADES_CACHE.get(citizenId);

                net.minecraft.world.entity.npc.WanderingTrader dummyMerchant = new net.minecraft.world.entity.npc.WanderingTrader(net.minecraft.world.entity.EntityType.WANDERING_TRADER, event.getLevel()) {
                    @Override
                    public net.minecraft.world.item.trading.@NotNull MerchantOffers getOffers() {
                        return lockedOffers;
                    }

                    @Override
                    public void overrideOffers(net.minecraft.world.item.trading.MerchantOffers offers) {
                    }

                    @Override
                    public net.minecraft.sounds.SoundEvent getNotifyTradeSound() {
                        return null;
                    }
                };
                dummyMerchant.setSilent(true);
                dummyMerchant.setPos(citizen.getX(), citizen.getY(), citizen.getZ());
                dummyMerchant.setTradingPlayer(event.getEntity());

                dummyMerchant.openTradingScreen(event.getEntity(), Component.literal("PokeMart"), bldLevel);

                event.setCanceled(true);
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
            }
        }
    }
}