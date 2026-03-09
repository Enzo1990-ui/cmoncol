package com.ogtenzohd.cmoncol.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class CCConfig {
    public static final CCConfig INSTANCE;
    public static final ModConfigSpec SPEC;

    static {
        final Pair<CCConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(CCConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }

    public final ModConfigSpec.ConfigValue<Integer> daycareCurrencyCost;
    public final ModConfigSpec.ConfigValue<String> daycareCurrencyItem;
    public final ModConfigSpec.ConfigValue<Integer> eggCycleThreshold;
    public final ModConfigSpec.ConfigValue<Double> eggGenerationChance;
    public final ModConfigSpec.ConfigValue<Integer> xpPerTick;
    public final ModConfigSpec.ConfigValue<Integer> evsPerCycle;
	public final net.neoforged.neoforge.common.ModConfigSpec.EnumValue<BreedingMode> breedingMode;
	
	public enum BreedingMode {
        CLASSIC, MODERN, EASY
    }

    public CCConfig(ModConfigSpec.Builder builder) {
        builder.push("Daycare Settings");
		breedingMode = builder.comment("Breeding Difficulty. CLASSIC (Gen 2-5), MODERN (Gen 6-8), EASY (Casual/OP)").defineEnum("breedingMode", BreedingMode.MODERN);
        daycareCurrencyCost = builder.comment("Cost per operation").defineInRange("daycareCurrencyCost", 1, 0, 64);
        daycareCurrencyItem = builder.comment("Currency Item ID").define("daycareCurrencyItem", "minecraft:emerald");
        eggCycleThreshold = builder.comment("Ticks per egg check").defineInRange("eggCycleThreshold", 6000, 100, 72000);
        eggGenerationChance = builder.comment("Chance (0.0-1.0) to generate egg").defineInRange("eggGenerationChance", 0.5, 0.0, 1.0);
        xpPerTick = builder.comment("XP awarded per tick in Daycare").defineInRange("xpPerTick", 10, 0, 1000);
        evsPerCycle = builder.comment("EVs awarded per training cycle in Academy").defineInRange("evsPerCycle", 4, 1, 252);
        builder.pop();
        
    }
}