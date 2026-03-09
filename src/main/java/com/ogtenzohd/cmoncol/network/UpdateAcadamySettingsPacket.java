package com.ogtenzohd.cmoncol.network;

import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.blocks.custom.traineracadamy.TrainerAcadamyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateAcadamySettingsPacket(BlockPos pos, String targetStat, boolean hyperTrain) implements CustomPacketPayload {

    public static final Type<UpdateAcadamySettingsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "update_gym_settings"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateAcadamySettingsPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, UpdateAcadamySettingsPacket::pos,
        ByteBufCodecs.STRING_UTF8, UpdateAcadamySettingsPacket::targetStat,
        ByteBufCodecs.BOOL, UpdateAcadamySettingsPacket::hyperTrain,
        UpdateAcadamySettingsPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(final UpdateAcadamySettingsPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player.level().getBlockEntity(payload.pos) instanceof TrainerAcadamyBlockEntity gym) {
                if (!gym.hasPokemon() || player.getUUID().equals(gym.getOwnerUUID())) {
                    gym.setTargetStat(payload.targetStat);
                    gym.setHyperTrain(payload.hyperTrain);
                }
            }
        });
    }
}