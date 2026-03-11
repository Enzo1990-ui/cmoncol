package com.ogtenzohd.cmoncol.client;

import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.client.render.InvisibleEntityRenderer;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = CobblemonColonies.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CmoncolReg.GHOST_RECEPTIONIST.get(), InvisibleEntityRenderer::new);
    }
}
