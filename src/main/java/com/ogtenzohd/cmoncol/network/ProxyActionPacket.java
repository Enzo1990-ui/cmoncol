package com.ogtenzohd.cmoncol.network;

import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.blocks.custom.daycare.DaycareBlockEntity;
import com.ogtenzohd.cmoncol.blocks.custom.pasture.PastureBlockEntity;
import com.ogtenzohd.cmoncol.blocks.custom.traineracadamy.TrainerAcadamyBlockEntity;
import com.ogtenzohd.cmoncol.blocks.custom.sciencelab.ScienceLabBlockEntity; // NEW IMPORT
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import java.util.List;
import java.util.UUID;

public record ProxyActionPacket(BlockPos pos, int actionId, int targetSlot) implements CustomPacketPayload {
    public static final Type<ProxyActionPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "proxy_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProxyActionPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, ProxyActionPacket::pos, 
        ByteBufCodecs.INT, ProxyActionPacket::actionId, 
        ByteBufCodecs.INT, ProxyActionPacket::targetSlot, 
        ProxyActionPacket::new
    );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(final ProxyActionPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            
            PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(serverPlayer);
            RegistryAccess regAccess = serverPlayer.level().registryAccess();
            String pName = serverPlayer.getName().getString(); 

            // buttons for pasture! - labeling so i stop modifing the wrong button
            if (player.level().getBlockEntity(payload.pos()) instanceof PastureBlockEntity pasture) {
                // -----------------------------------
				// DEPOSIT!!!!! \/\/\/
                if (payload.actionId() == 0) {
                    Pokemon monToDeposit = party.get(payload.targetSlot());
                    if (monToDeposit != null && pasture.canAcceptMore(player.getUUID())) {
                        CompoundTag nbt = monToDeposit.saveToNBT(regAccess, new CompoundTag());
                        party.remove(monToDeposit);
                        pasture.addPokemon(player.getUUID(), pName, nbt);
                    }
                }
                // -----------------------------------
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
                // -----------------------------------
				// TOGLLE!!!!! \/\/\/
                else if (payload.actionId() == 7) {
                    pasture.toggleRecipe(payload.targetSlot());
                }
            } 
            
            // buttons for EV-Trainer!
            else if (player.level().getBlockEntity(payload.pos()) instanceof TrainerAcadamyBlockEntity gym) {
                // -----------------------------------
				// DEPOSIT!!!!! \/\/\/
                if (payload.actionId() == 4) {
                    Pokemon monToDeposit = party.get(payload.targetSlot());
                    if (monToDeposit != null && !gym.hasPokemon()) {
                        CompoundTag nbt = monToDeposit.saveToNBT(regAccess, new CompoundTag());
                        party.remove(monToDeposit);
                        gym.setProxyData(player.getUUID(), pName, nbt);
                    }
                }
                // -----------------------------------
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
                
                // -----------------------------------
				// DEPOSIT!!!!! \/\/\/
                if (payload.actionId() == 2) { 
                    Pokemon monToDeposit = party.get(payload.targetSlot());
                    if (monToDeposit != null && daycare.canAcceptMore(player.getUUID())) {
                        CompoundTag nbt = monToDeposit.saveToNBT(regAccess, new CompoundTag());
                        party.remove(monToDeposit); // <--- FIXED LINE
                        daycare.addPokemon(player.getUUID(), pName, nbt);
                    }
                }
                
                // -----------------------------------
				// WITHDRAW NUMBER 1!!!!! \/\/\/
                else if (payload.actionId() == 3) { 
                    if (daycare.getStoredPokemon().size() > 0) {
                        DaycareBlockEntity.DaycareSlot slot = daycare.getStoredPokemon().get(0);
                        if (slot.ownerUUID.equals(player.getUUID())) {
                            Pokemon mon = new Pokemon();
                            mon.loadFromNBT(regAccess, slot.pokemonNBT);
                            
                            if (party.add(mon) || Cobblemon.INSTANCE.getStorage().getPC(serverPlayer).add(mon)) {
                                daycare.removePokemon(0);
                            }
                        }
                    }
                } 
                
                // -----------------------------------
				// WITHDRAW NUMBER 2!!!!! \/\/\/
                else if (payload.actionId() == 6) { 
                    if (daycare.getStoredPokemon().size() > 1) {
                        DaycareBlockEntity.DaycareSlot slot = daycare.getStoredPokemon().get(1);
                        if (slot.ownerUUID.equals(player.getUUID())) {
                            Pokemon mon = new Pokemon();
                            mon.loadFromNBT(regAccess, slot.pokemonNBT);
                            
                            if (party.add(mon) || Cobblemon.INSTANCE.getStorage().getPC(serverPlayer).add(mon)) {
                                daycare.removePokemon(1);
                            }
                        }
                    }
                }
            } 
            // buttons for Science Lab
            else if (player.level().getBlockEntity(payload.pos) instanceof ScienceLabBlockEntity lab) {
                if (payload.actionId == 8) {
                    lab.setExpeditionActive(true);
                } else if (payload.actionId == 9) {
                    lab.setExpeditionActive(false);
                }
            } 
            
            BlockState state = player.level().getBlockState(payload.pos);
            player.level().sendBlockUpdated(payload.pos, state, state, 3);
        });
    }
}