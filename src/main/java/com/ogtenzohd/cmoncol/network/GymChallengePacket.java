package com.ogtenzohd.cmoncol.network;

import com.ogtenzohd.cmoncol.blocks.custom.gym.GymBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public record GymChallengePacket(BlockPos gymPos, String leaderName) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<GymChallengePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "gym_challenge"));

    public static final StreamCodec<FriendlyByteBuf, GymChallengePacket> STREAM_CODEC = StreamCodec.ofMember(
        GymChallengePacket::write, GymChallengePacket::new
    );

    public GymChallengePacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readUtf());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(gymPos);
        buf.writeUtf(leaderName);
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final GymChallengePacket data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.level().getBlockEntity(data.gymPos()) instanceof GymBlockEntity gym) {
                    
                    String status = gym.checkCanBattle(data.leaderName());

                    if (status.equals("yes")) { //screw true or false i want it to be yes..
                        gym.loadGymArena(player.serverLevel(), data.leaderName());
                        player.sendSystemMessage(Component.literal("§aThe Gym Arena is shifting..."));
                    } else {
                        player.sendSystemMessage(Component.literal("§c" + status));
                    }
                }
            }
        });
    }
}