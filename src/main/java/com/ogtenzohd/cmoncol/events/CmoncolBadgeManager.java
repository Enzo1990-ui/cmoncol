package com.ogtenzohd.cmoncol.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.cobblemon.mod.common.entity.npc.NPCEntity;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.network.CmoncolPackets;
import com.ogtenzohd.cmoncol.network.SyncBadgesPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import kotlin.Unit;

@EventBusSubscriber(modid = CobblemonColonies.MODID)
public class CmoncolBadgeManager {

    private static boolean eventsRegistered = false;

    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        if (eventsRegistered) return;
        eventsRegistered = true;

        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, (battleEvent) -> {
            BattleVictoryEvent victoryEvent = (BattleVictoryEvent) battleEvent;
            
            if (!victoryEvent.getWinners().isEmpty()) {
                Object winnerActor = victoryEvent.getWinners().get(0);
                Entity winningEntity = getEntityFromActor(winnerActor);
                
                if (winningEntity instanceof ServerPlayer player) {
                    
                    victoryEvent.getLosers().forEach(loserActor -> {
                        Entity losingEntity = getEntityFromActor(loserActor);
                        
                        if (losingEntity instanceof NPCEntity npc) {
                            
                            String npcName = npc.getName().getString().toLowerCase();
                            
                            if (npcName.contains("brock")) awardBadge(player, "boulder");
                            else if (npcName.contains("misty")) awardBadge(player, "cascade");
                            else if (npcName.contains("surge")) awardBadge(player, "thunder");
                            else if (npcName.contains("erika")) awardBadge(player, "rainbow");
                            else if (npcName.contains("koga")) awardBadge(player, "soul");
                            else if (npcName.contains("sabrina")) awardBadge(player, "marsh");
                            else if (npcName.contains("blaine")) awardBadge(player, "volcano");
                            else if (npcName.contains("giovanni")) awardBadge(player, "earth");
                        }
                    });
                }
            }
            return Unit.INSTANCE;
        });
    }

    private static Entity getEntityFromActor(Object actor) {
        try {
            return (Entity) actor.getClass().getMethod("getEntity").invoke(actor);
        } catch (Exception e) {
            return null;
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncPlayerBadges(player);
        }
    }

    public static void syncPlayerBadges(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        CmoncolPackets.sendToPlayer(new SyncBadgesPacket(data), player);
    }

    public static void awardBadge(ServerPlayer player, String badgeName) {
        CompoundTag data = player.getPersistentData();
        String tag = "has_" + badgeName.toLowerCase() + "_badge";

        if (!data.getBoolean(tag)) {
            data.putBoolean(tag, true);
            player.sendSystemMessage(Component.literal("§6§lCongratulations! You received the " + badgeName.toUpperCase() + " Badge!"));
            
            CmoncolPackets.sendToPlayer(new SyncBadgesPacket(data), player);
        } else {
            player.sendSystemMessage(Component.literal("§cYou already have the " + badgeName + " badge!"));
        }
    }
}