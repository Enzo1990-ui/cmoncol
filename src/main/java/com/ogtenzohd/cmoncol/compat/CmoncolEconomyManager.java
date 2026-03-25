package com.ogtenzohd.cmoncol.compat;

import com.ogtenzohd.cmoncol.economy.IEconomyProvider;
import com.ogtenzohd.cmoncol.economy.providers.CobbleDollarsProvider;
import com.ogtenzohd.cmoncol.economy.providers.ItemEconomyProvider;
import net.neoforged.fml.ModList;

public class CmoncolEconomyManager {

    private static IEconomyProvider ACTIVE_PROVIDER;

    public static void initialize() {
        if (ModList.get().isLoaded("cobbledollars")) {
            ACTIVE_PROVIDER = new CobbleDollarsProvider();
            com.ogtenzohd.cmoncol.CobblemonColonies.LOGGER.info("Hooked into CobbleDollars Economy!");
            return;
        }
        ACTIVE_PROVIDER = new ItemEconomyProvider();
        com.ogtenzohd.cmoncol.CobblemonColonies.LOGGER.info("Using Item-Based Economy.");
    }

    public static IEconomyProvider get() {
        if (ACTIVE_PROVIDER == null) {
            initialize();
        }
        return ACTIVE_PROVIDER;
    }
}