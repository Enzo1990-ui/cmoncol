package com.ogtenzohd.cmoncol.client;

import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.blocks.custom.gym.GymBlock;
import com.ogtenzohd.cmoncol.entity.GhostReceptionistEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = CobblemonColonies.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                com.ogtenzohd.cmoncol.registration.CmoncolReg.GHOST_RECEPTIONIST.get(),
                com.ogtenzohd.cmoncol.client.render.InvisibleEntityRenderer::new
        );
        event.registerEntityRenderer(
                com.ogtenzohd.cmoncol.registration.CmoncolReg.RANGER_ENTITY.get(),
                com.ogtenzohd.cmoncol.client.render.InvisibleEntityRenderer::new
        );
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        var defaultRenderer = event.getSkin(net.minecraft.client.resources.PlayerSkin.Model.WIDE);
        if (defaultRenderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer playerRenderer) {
            playerRenderer.addLayer(new com.ogtenzohd.cmoncol.client.render.VipBadgeLayer(playerRenderer));
            playerRenderer.addLayer(new com.ogtenzohd.cmoncol.client.render.VipCapeLayer(playerRenderer));
        }

        var slimRenderer = event.getSkin(net.minecraft.client.resources.PlayerSkin.Model.SLIM);
        if (slimRenderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer playerRenderer) {
            playerRenderer.addLayer(new com.ogtenzohd.cmoncol.client.render.VipBadgeLayer(playerRenderer));
            playerRenderer.addLayer(new com.ogtenzohd.cmoncol.client.render.VipCapeLayer(playerRenderer));
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide && event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND) {
            if (event.getTarget() instanceof GhostReceptionistEntity receptionist) {

                BlockPos.betweenClosedStream(
                                receptionist.blockPosition().offset(-10, -5, -10),
                                receptionist.blockPosition().offset(10, 5, 10))
                        .filter(pos -> receptionist.level().getBlockState(pos).getBlock() instanceof GymBlock)
                        .findFirst()
                        .ifPresent(ClientHelper::openGymScreen);

                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }
}