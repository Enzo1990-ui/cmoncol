package com.ogtenzohd.cmoncol.events;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.Priority;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import kotlin.Unit;

@EventBusSubscriber(modid = CobblemonColonies.MODID)
public class CmoncolCobblemonEvents {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {

            CobblemonEvents.BATTLE_STARTED_PRE.subscribe(Priority.HIGHEST, evt -> {
                try {
                    evt.getBattle().getActors().forEach(actor -> actor.getPokemonList().forEach(pokemon -> {
                        if (pokemon.getEntity() != null) {
                            if (pokemon.getEntity().getTags().contains("cmoncol_dummy")) {
                                evt.cancel();
                            }
                            if (pokemon.getEntity().getTags().stream().anyMatch(tag -> tag.startsWith("guard_partner_"))) {
                                evt.cancel();
                            }
                        }
                    }));
                } catch (Exception e) {
                    CobblemonColonies.LOGGER.error("Failed to cancel invalid battle interaction", e);
                }

                return Unit.INSTANCE;
            });
        });
    }
}