package com.ogtenzohd.cmoncol.network;

import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.blocks.custom.pokemonguard.PokemonGuardBuildingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record UpdateGuardSettingsPacket(BlockPos pos, int citizenId, String selectedPartner) implements CustomPacketPayload {

    public static final Type<UpdateGuardSettingsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "update_guard_settings"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateGuardSettingsPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateGuardSettingsPacket::pos,
            ByteBufCodecs.INT, UpdateGuardSettingsPacket::citizenId,
            ByteBufCodecs.STRING_UTF8, UpdateGuardSettingsPacket::selectedPartner,
            UpdateGuardSettingsPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(final UpdateGuardSettingsPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (!(player instanceof ServerPlayer serverPlayer)) return;

            if (serverPlayer.distanceToSqr(payload.pos().getX(), payload.pos().getY(), payload.pos().getZ()) > 4096.0) {
                return;
            }

            BlockEntity be = player.level().getBlockEntity(payload.pos());
            if (be instanceof PokemonGuardBuildingBlockEntity guardBE) {
                guardBE.setAssignedPartner(payload.citizenId(), payload.selectedPartner());
                BlockState state = player.level().getBlockState(payload.pos());
                player.level().sendBlockUpdated(payload.pos(), state, state, 3);
                if (guardBE.getBuilding() != null) {
                    for (com.minecolonies.api.colony.ICitizenData cit : guardBE.getBuilding().getAllAssignedCitizen()) {
                        if (cit.getId() == payload.citizenId() && cit.getEntity().isPresent()) {
                            LivingEntity worker = cit.getEntity().get();
                            String targetTag = "guard_partner_" + worker.getUUID();

                            java.util.List<com.cobblemon.mod.common.entity.pokemon.PokemonEntity> orphans = serverPlayer.serverLevel().getEntitiesOfClass(
                                    com.cobblemon.mod.common.entity.pokemon.PokemonEntity.class,
                                    worker.getBoundingBox().inflate(64.0D),
                                    entity -> entity.getTags().contains(targetTag)
                            );
                            for (com.cobblemon.mod.common.entity.pokemon.PokemonEntity orphan : orphans) {
                                orphan.discard();
                            }
                            break;
                        }
                    }
                }
            }
        });
    }
}