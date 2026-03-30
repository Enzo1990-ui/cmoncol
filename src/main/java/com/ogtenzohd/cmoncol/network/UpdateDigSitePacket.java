package com.ogtenzohd.cmoncol.network;

import com.ogtenzohd.cmoncol.blocks.custom.sciencelab.ScienceLabBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import org.jetbrains.annotations.NotNull;

public class UpdateDigSitePacket implements CustomPacketPayload {
    public static final Type<UpdateDigSitePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "update_dig_site"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateDigSitePacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, UpdateDigSitePacket::pos,
        UpdateDigSitePacket::new
    );

    private final BlockPos pos;

    public UpdateDigSitePacket(BlockPos pos) { this.pos = pos; }
    public BlockPos pos() { return pos; }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(final UpdateDigSitePacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BlockEntity be = player.level().getBlockEntity(payload.pos);
                if (be instanceof ScienceLabBlockEntity lab) {
                    lab.cycleDigSite();
                    lab.setChanged();
                    player.level().sendBlockUpdated(payload.pos, lab.getBlockState(), lab.getBlockState(), 3);
                }
            }
        });
    }
}