package com.ogtenzohd.cmoncol.client;

import net.minecraft.nbt.CompoundTag;

public class ClientBadgeCache {

    // ================== KANTO (Gen 1) ==================
    public static boolean hasBoulderBadge, hasCascadeBadge, hasThunderBadge, hasRainbowBadge;
    public static boolean hasSoulBadge, hasMarshBadge, hasVolcanoBadge, hasEarthBadge;

    // ================== JOHTO (Gen 2) ==================
    public static boolean hasZephyrBadge, hasHiveBadge, hasPlainBadge, hasFogBadge;
    public static boolean hasStormBadge, hasMineralBadge, hasGlacierBadge, hasRisingBadge;

    // ================== HOENN (Gen 3) ==================
    public static boolean hasStoneBadge, hasKnuckleBadge, hasDynamoBadge, hasHeatBadge;
    public static boolean hasBalanceBadge, hasFeatherBadge, hasMindBadge, hasRainBadge;

    // ================== SINNOH (Gen 4) ==================
    public static boolean hasCoalBadge, hasForestBadge, hasCobbleBadge, hasFenBadge;
    public static boolean hasRelicBadge, hasMineBadge, hasIcicleBadge, hasBeaconBadge;

    // ================== UNOVA (Gen 5 - includes B2W2) ==================
    public static boolean hasTrioBadge, hasBasicBadge, hasInsectBadge, hasBoltBadge, hasQuakeBadge;
    public static boolean hasJetBadge, hasFreezeBadge, hasLegendBadge, hasToxicBadge, hasWaveBadge;

    // ================== KALOS (Gen 6) ==================
    public static boolean hasBugBadge, hasCliffBadge, hasRumbleBadge, hasPlantBadge;
    public static boolean hasVoltageBadge, hasFairyBadge, hasPsychicBadge, hasIcebergBadge;

    // ================== GALAR (Gen 8) ==================
    public static boolean hasGalarGrassBadge, hasGalarWaterBadge, hasGalarFireBadge, hasGalarFightingBadge;
    public static boolean hasGalarGhostBadge, hasGalarFairyBadge, hasGalarRockBadge, hasGalarIceBadge;
    public static boolean hasGalarDarkBadge, hasGalarDragonBadge;

    // ================== PALDEA (Gen 9) ==================
    public static boolean hasPaldeaBugBadge, hasPaldeaGrassBadge, hasPaldeaElectricBadge, hasPaldeaWaterBadge;
    public static boolean hasPaldeaNormalBadge, hasPaldeaGhostBadge, hasPaldeaPsychicBadge, hasPaldeaIceBadge;


    public static void updateFromTag(CompoundTag tag) {
        if (tag == null) return;

        // Kanto
        hasBoulderBadge = tag.getBoolean("has_boulder_badge");
        hasCascadeBadge = tag.getBoolean("has_cascade_badge");
        hasThunderBadge = tag.getBoolean("has_thunder_badge");
        hasRainbowBadge = tag.getBoolean("has_rainbow_badge");
        hasSoulBadge    = tag.getBoolean("has_soul_badge");
        hasMarshBadge   = tag.getBoolean("has_marsh_badge");
        hasVolcanoBadge = tag.getBoolean("has_volcano_badge");
        hasEarthBadge   = tag.getBoolean("has_earth_badge");

        // Johto
        hasZephyrBadge  = tag.getBoolean("has_zephyr_badge");
        hasHiveBadge    = tag.getBoolean("has_hive_badge");
        hasPlainBadge   = tag.getBoolean("has_plain_badge");
        hasFogBadge     = tag.getBoolean("has_fog_badge");
        hasStormBadge   = tag.getBoolean("has_storm_badge");
        hasMineralBadge = tag.getBoolean("has_mineral_badge");
        hasGlacierBadge = tag.getBoolean("has_glacier_badge");
        hasRisingBadge  = tag.getBoolean("has_rising_badge");

        // Hoenn
        hasStoneBadge   = tag.getBoolean("has_stone_badge");
        hasKnuckleBadge = tag.getBoolean("has_knuckle_badge");
        hasDynamoBadge  = tag.getBoolean("has_dynamo_badge");
        hasHeatBadge    = tag.getBoolean("has_heat_badge");
        hasBalanceBadge = tag.getBoolean("has_balance_badge");
        hasFeatherBadge = tag.getBoolean("has_feather_badge");
        hasMindBadge    = tag.getBoolean("has_mind_badge");
        hasRainBadge    = tag.getBoolean("has_rain_badge");

        // Sinnoh
        hasCoalBadge    = tag.getBoolean("has_coal_badge");
        hasForestBadge  = tag.getBoolean("has_forest_badge");
        hasCobbleBadge  = tag.getBoolean("has_cobble_badge");
        hasFenBadge     = tag.getBoolean("has_fen_badge");
        hasRelicBadge   = tag.getBoolean("has_relic_badge");
        hasMineBadge    = tag.getBoolean("has_mine_badge");
        hasIcicleBadge  = tag.getBoolean("has_icicle_badge");
        hasBeaconBadge  = tag.getBoolean("has_beacon_badge");

        // Unova
        hasTrioBadge    = tag.getBoolean("has_trio_badge");
        hasBasicBadge   = tag.getBoolean("has_basic_badge");
        hasInsectBadge  = tag.getBoolean("has_insect_badge");
        hasBoltBadge    = tag.getBoolean("has_bolt_badge");
        hasQuakeBadge   = tag.getBoolean("has_quake_badge");
        hasJetBadge     = tag.getBoolean("has_jet_badge");
        hasFreezeBadge  = tag.getBoolean("has_freeze_badge");
        hasLegendBadge  = tag.getBoolean("has_legend_badge");
        hasToxicBadge   = tag.getBoolean("has_toxic_badge");
        hasWaveBadge    = tag.getBoolean("has_wave_badge");

        // Kalos
        hasBugBadge      = tag.getBoolean("has_bug_badge");
        hasCliffBadge    = tag.getBoolean("has_cliff_badge");
        hasRumbleBadge   = tag.getBoolean("has_rumble_badge");
        hasPlantBadge    = tag.getBoolean("has_plant_badge");
        hasVoltageBadge  = tag.getBoolean("has_voltage_badge");
        hasFairyBadge    = tag.getBoolean("has_fairy_badge");
        hasPsychicBadge  = tag.getBoolean("has_psychic_badge");
        hasIcebergBadge  = tag.getBoolean("has_iceberg_badge");

        // Galar
        hasGalarGrassBadge    = tag.getBoolean("has_galar_grass_badge");
        hasGalarWaterBadge    = tag.getBoolean("has_galar_water_badge");
        hasGalarFireBadge     = tag.getBoolean("has_galar_fire_badge");
        hasGalarFightingBadge = tag.getBoolean("has_galar_fighting_badge");
        hasGalarGhostBadge    = tag.getBoolean("has_galar_ghost_badge");
        hasGalarFairyBadge    = tag.getBoolean("has_galar_fairy_badge");
        hasGalarRockBadge     = tag.getBoolean("has_galar_rock_badge");
        hasGalarIceBadge      = tag.getBoolean("has_galar_ice_badge");
        hasGalarDarkBadge     = tag.getBoolean("has_galar_dark_badge");
        hasGalarDragonBadge   = tag.getBoolean("has_galar_dragon_badge");

        // Paldea
        hasPaldeaBugBadge      = tag.getBoolean("has_paldea_bug_badge");
        hasPaldeaGrassBadge    = tag.getBoolean("has_paldea_grass_badge");
        hasPaldeaElectricBadge = tag.getBoolean("has_paldea_electric_badge");
        hasPaldeaWaterBadge    = tag.getBoolean("has_paldea_water_badge");
        hasPaldeaNormalBadge   = tag.getBoolean("has_paldea_normal_badge");
        hasPaldeaGhostBadge    = tag.getBoolean("has_paldea_ghost_badge");
        hasPaldeaPsychicBadge  = tag.getBoolean("has_paldea_psychic_badge");
        hasPaldeaIceBadge      = tag.getBoolean("has_paldea_ice_badge");
    }
}