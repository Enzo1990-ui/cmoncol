package com.ogtenzohd.cmoncol.network;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.blocks.custom.daycare.DaycareBlockEntity;
import com.ogtenzohd.cmoncol.blocks.custom.pasture.PastureBlockEntity;
import com.ogtenzohd.cmoncol.blocks.custom.sciencelab.ScienceLabBlockEntity;
import com.ogtenzohd.cmoncol.blocks.custom.traineracadamy.TrainerAcadamyBlockEntity;
import com.ogtenzohd.cmoncol.blocks.custom.wondertrade.WonderTradeCentreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ProxyActionPacket(BlockPos pos, int actionId, int targetSlot) implements CustomPacketPayload {
    public static final Type<ProxyActionPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "proxy_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProxyActionPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, ProxyActionPacket::pos, 
        ByteBufCodecs.INT, ProxyActionPacket::actionId, 
        ByteBufCodecs.INT, ProxyActionPacket::targetSlot, 
        ProxyActionPacket::new
    );

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(final ProxyActionPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            
            PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(serverPlayer);
            RegistryAccess regAccess = serverPlayer.level().registryAccess();
            String pName = serverPlayer.getName().getString(); 

            // buttons for pasture! - labeling so i stop modifing the wrong button
            if (player.level().getBlockEntity(payload.pos()) instanceof PastureBlockEntity pasture) {
				// DEPOSIT!!!!! \/\/\/
                if (payload.actionId() == 0) {
                    Pokemon monToDeposit = party.get(payload.targetSlot());
                    if (monToDeposit != null && pasture.canAcceptMore(player.getUUID())) {
                        CompoundTag nbt = monToDeposit.saveToNBT(regAccess, new CompoundTag());
                        party.remove(monToDeposit);
                        pasture.addPokemon(player.getUUID(), pName, nbt);
                    }
                }
				// WITHDRAW!!!!! \/\/\/
                else if (payload.actionId() == 1) {
                    if (pasture.getStoredPokemon().size() > payload.targetSlot()) {
                        PastureBlockEntity.PastureSlot slot = pasture.getStoredPokemon().get(payload.targetSlot());
                        if (slot.ownerUUID.equals(player.getUUID())) {
                            Pokemon mon = new Pokemon();
                            mon.loadFromNBT(regAccess, slot.pokemonNBT);
                            
                            if (party.add(mon) || Cobblemon.INSTANCE.getStorage().getPC(serverPlayer).add(mon)) {
                                pasture.removePokemon(payload.targetSlot());
                            }
                        }
                    }
                }
				// TOGLLE!!!!! \/\/\/
                else if (payload.actionId() == 7) {
                    pasture.toggleRecipe(payload.targetSlot());
                }
            } 
            
            // buttons for EV-Trainer!
            else if (player.level().getBlockEntity(payload.pos()) instanceof TrainerAcadamyBlockEntity gym) {
				// DEPOSIT!!!!! \/\/\/
                if (payload.actionId() == 4) {
                    Pokemon monToDeposit = party.get(payload.targetSlot());
                    if (monToDeposit != null && !gym.hasPokemon()) {
                        CompoundTag nbt = monToDeposit.saveToNBT(regAccess, new CompoundTag());
                        party.remove(monToDeposit);
                        gym.setProxyData(player.getUUID(), pName, nbt);
                    }
                }
				// WITHDRAW!!!!! \/\/\/
                else if (payload.actionId() == 5) {
                    if (gym.hasPokemon() && player.getUUID().equals(gym.getOwnerUUID())) {
                        Pokemon mon = gym.extractLivePokemon((net.minecraft.server.level.ServerLevel) player.level());
                        if (mon != null) {
                            if (party.add(mon) || Cobblemon.INSTANCE.getStorage().getPC(serverPlayer).add(mon)) {
                                gym.clearProxyData();
                            }
                        }
                    }
                }
            }
            // buttons for daycare
            else if (player.level().getBlockEntity(payload.pos()) instanceof DaycareBlockEntity daycare) {

				// DEPOSIT!!!!! \/\/\/
                if (payload.actionId() == 2) {
                    Pokemon monToDeposit = party.get(payload.targetSlot());
                    if (monToDeposit != null && daycare.canAcceptMore(player.getUUID())) {
                        CompoundTag nbt = monToDeposit.saveToNBT(regAccess, new CompoundTag());
                        party.remove(monToDeposit);
                        daycare.addPokemon(player.getUUID(), pName, nbt);
                    }
                }

                else if (payload.actionId() == 3 || payload.actionId() == 6) {
                    int targetIndex = (payload.actionId() == 3) ? 0 : 1;

                    if (daycare.getStoredPokemon().size() > targetIndex) {
                        DaycareBlockEntity.DaycareSlot slot = daycare.getStoredPokemon().get(targetIndex);

                        if (slot.ownerUUID.equals(player.getUUID())) {
                            Pokemon currentMon = new Pokemon();
                            currentMon.loadFromNBT(regAccess, slot.pokemonNBT);

                            Pokemon originalMon = new Pokemon();
                            originalMon.loadFromNBT(regAccess, slot.snapshotNBT);

                            double cost = 0;
                            if (com.ogtenzohd.cmoncol.config.CCConfig.INSTANCE.enableDaycareCost.get()) {
                                int levelsGained = Math.max(0, currentMon.getLevel() - originalMon.getLevel());
                                cost = com.ogtenzohd.cmoncol.compat.CmoncolEconomyManager.get() instanceof com.ogtenzohd.cmoncol.economy.providers.CobbleDollarsProvider ?
                                        100 + (levelsGained * 100) : levelsGained;
                            }
                            boolean canAfford = true;
                            if (cost > 0) {
                                canAfford = com.ogtenzohd.cmoncol.compat.CmoncolEconomyManager.get().withdraw(serverPlayer, cost);
                            }
                            if (canAfford) {
                                if (party.add(currentMon) || Cobblemon.INSTANCE.getStorage().getPC(serverPlayer).add(currentMon)) {
                                    daycare.removePokemon(targetIndex);
                                    if (cost > 0) {
                                        player.displayClientMessage(Component.literal("§aYou paid " + com.ogtenzohd.cmoncol.compat.CmoncolEconomyManager.get().formatCurrency(cost).getString() + " to withdraw " + currentMon.getDisplayName(true).getString() + "."), false);
                                    } else {
                                        player.displayClientMessage(Component.literal("§aYou withdrew " + currentMon.getDisplayName(true).getString() + "."), false);
                                    }
                                }
                            } else {
                                player.closeContainer();
                                player.displayClientMessage(Component.literal("§cYou cannot afford to withdraw this Pokemon! You need " + com.ogtenzohd.cmoncol.compat.CmoncolEconomyManager.get().formatCurrency(cost).getString() + "."), true);
                            }
                        }
                    }
                }
            }
            // buttons for Science Lab
            else if (player.level().getBlockEntity(payload.pos()) instanceof ScienceLabBlockEntity lab) {
                if (payload.actionId() == 8) {
                    lab.setExpeditionActive(true);
                } else if (payload.actionId() == 9) {
                    lab.setExpeditionActive(false);
                }
            }
			
            else if (player.level().getBlockEntity(payload.pos()) instanceof WonderTradeCentreBlockEntity wonderTrade) {
                
                // -----------------------------------
                // DEPOSIT!!!!! \/\/\/
                if (payload.actionId() == 10) { 
                    Pokemon monToDeposit = party.get(payload.targetSlot());
                    if (monToDeposit != null && wonderTrade.getReadyPokemon() == null && wonderTrade.getTradeTimer() <= 0) {
                        CompoundTag nbt = monToDeposit.saveToNBT(regAccess, new CompoundTag());
                        party.remove(monToDeposit);
                        
                        ItemStack booster = player.getInventory().items.stream() 
                            .filter(stack -> stack.is(com.ogtenzohd.cmoncol.registration.CmoncolReg.WONDER_BOOSTER.get()))
                            .findFirst().orElse(ItemStack.EMPTY);

                        boolean useBoost = !booster.isEmpty();
                        if (useBoost) {
                            booster.shrink(1);
                        }

                        wonderTrade.startTrade(nbt, player.getUUID(), useBoost);
                    }
                }
                
                // -----------------------------------
                // -----------------------------------
                // CLAIM!!!!! \/\/\/
                else if (payload.actionId() == 11) {
                    CompoundTag readyMonNBT = wonderTrade.getReadyPokemon();

                    if (readyMonNBT != null && player.getUUID().equals(wonderTrade.getDepositorUUID())) {
                        Pokemon mon = new Pokemon();
                        mon.loadFromNBT(regAccess, readyMonNBT);

                        if (party.add(mon) || Cobblemon.INSTANCE.getStorage().getPC(serverPlayer).add(mon)) {
                            String colorCode = "§f";
                            java.util.Set<String> labels = mon.getSpecies().getLabels();

                            if (labels.contains("legendary") || labels.contains("mythical")) {
                                colorCode = "§6";
                            } else if (labels.contains("ultra_beast")) {
                                colorCode = "§b";
                            } else if (mon.getShiny()) {
                                colorCode = "§d";
                            }

                            player.displayClientMessage(Component.literal("§aClaimed: " + colorCode + mon.getDisplayName(true).getString() + " §a(Lvl " + mon.getLevel() + ")"), false);

                            wonderTrade.claimPokemon();
                        }
                    }
                    else if (readyMonNBT != null) {
                        player.displayClientMessage(Component.literal("§cThis isn't your trade!"), true);
                    }
                }
            }
            BlockState state = player.level().getBlockState(payload.pos());
            player.level().sendBlockUpdated(payload.pos(), state, state, 3);
        });
    }
}