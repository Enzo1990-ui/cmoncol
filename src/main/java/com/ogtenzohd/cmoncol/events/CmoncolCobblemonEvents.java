package com.ogtenzohd.cmoncol.events;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.Priority;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import kotlin.Unit;

import java.util.UUID;

@EventBusSubscriber(modid = CobblemonColonies.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CmoncolCobblemonEvents {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            
            // Stop me from battling my own daycare and pasture pokemon, its a bit weird..
            CobblemonEvents.BATTLE_STARTED_PRE.subscribe(Priority.HIGHEST, evt -> {
                try {
                    evt.getBattle().getActors().forEach(actor -> {
                        actor.getPokemonList().forEach(pokemon -> {
                            if (pokemon.getEntity() != null) {
                                if (pokemon.getEntity().getTags().contains("cmoncol_dummy")) {
                                    evt.cancel();
                                }
                                if (pokemon.getEntity().getTags().stream().anyMatch(tag -> tag.startsWith("guard_partner_"))) {
                                    evt.cancel();
                                }
                            }
                        });
                    });
                } catch (Exception e) {
                }
                return Unit.INSTANCE;
            });
            
            // gym was defeated 
            CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, battleEvent -> {
                
                String badgeToAward = null;

                for (var loser : battleEvent.getLosers()) {
                    String loserName = loser.getName().getString();
                    
                    if (loserName.contains("Brock")) {
                        badgeToAward = "boulder";
                        break;
                    } else if (loserName.contains("Misty")) {
                        badgeToAward = "cascade";
                        break;
                    } else if (loserName.contains("Surge")) {
                        badgeToAward = "thunder";
                        break;
                    } else if (loserName.contains("Erika")) {
                        badgeToAward = "rainbow";
                        break;
                    } else if (loserName.contains("Koga")) {
                        badgeToAward = "soul";
                        break;
                    } else if (loserName.contains("Sabrina")) {
                        badgeToAward = "marsh";
                        break;
                    } else if (loserName.contains("Blaine")) {
                        badgeToAward = "volcano";
                        break;
                    } else if (loserName.contains("Giovanni")) { // i need to add Giovann's arena!!
                        badgeToAward = "earth";
                        break;
                    }
                }

                // give the badge
                if (badgeToAward != null) {
                    for (var winner : battleEvent.getWinners()) {
                        for (UUID uuid : winner.getPlayerUUIDs()) {
                            for (ServerPlayer player : battleEvent.getBattle().getPlayers()) {
                                if (player.getUUID().equals(uuid)) {
                                    CmoncolBadgeManager.awardBadge(player, badgeToAward);
                                }
                            }
                        }
                    }
                }
                
                return Unit.INSTANCE; // acording to forums i need to add this becasue of Kotlin... 
            });
        });
    }
}