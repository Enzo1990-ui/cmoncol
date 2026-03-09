package com.ogtenzohd.cmoncol.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.ogtenzohd.cmoncol.CobblemonColonies;

public record SyncBadgesPacket(
    boolean boulder, boolean cascade, boolean thunder, boolean rainbow, 
    boolean soul, boolean marsh, boolean volcano, boolean earth
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncBadgesPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "sync_badges"));

    public static final StreamCodec<FriendlyByteBuf, SyncBadgesPacket> STREAM_CODEC = StreamCodec.ofMember(
        SyncBadgesPacket::write, SyncBadgesPacket::new
    );
	
	public SyncBadgesPacket(net.minecraft.nbt.CompoundTag data) {
        this(
            data.getBoolean("has_boulder_badge"),
            data.getBoolean("has_cascade_badge"),
            data.getBoolean("has_thunder_badge"),
            data.getBoolean("has_rainbow_badge"),
            data.getBoolean("has_soul_badge"),
            data.getBoolean("has_marsh_badge"),
            data.getBoolean("has_volcano_badge"),
            data.getBoolean("has_earth_badge")
        );
    }

    public SyncBadgesPacket(FriendlyByteBuf buf) {
        this(
            buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), 
            buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean()
        );
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(boulder);
        buf.writeBoolean(cascade);
        buf.writeBoolean(thunder);
        buf.writeBoolean(rainbow);
        buf.writeBoolean(soul);
        buf.writeBoolean(marsh);
        buf.writeBoolean(volcano);
        buf.writeBoolean(earth);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final SyncBadgesPacket data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            com.ogtenzohd.cmoncol.client.ClientBadgeCache.hasBoulderBadge = data.boulder();
            com.ogtenzohd.cmoncol.client.ClientBadgeCache.hasCascadeBadge = data.cascade();
            com.ogtenzohd.cmoncol.client.ClientBadgeCache.hasThunderBadge = data.thunder();
            com.ogtenzohd.cmoncol.client.ClientBadgeCache.hasRainbowBadge = data.rainbow();
            com.ogtenzohd.cmoncol.client.ClientBadgeCache.hasSoulBadge = data.soul();
            com.ogtenzohd.cmoncol.client.ClientBadgeCache.hasMarshBadge = data.marsh();
            com.ogtenzohd.cmoncol.client.ClientBadgeCache.hasVolcanoBadge = data.volcano();
            com.ogtenzohd.cmoncol.client.ClientBadgeCache.hasEarthBadge = data.earth();
        });
    }
}