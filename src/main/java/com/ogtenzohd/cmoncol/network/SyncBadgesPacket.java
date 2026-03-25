package com.ogtenzohd.cmoncol.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.ogtenzohd.cmoncol.CobblemonColonies;

public record SyncBadgesPacket(CompoundTag badgeData) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncBadgesPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "sync_badges"));

    public static final StreamCodec<FriendlyByteBuf, SyncBadgesPacket> STREAM_CODEC = StreamCodec.ofMember(
            SyncBadgesPacket::write, SyncBadgesPacket::new
    );

    public SyncBadgesPacket(FriendlyByteBuf buf) {
        this(buf.readNbt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(badgeData);
    }

    @Override
    @org.jetbrains.annotations.NotNull
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final SyncBadgesPacket data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            com.ogtenzohd.cmoncol.client.ClientBadgeCache.updateFromTag(data.badgeData());
        });
    }
}