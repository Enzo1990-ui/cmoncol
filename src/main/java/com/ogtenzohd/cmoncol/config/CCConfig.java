package com.ogtenzohd.cmoncol.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class CCConfig {
    public static final CCConfig INSTANCE;
    public static final ModConfigSpec SPEC;

    static {
        final Pair<CCConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(CCConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }

    public final ModConfigSpec.BooleanValue debug;

    public final ModConfigSpec.BooleanValue enableDaycareCost;
    public final ModConfigSpec.ConfigValue<String> daycareCurrencyItem;
    public final ModConfigSpec.ConfigValue<Integer> eggCycleThreshold;
    public final ModConfigSpec.ConfigValue<Double> eggGenerationChance;
    public final ModConfigSpec.ConfigValue<Integer> xpPerTick;
    public final ModConfigSpec.ConfigValue<Integer> evsPerCycle;
    public final ModConfigSpec.EnumValue<BreedingMode> breedingMode;

    public final ModConfigSpec.BooleanValue strictRarityMatching; // NEW
    public final ModConfigSpec.DoubleValue tierUpgradeChance;
    public final ModConfigSpec.DoubleValue legendaryWeight;
    public final ModConfigSpec.DoubleValue shinyWeight;
    public final ModConfigSpec.DoubleValue rareWeight;

    public final ModConfigSpec.ConfigValue<List<? extends String>> gymVictoryCommands;
    public final ModConfigSpec.ConfigValue<List<? extends String>> pokemartTrades;

    public enum BreedingMode {
        CLASSIC, MODERN, EASY
    }

    public CCConfig(ModConfigSpec.Builder builder) {

        builder.push("Debug");
        builder.comment("Debug mode: ");
        debug = builder.define("debug", false);
        builder.pop();
        builder.push("Daycare Settings");
        breedingMode = builder.comment("Breeding Difficulty.").defineEnum("breedingMode", BreedingMode.MODERN);
        enableDaycareCost = builder.comment("Whether withdrawing Pokemon from the Daycare costs currency/items.").define("enableDaycareCost", true);
        daycareCurrencyItem = builder.define("daycareCurrencyItem", "minecraft:emerald");
        eggCycleThreshold = builder.defineInRange("eggCycleThreshold", 6000, 100, 72000);
        eggGenerationChance = builder.defineInRange("eggGenerationChance", 0.5, 0.0, 1.0);
        xpPerTick = builder.defineInRange("xpPerTick", 10, 0, 1000);
        evsPerCycle = builder.defineInRange("evsPerCycle", 4, 1, 252);
        builder.pop();
        
        builder.push("Wonder Trade Settings");
        strictRarityMatching = builder.comment("If true, Wonder Trades will return a Pokemon of the same rarity tier as the one deposited.").define("strictRarityMatching", true);
        tierUpgradeChance = builder.comment("If strict rarity is true, this is the chance (0.0 to 1.0) that the trade will 'upgrade' to the next rarity tier.").defineInRange("tierUpgradeChance", 0.05, 0.0, 1.0);
        legendaryWeight = builder.defineInRange("legendaryWeight", 0.01, 0.0, 1.0);
        shinyWeight = builder.defineInRange("shinyWeight", 0.05, 0.0, 1.0);
        rareWeight = builder.defineInRange("rareWeight", 0.20, 0.0, 1.0);
        builder.pop();

        builder.push("Gym Settings");
        gymVictoryCommands = builder.comment(
                "A list of server commands to execute when a player defeats a Gym Leader for the first time.",
                "Placeholders:",
                "  %player% - The player's name",
                "  %leader% - The capitalized name of the Gym Leader (e.g., Brock)",
                "  %badge%  - The capitalized name of the Badge (e.g., Boulder)"
        ).defineListAllowEmpty(
                "gymVictoryCommands",
                () -> java.util.Arrays.asList(
                        "give %player% minecraft:emerald 5",
                        "tellraw @a {\"text\":\"%player% just defeated Gym Leader %leader% and earned the %badge% Badge!\",\"color\":\"gold\"}"
                ),
                () -> "",
                obj -> obj instanceof String
        );
        builder.pop();
        builder.push("Pokemart Settings");
        pokemartTrades = builder.comment(
                "List of all trades available at the PokeMart.",
                "Format: \"level,action,item_id,currency_amount,item_amount\"",
                "  level: The minimum building level required (1-5)",
                "  action: 'buy' (player pays currency to get items) or 'sell' (player gives items to get currency)",
                "  item_id: The registry name of the item (e.g., 'cobblemon:poke_ball')",
                "  currency_amount: How much currency is paid/rewarded",
                "  item_amount: How many of the item is given/taken"
        ).defineListAllowEmpty(
                "pokemartTrades",
                () -> java.util.Arrays.asList(
                        "1,buy,cobblemon:poke_ball,1,5",
                        "1,buy,cobblemon:potion,1,2",
                        "1,sell,cobblemon:poke_ball,1,10",
                        "1,sell,cobblemon:potion,1,4",

                        "2,buy,cobblemon:great_ball,1,2",
                        "2,buy,cobblemon:super_potion,1,2",
                        "2,buy,cobblemon:repel,1,1",
                        "2,buy,cobblemon:burn_heal,1,1",
                        "2,buy,cobblemon:paralyze_heal,1,1",
                        "2,buy,cobblemon:ice_heal,1,1",
                        "2,buy,cobblemon:awakening,1,1",
                        "2,buy,cobblemon:antidote,1,1",
                        "2,buy,cobblemon:escape_rope,1,2",
                        "2,sell,cobblemon:great_ball,1,4",
                        "2,sell,cobblemon:super_potion,1,4",
                        "2,sell,cobblemon:repel,1,2",
                        "2,sell,cobblemon:burn_heal,1,2",
                        "2,sell,cobblemon:paralyze_heal,1,2",
                        "2,sell,cobblemon:ice_heal,1,2",
                        "2,sell,cobblemon:awakening,1,2",
                        "2,sell,cobblemon:antidote,1,2",
                        "2,sell,cobblemon:escape_rope,1,4",

                        "3,buy,cobblemon:hyper_potion,1,3",
                        "3,buy,cobblemon:revive,1,4",
                        "3,buy,cobblemon:super_repel,1,2",
                        "3,sell,cobblemon:hyper_potion,1,6",
                        "3,sell,cobblemon:revive,1,8",
                        "3,sell,cobblemon:super_repel,1,4",

                        "4,buy,cobblemon:ultra_ball,1,4",
                        "4,buy,cobblemon:max_repel,1,3",
                        "4,buy,cobblemon:full_heal,1,3",
                        "4,sell,cobblemon:ultra_ball,1,8",
                        "4,sell,cobblemon:max_repel,1,6",
                        "4,sell,cobblemon:full_heal,1,6",

                        "5,buy,cobblemon:full_restore,1,6",
                        "5,buy,cobblemon:max_potion,1,6",
                        "5,sell,cobblemon:full_restore,1,12",
                        "5,sell,cobblemon:max_potion,1,10"
                ),
                () -> "1,buy,minecraft:apple,1,1",
                obj -> obj instanceof String
        );
        builder.pop();
    }
}