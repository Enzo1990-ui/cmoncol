package com.ogtenzohd.cmoncol.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.entity.npc.NPCEntity;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.network.CmoncolPackets;
import com.ogtenzohd.cmoncol.network.SyncBadgesPacket;
import kotlin.Unit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = CobblemonColonies.MODID)
public class CmoncolBadgeManager {

    private static boolean eventsRegistered = false;

    private static final Map<String, String> LEADER_TO_BADGE = new HashMap<>();

    static {
        // Kanto
        LEADER_TO_BADGE.put("brock", "boulder"); LEADER_TO_BADGE.put("misty", "cascade");
        LEADER_TO_BADGE.put("surge", "thunder"); LEADER_TO_BADGE.put("erika", "rainbow");
        LEADER_TO_BADGE.put("koga", "soul");     LEADER_TO_BADGE.put("sabrina", "marsh");
        LEADER_TO_BADGE.put("blaine", "volcano");LEADER_TO_BADGE.put("giovanni", "earth");

        // Johto
        LEADER_TO_BADGE.put("falkner", "zephyr");LEADER_TO_BADGE.put("bugsy", "hive");
        LEADER_TO_BADGE.put("whitney", "plain"); LEADER_TO_BADGE.put("morty", "fog");
        LEADER_TO_BADGE.put("chuck", "storm");   LEADER_TO_BADGE.put("jasmine", "mineral");
        LEADER_TO_BADGE.put("pryce", "glacier"); LEADER_TO_BADGE.put("clair", "rising");

        // Hoenn
        LEADER_TO_BADGE.put("roxanne", "stone"); LEADER_TO_BADGE.put("brawly", "knuckle");
        LEADER_TO_BADGE.put("wattson", "dynamo");LEADER_TO_BADGE.put("flannery", "heat");
        LEADER_TO_BADGE.put("norman", "balance");LEADER_TO_BADGE.put("winona", "feather");
        LEADER_TO_BADGE.put("tate", "mind");     LEADER_TO_BADGE.put("liza", "mind");
        LEADER_TO_BADGE.put("juan", "rain");     LEADER_TO_BADGE.put("wallace", "rain");

        // Sinnoh
        LEADER_TO_BADGE.put("roark", "coal");    LEADER_TO_BADGE.put("gardenia", "forest");
        LEADER_TO_BADGE.put("maylene", "cobble");LEADER_TO_BADGE.put("wake", "fen");
        LEADER_TO_BADGE.put("fantina", "relic"); LEADER_TO_BADGE.put("byron", "mine");
        LEADER_TO_BADGE.put("candice", "icicle");LEADER_TO_BADGE.put("volkner", "beacon");

        // Unova
        LEADER_TO_BADGE.put("cilan", "trio");    LEADER_TO_BADGE.put("chili", "trio");
        LEADER_TO_BADGE.put("cress", "trio");    LEADER_TO_BADGE.put("lenora", "basic");
        LEADER_TO_BADGE.put("cheren", "basic");  LEADER_TO_BADGE.put("burgh", "insect");
        LEADER_TO_BADGE.put("elesa", "bolt");    LEADER_TO_BADGE.put("clay", "quake");
        LEADER_TO_BADGE.put("skyla", "jet");     LEADER_TO_BADGE.put("brycen", "freeze");
        LEADER_TO_BADGE.put("drayden", "legend");LEADER_TO_BADGE.put("iris", "legend");
        LEADER_TO_BADGE.put("roxie", "toxic");   LEADER_TO_BADGE.put("marlon", "wave");

        // Kalos
        LEADER_TO_BADGE.put("viola", "bug");     LEADER_TO_BADGE.put("grant", "cliff");
        LEADER_TO_BADGE.put("korrina", "rumble");LEADER_TO_BADGE.put("ramos", "plant");
        LEADER_TO_BADGE.put("clemont", "voltage");LEADER_TO_BADGE.put("valerie", "fairy");
        LEADER_TO_BADGE.put("olympia", "psychic");LEADER_TO_BADGE.put("wulfric", "iceberg");

        // Galar
        LEADER_TO_BADGE.put("milo", "galar_grass"); LEADER_TO_BADGE.put("nessa", "galar_water");
        LEADER_TO_BADGE.put("kabu", "galar_fire");  LEADER_TO_BADGE.put("bea", "galar_fighting");
        LEADER_TO_BADGE.put("allister", "galar_ghost"); LEADER_TO_BADGE.put("opal", "galar_fairy");
        LEADER_TO_BADGE.put("gordie", "galar_rock");LEADER_TO_BADGE.put("melony", "galar_ice");
        LEADER_TO_BADGE.put("piers", "galar_dark"); LEADER_TO_BADGE.put("raihan", "galar_dragon");

        // Paldea
        LEADER_TO_BADGE.put("katy", "paldea_bug");   LEADER_TO_BADGE.put("brassius", "paldea_grass");
        LEADER_TO_BADGE.put("iono", "paldea_electric");LEADER_TO_BADGE.put("kofu", "paldea_water");
        LEADER_TO_BADGE.put("larry", "paldea_normal"); LEADER_TO_BADGE.put("ryme", "paldea_ghost");
        LEADER_TO_BADGE.put("tulip", "paldea_psychic");LEADER_TO_BADGE.put("grusha", "paldea_ice");
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        if (eventsRegistered) return;
        eventsRegistered = true;

        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, (victoryEvent) -> {

            if (!victoryEvent.getWinners().isEmpty()) {
                Object winnerActor = victoryEvent.getWinners().getFirst();
                Entity winningEntity = getEntityFromActor(winnerActor);

                if (winningEntity instanceof ServerPlayer player) {
                    victoryEvent.getLosers().forEach(loserActor -> {
                        Entity losingEntity = getEntityFromActor(loserActor);

                        if (losingEntity instanceof NPCEntity npc) {
                            String npcName = npc.getName().getString().toLowerCase();

                            for (Map.Entry<String, String> entry : LEADER_TO_BADGE.entrySet()) {
                                if (npcName.contains(entry.getKey())) {
                                    awardBadge(player, entry.getKey(), entry.getValue());
                                    break;
                                }
                            }
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

    public static void awardBadge(ServerPlayer player, String leaderName, String badgeName) {
        CompoundTag data = player.getPersistentData();
        String tag = "has_" + badgeName.toLowerCase() + "_badge";

        String displayBadge = java.util.Arrays.stream(badgeName.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(java.util.stream.Collectors.joining(" "));

        if (net.neoforged.fml.ModList.get().isLoaded("rctmod")) {
            com.ogtenzohd.cmoncol.compat.RCTCompat.advancePlayerProgress(player, leaderName);
        }

        if (!data.getBoolean(tag)) {
            data.putBoolean(tag, true);
            player.sendSystemMessage(Component.literal("§6§lCongratulations! You received the " + displayBadge + " Badge!"));
            CmoncolPackets.sendToPlayer(new SyncBadgesPacket(data), player);
            if (leaderName.equals("admin")) return;

            String capitalizedLeader = leaderName.substring(0, 1).toUpperCase() + leaderName.substring(1);

            for (String cmd : com.ogtenzohd.cmoncol.config.CCConfig.INSTANCE.gymVictoryCommands.get()) {
                String formattedCmd = cmd
                        .replace("%player%", player.getScoreboardName())
                        .replace("%leader%", capitalizedLeader)
                        .replace("%badge%", displayBadge);
                player.server.getCommands().performPrefixedCommand(
                        player.server.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                        formattedCmd
                );
            }

        } else {
            player.sendSystemMessage(Component.literal("§cYou already have the " + displayBadge + " badge!"));
        }
    }
}