package com.ogtenzohd.cmoncol.util;

import net.minecraft.world.entity.player.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static java.net.URI.create;

public class CmoncolPerks {

    private static final Set<String> OWNER = new HashSet<>();
    private static final Set<String> PATRONS = new HashSet<>();
    private static final Set<String> HELPERS = new HashSet<>();
    private static final Random RANDOM = new Random();

    private static final String OWNER_URL = "https://gist.github.com/Enzo1990-ui/37dda306e16f65da407b9d5e8fe6c516/raw/";
    private static final String PATRON_URL = "https://gist.github.com/Enzo1990-ui/06ad38476b7f831dea6b692520d548c1/raw/";
    private static final String HELPER_URL = "https://gist.github.com/Enzo1990-ui/763b350350f2b5231d7edf654bfcb054/raw/";

    public static void fetchVIPLists() {
        new Thread(() -> {
            fetchList(OWNER_URL, OWNER);
            fetchList(PATRON_URL, PATRONS);
            fetchList(HELPER_URL, HELPERS);
            com.ogtenzohd.cmoncol.CobblemonColonies.LOGGER.info("Successfully fetched Cmoncol VIP lists from the web!");
        }).start();
    }

    private static void fetchList(String urlString, Set<String> targetSet) {
        try {
            URL url = create(urlString).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {

                    targetSet.add(line.trim());
                }
                reader.close();
            }
        } catch (Exception e) {
            com.ogtenzohd.cmoncol.CobblemonColonies.LOGGER.error("Failed to fetch VIP list from web!", e);
        }
    }

    public static boolean isOwner(UUID uuid) {
        if (uuid == null) return false;
        return OWNER.contains(uuid.toString());
    }

    public static boolean isPatron(UUID uuid) {
        if (uuid == null) return false;
        return PATRONS.contains(uuid.toString());
    }

    public static boolean isHelper(UUID uuid) {
        if (uuid == null) return false;
        return HELPERS.contains(uuid.toString());
    }

    public static boolean hasVIPPerks(UUID uuid) {
        return isOwner(uuid) || isPatron(uuid) || isHelper(uuid);
    }

    public static boolean hasVIPPerks(Player player) {
        if (player == null) return false;
        return hasVIPPerks(player.getUUID());
    }

    public static String getRandomSupporterUUID() {
        List<String> combined = new ArrayList<>();
        combined.addAll(PATRONS);
        combined.addAll(HELPERS);

        if (combined.isEmpty()) {
            return "Notch";
        }
        return combined.get(RANDOM.nextInt(combined.size()));
    }
}