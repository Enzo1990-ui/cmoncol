package com.ogtenzohd.cmoncol.network;

import com.ogtenzohd.cmoncol.CobblemonColonies;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import com.ogtenzohd.cmoncol.network.SyncBadgesPacket;

public class CmoncolPackets {
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CobblemonColonies.MODID).versioned("1.0.0");

        registrar.playBidirectional(
            ProxyActionPacket.TYPE,
            ProxyActionPacket.STREAM_CODEC,
            ProxyActionPacket::handle
        );
		
		registrar.playBidirectional(
			UpdateDigSitePacket.TYPE, 
			UpdateDigSitePacket.STREAM_CODEC, 
			UpdateDigSitePacket::handle
		);
		
		registrar.playToServer(
            GymChallengePacket.TYPE,
            GymChallengePacket.STREAM_CODEC,
            GymChallengePacket::handle
        );
		
		registrar.playToClient(
			SyncBadgesPacket.TYPE,
			SyncBadgesPacket.STREAM_CODEC,
			SyncBadgesPacket::handle
		);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    public static void sendToPlayer(CustomPacketPayload payload, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}