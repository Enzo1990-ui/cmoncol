package com.ogtenzohd.cmoncol.util;

import com.google.gson.*;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RancherRecipeManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, List<RancherRecipe>> RECIPES = new HashMap<>();

    public record Drop(Item item, int min, int max, float chance) {
    }

    public record RancherRecipe(Item tool, List<Drop> drops) {
        public String getLabel() {
            if (drops.isEmpty()) return "Nothing";

            String toolSuffix = (tool != Items.AIR) ? " (" + new ItemStack(tool).getHoverName().getString() + ")" : "";

            if (drops.size() > 1) {
                return "All Drops" + toolSuffix;
            }

            return new ItemStack(drops.getFirst().item()).getHoverName().getString() + toolSuffix;
        }
    }

    public static void loadLocalConfigs() {
        Path configDir = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("cmoncol");
        Path defaultPath = configDir.resolve("rancher_recipes.json");
        Path customPath = configDir.resolve("custom_rancher_recipes.json");

        try {
            if (!Files.exists(defaultPath)) {
                Files.createDirectories(configDir);
                writeDefaultConfig(defaultPath);
            }
            try (Reader reader = new FileReader(defaultPath.toFile())) {
                parseAndAddRecipes(GSON.fromJson(reader, JsonObject.class));
            }
            if (Files.exists(customPath)) {
                try (Reader reader = new FileReader(customPath.toFile())) {
                    parseAndAddRecipes(GSON.fromJson(reader, JsonObject.class));
                }
            }
        } catch (Exception e) {
            com.ogtenzohd.cmoncol.CobblemonColonies.LOGGER.error("Failed to load local Rancher configs!", e);
        }
    }

    public static boolean addDynamicRecipe(String species, String tool, String drop, int min, int max, float chance) {
        try {
            Path configDir = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("cmoncol");
            File configFile = new File(configDir.toFile(), "custom_rancher_recipes.json");
            JsonObject root;

            if (configFile.exists()) {
                try (Reader reader = new FileReader(configFile)) {
                    root = GSON.fromJson(reader, JsonObject.class);
                }
            } else {
                root = new JsonObject();
            }

            species = species.toLowerCase();
            JsonArray speciesArray = root.has(species) ? root.getAsJsonArray(species) : new JsonArray();
            JsonObject targetRecipe = null;

            for (com.google.gson.JsonElement element : speciesArray) {
                JsonObject existingRecipe = element.getAsJsonObject();
                if (existingRecipe.has("tool") && existingRecipe.get("tool").getAsString().equalsIgnoreCase(tool)) {
                    targetRecipe = existingRecipe;
                    break;
                }
            }

            JsonObject newDrop = createDrop(drop, min, max, chance);

            if (targetRecipe != null) {
                targetRecipe.getAsJsonArray("drops").add(newDrop);
            } else {
                JsonObject newRecipe = new JsonObject();
                newRecipe.addProperty("tool", tool);
                JsonArray dropsArray = new JsonArray();
                dropsArray.add(newDrop);
                newRecipe.add("drops", dropsArray);
                speciesArray.add(newRecipe);
            }

            root.add(species, speciesArray);

            try (Writer writer = new FileWriter(configFile)) {
                GSON.toJson(root, writer);
            }

            RECIPES.remove(species);
            JsonObject speciesOnlyObject = new JsonObject();
            speciesOnlyObject.add(species, speciesArray);
            parseAndAddRecipes(speciesOnlyObject);

            return true;
        } catch (Exception e) {
            CobblemonColonies.LOGGER.error("Failed to add dynamic recipe for {}", species, e);
            return false;
        }
    }

    public static List<RancherRecipe> getRecipesFor(String species) {
        return RECIPES.getOrDefault(species.toLowerCase(), Collections.emptyList());
    }

    public static RancherRecipe getRecipe(String species, int index) {
        List<RancherRecipe> list = getRecipesFor(species);
        if (list.isEmpty()) return null;
        return list.get(Math.abs(index) % list.size());
    }

    private static void writeDefaultConfig(Path path) throws IOException {
        JsonObject root = new JsonObject();

        // LONG LIST START - so i can jump to top

        //gen1
        addRecipe(root, "bulbasaur", "none", "minecraft:melon_seeds", 0, 1, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.05f);
        addRecipe(root, "ivysaur", "none", "minecraft:melon_seeds", 0, 2, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.10f);
        addRecipe(root, "venusaur", "none", "minecraft:melon_seeds", 0, 3, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.25f);

        addRecipe(root, "charmander", "none", "minecraft:blaze_powder", 0, 1, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.05f);
        addRecipe(root, "charmeleon", "none", "minecraft:blaze_powder", 0, 2, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.10f);
        addRecipe(root, "charizard", "none", "minecraft:blaze_powder", 0, 3, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.25f);

        addRecipe(root, "squirtle", "none", "minecraft:turtle_scute", 0, 1, 1.0f, "cobblemon:mystic_water", 1, 1, 0.05f);
        addRecipe(root, "wartortle", "none", "minecraft:turtle_scute", 0, 2, 1.0f, "cobblemon:mystic_water", 1, 1, 0.10f);
        addRecipe(root, "blastoise", "none", "minecraft:turtle_scute", 0, 3, 1.0f, "cobblemon:mystic_water", 1, 1, 0.25f);

        addRecipe(root, "caterpie", "minecraft:shears", "minecraft:string", 0, 1, 1.0f, "cobblemon:wepear_berry", 1, 1, 0.025f);
        addRecipe(root, "metapod", "minecraft:shears", "minecraft:string", 0, 2, 1.0f, "cobblemon:wepear_berry", 1, 1, 0.025f);
        addRecipe(root, "butterfree", "none", "cobblemon:silver_powder", 1, 1, 0.05f, "cobblemon:wepear_berry", 1, 1, 0.05f);

        addRecipe(root, "weedle", "minecraft:shears", "minecraft:string", 0, 1, 1.0f, "cobblemon:pinap_berry", 1, 1, 0.025f);
        addRecipe(root, "kakuna", "minecraft:shears", "minecraft:string", 0, 2, 1.0f, "cobblemon:pinap_berry", 1, 1, 0.025f);
        addRecipe(root, "beedrill", "none", "cobblemon:poison_barb", 1, 1, 0.05f, "cobblemon:pinap_berry", 1, 1, 0.05f);

        addRecipe(root, "pidgey", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:chilan_berry", 1, 1, 0.025f);
        addRecipe(root, "pidgeotto", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.025f, "cobblemon:chilan_berry", 1, 1, 0.05f);
        addRecipe(root, "pidgeot", "minecraft:shears", "minecraft:feather", 0, 3, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.05f, "cobblemon:chilan_berry", 1, 1, 0.10f);

        addRecipe(root, "rattata", "none", "minecraft:rotten_flesh", 0, 1, 1.0f, "cobblemon:chilan_berry", 1, 1, 0.025f);
        addRecipe(root, "raticate", "none", "minecraft:rotten_flesh", 0, 2, 1.0f, "cobblemon:chilan_berry", 1, 1, 0.05f);

        addRecipe(root, "spearow", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.025f, "cobblemon:charti_berry", 1, 1, 0.025f);
        addRecipe(root, "fearow", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.05f, "cobblemon:charti_berry", 1, 1, 0.05f);

        addRecipe(root, "ekans", "none", "cobblemon:razor_fang", 1, 1, 0.025f);
        addRecipe(root, "arbok", "none", "cobblemon:razor_fang", 1, 1, 0.05f);

        addRecipe(root, "pikachu", "none", "cobblemon:light_ball", 1, 1, 0.05f, "minecraft:thunder_stone", 1, 1, 0.05f, "cobblemon:oran_berry", 1, 1, 0.05f);
        addRecipe(root, "raichu", "none", "cobblemon:light_ball", 1, 1, 0.10f, "minecraft:thunder_stone", 1, 1, 0.10f, "cobblemon:oran_berry", 1, 1, 0.10f);

        addRecipe(root, "sandshrew", "none", "minecraft:armadillo_scute", 0, 1, 1.0f, "cobblemon:quick_claw", 1, 1, 0.025f, "minecraft:soft_sand", 1, 1, 0.025f);
        addRecipe(root, "sandslash", "none", "minecraft:armadillo_scute", 0, 2, 1.0f, "cobblemon:quick_claw", 1, 1, 0.05f, "minecraft:soft_sand", 1, 1, 0.05f);

        addRecipe(root, "clefairy", "none", "minecraft:moon_stone", 1, 1, 0.05f, "cobblemon:babiri_berry", 1, 1, 0.05f, "cobblemon:fairy_feather", 1, 1, 0.05f);
        addRecipe(root, "clefable", "none", "minecraft:moon_stone", 1, 1, 0.10f, "cobblemon:babiri_berry", 1, 1, 0.10f, "cobblemon:fairy_feather", 1, 1, 0.10f);

        addRecipe(root, "vulpix", "none", "minecraft:sweet_berries", 1, 3, 1.0f, "minecraft:charcoal", 1, 1, 0.025f, "cobblemon:rawst_berry", 1, 1, 0.025f);
        addRecipe(root, "ninetales", "none", "minecraft:sweet_berries", 2, 4, 1.0f, "minecraft:charcoal", 1, 1, 0.05f, "cobblemon:rawst_berry", 1, 1, 0.05f);

        addRecipe(root, "zubat", "none", "minecraft:phantom_membrane", 1, 1, 0.025f, "cobblemon:razor_fang", 1, 1, 0.025f, "cobblemon:persim_berry", 1, 1, 0.025f);
        addRecipe(root, "golbat", "none", "minecraft:phantom_membrane", 1, 1, 0.05f, "cobblemon:razor_fang", 1, 1, 0.05f, "cobblemon:persim_berry", 1, 1, 0.05f);

        addRecipe(root, "oddish", "none", "cobblemon:absorb_bulb", 1, 1, 0.025f, "cobblemon:pecha_berry", 1, 1, 0.025f);
        addRecipe(root, "gloom", "none", "cobblemon:absorb_bulb", 1, 1, 0.05f, "cobblemon:pecha_berry", 1, 1, 0.05f);
        addRecipe(root, "vileplume", "none", "cobblemon:absorb_bulb", 1, 1, 0.10f, "cobblemon:pecha_berry", 1, 1, 0.10f);

        addRecipe(root, "paras", "none", "minecraft:red_mushroom", 0, 2, 1.0f, "cobblemon:coba_berry", 1, 1, 0.025f);
        addRecipe(root, "parasect", "none", "minecraft:red_mushroom", 1, 1, 1.0f, "cobblemon:coba_berry", 1, 1, 0.05f);

        addRecipe(root, "diglett", "none", "minecraft:dirt", 0, 1, 1.0f, "minecraft:potato", 1, 1, 0.025f, "minecraft:soft_sand", 1, 1, 0.025f);
        addRecipe(root, "dugtrio", "none", "minecraft:dirt", 0, 2, 1.0f, "minecraft:potato", 1, 1, 0.05f, "minecraft:soft_sand", 1, 1, 0.05f);

        addRecipe(root, "meowth", "none", "minecraft:gold_nugget", 0, 1, 1.0f, "cobblemon:quick_claw", 1, 1, 0.025f, "cobblemon:pinap_berry", 1, 1, 0.025f);
        addRecipe(root, "persian", "none", "minecraft:gold_nugget", 0, 2, 1.0f, "cobblemon:quick_claw", 1, 1, 0.05f, "cobblemon:pinap_berry", 1, 1, 0.05f);

        addRecipe(root, "psyduck", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:mystic_water", 1, 1, 0.025f);
        addRecipe(root, "golduck", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:mystic_water", 1, 1, 0.05f);

        addRecipe(root, "mankey", "none", "cobblemon:muscle_band", 1, 1, 0.025f, "cobblemon:payapa_berry", 1, 1, 0.025f);
        addRecipe(root, "primeape", "none", "cobblemon:muscle_band", 1, 1, 0.05f, "cobblemon:payapa_berry", 1, 1, 0.05f);

        addRecipe(root, "growlithe", "none", "minecraft:bone", 0, 1, 1.0f, "cobblemon:rawst_berry", 1, 1, 0.025f);
        addRecipe(root, "arcane", "none", "minecraft:bone", 0, 2, 1.0f, "cobblemon:rawst_berry", 1, 1, 0.05f);

        addRecipe(root, "poliwag", "none", "cobblemon:king_rock", 1, 1, 0.025f, "cobblemon:chesto_berry", 1, 1, 0.025f);
        addRecipe(root, "poliwhirl", "none", "cobblemon:king_rock", 1, 1, 0.05f, "cobblemon:chesto_berry", 1, 1, 0.05f);
        addRecipe(root, "poliwrath", "none", "cobblemon:king_rock", 1, 1, 0.10f, "cobblemon:chesto_berry", 1, 1, 0.10f);

        addRecipe(root, "abra", "none", "minecraft:ender_pearl", 0, 1, 1.0f, "cobblemon:twisted_spoon", 1, 1, 0.025f, "cobblemon:kasib_berry", 1, 1, 0.025f);
        addRecipe(root, "kadabra", "none", "minecraft:ender_pearl", 0, 2, 1.0f, "cobblemon:twisted_spoon", 1, 1, 0.05f, "cobblemon:kasib_berry", 1, 1, 0.05f);
        addRecipe(root, "alakazam", "none", "minecraft:ender_pearl", 0, 3, 1.0f, "cobblemon:twisted_spoon", 1, 1, 0.10f, "cobblemon:kasib_berry", 1, 1, 0.10f);

        addRecipe(root, "machop", "none", "cobblemon:focus_band", 1, 1, 0.025f, "cobblemon:muscle_band", 1, 1, 0.025f, "cobblemon:wepear_berry", 1, 1, 0.025f);
        addRecipe(root, "machoke", "none", "cobblemon:focus_band", 1, 1, 0.05f, "cobblemon:muscle_band", 1, 1, 0.05f, "cobblemon:wepear_berry", 1, 1, 0.05f);
        addRecipe(root, "machamp", "none", "cobblemon:focus_band", 1, 1, 0.10f, "cobblemon:muscle_band", 1, 1, 0.10f, "cobblemon:wepear_berry", 1, 1, 0.10f);

        addRecipe(root, "bellsprout", "none", "cobblemon:big_root", 1, 1, 0.025f, "cobblemon:pinap_berry", 1, 1, 0.025f);
        addRecipe(root, "weepinbell", "none", "cobblemon:big_root", 1, 1, 0.05f, "cobblemon:pinap_berry", 1, 1, 0.05f);
        addRecipe(root, "victreebel", "none", "cobblemon:big_root", 1, 1, 0.10f, "cobblemon:pinap_berry", 1, 1, 0.10f);

        addRecipe(root, "tentacool", "none", "minecraft:ink_sac", 1, 3, 1.0f, "cobblemon:poison_barb", 1, 1, 0.025f);
        addRecipe(root, "tentacruel", "none", "minecraft:ink_sac", 2, 4, 1.0f, "cobblemon:poison_barb", 1, 1, 0.05f);

        addRecipe(root, "geodude", "none", "minecraft:gravel", 0, 1, 1.0f, "cobblemon:everstone", 1, 1, 0.025f, "cobblemon:black_augurite", 1, 1, 0.025f, "cobblemon:rindo_berry", 1, 1, 0.025f);
        addRecipe(root, "graveler", "none", "minecraft:gravel", 0, 2, 1.0f, "cobblemon:everstone", 1, 1, 0.05f, "cobblemon:black_augurite", 1, 1, 0.05f, "cobblemon:rindo_berry", 1, 1, 0.05f);
        addRecipe(root, "golem", "none", "minecraft:gravel", 0, 3, 1.0f, "cobblemon:everstone", 1, 1, 0.10f, "cobblemon:black_augurite", 1, 1, 0.10f, "cobblemon:rindo_berry", 1, 1, 0.10f);

        addRecipe(root, "ponyta", "none", "minecraft:leather", 0, 1, 1.0f, "minecraft:blaze_powder", 1, 1, 0.025f, "cobblemon:shuca_berry", 1, 1, 0.025f);
        addRecipe(root, "rapidash", "none", "minecraft:leather", 0, 2, 1.0f, "minecraft:blaze_powder", 1, 1, 0.05f, "cobblemon:shuca_berry", 1, 1, 0.05f);

        addRecipe(root, "slowpoke", "none", "cobblemon:king_rock", 1, 1, 0.025f, "cobblemon:chesto_berry", 1, 1, 0.025f);
        addRecipe(root, "slowbro", "none", "cobblemon:king_rock", 1, 1, 0.05f, "cobblemon:chesto_berry", 1, 1, 0.05f);

        addRecipe(root, "magnemite", "none", "minecraft:raw_iron", 0, 1, 1.0f, "cobblemon:magnet", 1, 1, 0.025f, "cobblemon:metal_coat", 1, 1, 0.025f, "cobblemon:shuca_berry", 1, 1, 0.025f);
        addRecipe(root, "magneton", "none", "minecraft:raw_iron", 0, 2, 1.0f, "cobblemon:magnet", 1, 1, 0.05f, "cobblemon:metal_coat", 1, 1, 0.05f, "cobblemon:shuca_berry", 1, 1, 0.05f);

        addRecipe(root, "farfetchd", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:chilan_berry", 1, 1, 0.05f);
        addRecipe(root, "doduo", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.025f, "cobblemon:wacan_berry", 1, 1, 0.025f);
        addRecipe(root, "dodrio", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.05f, "cobblemon:wacan_berry", 1, 1, 0.05f);

        addRecipe(root, "seel", "none", "minecraft:cod", 0, 1, 1.0f, "cobblemon:never_melt_ice", 1, 1, 0.025f, "cobblemon:aspear_berry", 1, 1, 0.025f);
        addRecipe(root, "dewgong", "none", "minecraft:cod", 0, 2, 1.0f, "cobblemon:never_melt_ice", 1, 1, 0.05f, "cobblemon:aspear_berry", 1, 1, 0.05f);

        addRecipe(root, "grimer", "none", "minecraft:slime_ball", 0, 1, 1.0f, "cobblemon:black_sludge", 1, 1, 0.025f, "cobblemon:toxic_orb", 1, 1, 0.025f);
        addRecipe(root, "muk", "none", "minecraft:slime_ball", 0, 2, 1.0f, "cobblemon:black_sludge", 1, 1, 0.05f, "cobblemon:toxic_orb", 1, 1, 0.05f);

        addRecipe(root, "shellder", "none", "cobblemon:aspear_berry", 1, 1, 0.025f, "cobblemon:shell_bell", 1, 1, 0.025f);
        addRecipe(root, "cloyster", "none", "cobblemon:aspear_berry", 1, 1, 0.05f, "cobblemon:shell_bell", 1, 1, 0.05f);

        addRecipe(root, "gastly", "none", "cobblemon:spell_tag", 1, 1, 0.025f, "cobblemon:payapa_berry", 1, 1, 0.025f);
        addRecipe(root, "haunter", "none", "cobblemon:spell_tag", 1, 1, 0.05f, "cobblemon:payapa_berry", 1, 1, 0.05f);
        addRecipe(root, "gengar", "none", "cobblemon:spell_tag", 1, 1, 0.10f, "cobblemon:payapa_berry", 1, 1, 0.10f);

        addRecipe(root, "onix", "none", "minecraft:stone", 1, 3, 1.0f, "cobblemon:hard_stone", 1, 1, 0.025f, "cobblemon:passho_berry", 1, 1, 0.025f);
        addRecipe(root, "krabby", "none", "cobblemon:king_rock", 1, 1, 0.025f, "cobblemon:pinap_berry", 1, 1, 0.025f);
        addRecipe(root, "kingler", "none", "cobblemon:king_rock", 1, 1, 0.05f, "cobblemon:pinap_berry", 1, 1, 0.05f);

        addRecipe(root, "voltorb", "none", "minecraft:gunpowder", 0, 1, 1.0f);
        addRecipe(root, "electrode", "none", "minecraft:gunpowder", 0, 2, 1.0f);

        addRecipe(root, "exeggcute", "none", "minecraft:egg", 0, 3, 1.0f, "cobblemon:oval_stone", 1, 1, 0.025f, "cobblemon:tanga_berry", 1, 1, 0.025f);
        addRecipe(root, "exeggutor", "none", "minecraft:egg", 0, 3, 1.0f, "cobblemon:oval_stone", 1, 1, 0.05f, "cobblemon:tanga_berry", 1, 1, 0.05f);

        addRecipe(root, "cubone", "none", "minecraft:bone", 0, 1, 1.0f, "cobblemon:nanab_berry", 1, 1, 0.025f);
        addRecipe(root, "marowak", "none", "minecraft:bone", 1, 1, 1.0f, "cobblemon:nanab_berry", 1, 1, 0.05f);

        addRecipe(root, "hitmonlee", "none", "cobblemon:black_belt", 1, 1, 0.05f, "cobblemon:payapa_berry", 1, 1, 0.05f);
        addRecipe(root, "hitmonchan", "none", "cobblemon:black_belt", 1, 1, 0.05f, "cobblemon:payapa_berry", 1, 1, 0.05f);

        addRecipe(root, "lickitung", "none", "minecraft:slime_ball", 0, 1, 1.0f, "cobblemon:oran_berry", 1, 1, 0.025f);
        addRecipe(root, "koffing", "none", "cobblemon:black_sludge", 1, 1, 0.025f, "cobblemon:smoke_ball", 1, 1, 0.025f);
        addRecipe(root, "weezing", "none", "cobblemon:black_sludge", 1, 1, 0.05f, "cobblemon:smoke_ball", 1, 1, 0.05f);

        addRecipe(root, "rhyhorn", "none", "cobblemon:protector", 1, 1, 0.025f, "cobblemon:passho_berry", 1, 1, 0.025f);
        addRecipe(root, "rhydon", "none", "cobblemon:protector", 1, 1, 0.05f, "cobblemon:passho_berry", 1, 1, 0.05f);

        addRecipe(root, "chansey", "none", "cobblemon:lucky_egg", 1, 1, 0.10f, "cobblemon:oval_stone", 1, 1, 0.10f, "minecraft:egg", 1, 1, 0.50f);
        addRecipe(root, "tangela", "none", "minecraft:vine", 0, 1, 1.0f, "cobblemon:big_root", 1, 1, 0.025f);

        addRecipe(root, "horsea", "none", "minecraft:prismarine_shard", 0, 1, 1.0f, "cobblemon:dragon_scale", 1, 1, 0.025f);
        addRecipe(root, "seadra", "none", "minecraft:prismarine_shard", 0, 2, 1.0f, "cobblemon:dragon_scale", 1, 1, 0.05f);

        addRecipe(root, "goldeen", "none", "minecraft:salmon", 1, 1, 1.0f, "minecraft:bone_meal", 1, 1, 0.05f, "cobblemon:mystic_water", 1, 1, 0.025f, "cobblemon:wacan_berry", 1, 1, 0.025f);
        addRecipe(root, "seaking", "none", "minecraft:salmon", 1, 1, 1.0f, "minecraft:bone_meal", 1, 1, 0.10f, "cobblemon:mystic_water", 1, 1, 0.05f, "cobblemon:wacan_berry", 1, 1, 0.05f);

        addRecipe(root, "mrmime", "none", "cobblemon:light_clay", 1, 1, 0.05f, "cobblemon:babiri_berry", 1, 1, 0.05f);
        addRecipe(root, "scyther", "none", "cobblemon:wepear_berry", 1, 1, 0.025f);
        addRecipe(root, "electabuzz", "none", "minecraft:redstone", 0, 2, 1.0f, "cobblemon:electirizer", 1, 1, 0.05f, "cobblemon:cheri_berry", 1, 1, 0.05f);
        addRecipe(root, "magmar", "none", "minecraft:blaze_powder", 1, 1, 0.05f, "cobblemon:magmarizer", 1, 1, 0.05f, "cobblemon:rawst_berry", 1, 1, 0.05f);

        addRecipe(root, "pinsir", "none", "cobblemon:pinap_berry", 1, 1, 0.05f);
        addRecipe(root, "tauros", "none", "minecraft:leather", 0, 2, 1.0f, "minecraft:beef", 1, 3, 1.0f);
        addRecipe(root, "magikarp", "none", "minecraft:salmon", 1, 1, 1.0f, "minecraft:bone_meal", 1, 1, 0.05f, "cobblemon:wacan_berry", 1, 1, 0.025f);
        addRecipe(root, "gyarados", "none", "minecraft:salmon", 1, 1, 1.0f, "minecraft:bone_meal", 1, 1, 0.10f, "cobblemon:wacan_berry", 1, 1, 0.05f);

        addRecipe(root, "lapras", "none", "cobblemon:mystic_water", 1, 1, 0.05f, "cobblemon:never_melt_ice", 1, 1, 0.05f, "minecraft:heart_of_the_sea", 1, 1, 0.05f, "cobblemon:aspear_berry", 1, 1, 0.05f);
        addRecipe(root, "ditto", "none", "cobblemon:quick_powder", 0, 1, 1.0f, "cobblemon:metal_powder", 1, 1, 0.05f);
        addRecipe(root, "eevee", "none", "minecraft:silk_scarf", 1, 1, 0.05f, "cobblemon:eviolite", 1, 1, 0.05f);

        addRecipe(root, "vaporeon", "none", "minecraft:water_stone", 1, 1, 0.25f, "cobblemon:mystic_water", 1, 1, 0.10f);
        addRecipe(root, "jolteon", "none", "minecraft:thunder_stone", 1, 1, 0.25f, "cobblemon:magnet", 1, 1, 0.10f);
        addRecipe(root, "flareon", "none", "minecraft:fire_stone", 1, 1, 0.25f, "cobblemon:charcoal_stick", 1, 1, 0.10f);

        addRecipe(root, "omanyte", "none", "minecraft:nautilus_shell", 1, 1, 1.0f, "minecraft:bone_meal", 1, 1, 0.05f);
        addRecipe(root, "omastar", "none", "minecraft:nautilus_shell", 1, 1, 1.0f, "minecraft:bone_meal", 1, 1, 0.10f);
        addRecipe(root, "kabuto", "none", "cobblemon:razor_claw", 1, 1, 0.05f, "minecraft:bone_meal", 1, 1, 0.05f);
        addRecipe(root, "kabutops", "none", "cobblemon:razor_claw", 1, 1, 0.10f, "minecraft:bone_meal", 1, 1, 0.10f);
        addRecipe(root, "aerodactyl", "none", "minecraft:bone", 0, 3, 1.0f, "minecraft:rocky_helmet", 1, 1, 0.10f);
        addRecipe(root, "snorlax", "none", "minecraft:apple", 0, 3, 1.0f, "cobblemon:chesto_berry", 1, 1, 0.05f, "minecraft:leftovers", 1, 1, 0.05f);

        addRecipe(root, "dratini", "none", "minecraft:dragon_breath", 0, 1, 1.0f, "cobblemon:dragon_fang", 1, 1, 0.025f, "cobblemon:dragon_scale", 1, 1, 0.025f, "cobblemon:yache_berry", 1, 1, 0.025f);
        addRecipe(root, "dragonair", "none", "minecraft:dragon_breath", 0, 2, 1.0f, "cobblemon:dragon_fang", 1, 1, 0.05f, "cobblemon:dragon_scale", 1, 1, 0.05f, "cobblemon:yache_berry", 1, 1, 0.05f);
        addRecipe(root, "dragonite", "none", "minecraft:dragon_breath", 0, 3, 1.0f, "cobblemon:dragon_fang", 1, 1, 0.10f, "cobblemon:dragon_scale", 1, 1, 0.10f, "cobblemon:yache_berry", 1, 1, 0.10f);

        //gen2
        addRecipe(root, "chikorita", "none", "cobblemon:miracle_seed", 1, 1, 0.05f);
        addRecipe(root, "bayleef", "none", "cobblemon:miracle_seed", 1, 1, 0.10f);
        addRecipe(root, "meganium", "none", "cobblemon:miracle_seed", 1, 1, 0.25f);

        addRecipe(root, "cyndaquil", "none", "cobblemon:charcoal_stick", 1, 1, 0.05f);
        addRecipe(root, "quilava", "none", "cobblemon:charcoal_stick", 1, 1, 0.10f);
        addRecipe(root, "typhlosion", "none", "cobblemon:charcoal_stick", 1, 1, 0.25f);

        addRecipe(root, "totodile", "none", "cobblemon:mystic_water", 1, 1, 0.05f);
        addRecipe(root, "croconaw", "none", "cobblemon:mystic_water", 1, 1, 0.10f);
        addRecipe(root, "feraligatr", "none", "cobblemon:mystic_water", 1, 1, 0.25f);

        addRecipe(root, "sentret", "none", "minecraft:silk_scarf", 1, 1, 0.025f, "cobblemon:oran_berry", 1, 1, 0.025f);
        addRecipe(root, "furret", "none", "minecraft:silk_scarf", 1, 1, 0.05f, "cobblemon:oran_berry", 1, 1, 0.05f);

        addRecipe(root, "hoothoot", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:wise_glasses", 1, 1, 0.025f, "cobblemon:chilan_berry", 1, 1, 0.025f);
        addRecipe(root, "noctowl", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:wise_glasses", 1, 1, 0.05f, "cobblemon:chilan_berry", 1, 1, 0.05f);

        addRecipe(root, "ledyba", "none", "cobblemon:charti_berry", 1, 1, 0.025f);
        addRecipe(root, "ledian", "none", "cobblemon:charti_berry", 1, 1, 0.05f);

        addRecipe(root, "spinarak", "minecraft:shears", "minecraft:string", 0, 1, 1.0f, "minecraft:spider_eye", 0, 1, 1.0f, "minecraft:rotten_flesh", 0, 1, 1.0f, "cobblemon:poison_barb", 1, 1, 0.025f);
        addRecipe(root, "ariados", "minecraft:shears", "minecraft:string", 0, 2, 1.0f, "minecraft:spider_eye", 0, 2, 1.0f, "minecraft:rotten_flesh", 0, 2, 1.0f, "cobblemon:poison_barb", 1, 1, 0.05f);

        addRecipe(root, "crobat", "none", "minecraft:phantom_membrane", 1, 1, 0.10f, "cobblemon:razor_fang", 1, 1, 0.10f, "cobblemon:persim_berry", 1, 1, 0.10f);
        addRecipe(root, "chinchou", "none", "minecraft:prismarine_crystals", 0, 1, 1.0f, "cobblemon:deep_sea_scale", 1, 1, 0.025f, "cobblemon:persim_berry", 1, 1, 0.025f);
        addRecipe(root, "lanturn", "none", "minecraft:prismarine_crystals", 0, 2, 1.0f, "cobblemon:deep_sea_scale", 1, 1, 0.05f, "cobblemon:persim_berry", 1, 1, 0.05f);

        addRecipe(root, "pichu", "none", "cobblemon:light_ball", 1, 1, 0.025f, "minecraft:thunder_stone", 1, 1, 0.025f, "cobblemon:oran_berry", 1, 1, 0.025f);
        addRecipe(root, "cleffa", "none", "minecraft:moon_stone", 1, 1, 0.025f, "cobblemon:babiri_berry", 1, 1, 0.025f, "cobblemon:fairy_feather", 1, 1, 0.025f);
        addRecipe(root, "igglybuff", "none", "minecraft:moon_stone", 1, 1, 0.025f, "minecraft:air_balloon", 1, 1, 0.025f, "cobblemon:oran_berry", 1, 1, 0.025f);

        addRecipe(root, "natu", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:wise_glasses", 1, 1, 0.025f, "cobblemon:persim_berry", 1, 1, 0.025f);
        addRecipe(root, "xatu", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:wise_glasses", 1, 1, 0.05f, "cobblemon:persim_berry", 1, 1, 0.05f);

        addRecipe(root, "mareep", "minecraft:shears", "minecraft:mutton", 1, 2, 1.0f, "minecraft:white_wool", 1, 2, 1.0f, "cobblemon:cheri_berry", 1, 1, 0.025f);
        addRecipe(root, "flaaffy", "minecraft:shears", "minecraft:mutton", 1, 3, 1.0f, "minecraft:white_wool", 1, 1, 1.0f, "cobblemon:cheri_berry", 1, 1, 0.05f);
        addRecipe(root, "ampharos", "none", "minecraft:mutton", 2, 4, 1.0f, "cobblemon:cheri_berry", 1, 1, 0.10f);

        addRecipe(root, "bellossom", "none", "cobblemon:absorb_bulb", 1, 1, 0.10f, "cobblemon:pecha_berry", 1, 1, 0.10f);
        addRecipe(root, "sudowoodo", "none", "minecraft:hard_stone", 1, 1, 0.05f, "cobblemon:passho_berry", 1, 1, 0.05f);
        addRecipe(root, "politoed", "none", "minecraft:verdant_froglight", 1, 1, 1.0f, "cobblemon:king_rock", 1, 1, 0.10f, "cobblemon:chesto_berry", 1, 1, 0.10f);

        addRecipe(root, "aipom", "none", "cobblemon:nanab_berry", 1, 1, 0.025f);
        addRecipe(root, "sunkern", "none", "minecraft:wheat_seeds", 1, 1, 1.0f, "cobblemon:coba_berry", 1, 1, 0.025f);
        addRecipe(root, "sunflora", "none", "minecraft:sunflower", 1, 1, 1.0f, "cobblemon:coba_berry", 1, 1, 0.05f);

        addRecipe(root, "yanma", "none", "cobblemon:charti_berry", 1, 1, 0.025f);
        addRecipe(root, "wooper", "none", "minecraft:clay_ball", 0, 1, 1.0f, "cobblemon:rindo_berry", 1, 1, 0.025f);
        addRecipe(root, "quagsire", "none", "minecraft:clay_ball", 0, 2, 1.0f, "cobblemon:rindo_berry", 1, 1, 0.05f);

        addRecipe(root, "espeon", "none", "minecraft:sun_stone", 1, 1, 0.25f, "cobblemon:twisted_spoon", 1, 1, 0.10f);
        addRecipe(root, "umbreon", "none", "minecraft:moon_stone", 1, 1, 0.25f, "cobblemon:black_glasses", 1, 1, 0.10f);
        addRecipe(root, "murkrow", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:bluk_berry", 1, 1, 0.025f);
        addRecipe(root, "slowking", "none", "cobblemon:king_rock", 1, 1, 0.10f, "cobblemon:chesto_berry", 1, 1, 0.10f);

        addRecipe(root, "misdreavus", "none", "minecraft:phantom_membrane", 1, 1, 0.025f, "cobblemon:spell_tag", 1, 1, 0.025f, "cobblemon:kasib_berry", 1, 1, 0.025f);
        addRecipe(root, "wobbuffet", "none", "cobblemon:colbur_berry", 1, 1, 0.05f);
        addRecipe(root, "girafarig", "none", "minecraft:leather", 0, 1, 1.0f, "cobblemon:razor_fang", 1, 1, 0.025f, "cobblemon:persim_berry", 1, 1, 0.025f);

        addRecipe(root, "pineco", "minecraft:shears", "minecraft:string", 0, 1, 1.0f, "cobblemon:occa_berry", 1, 1, 0.025f, "minecraft:iron_ball", 1, 1, 0.025f);
        addRecipe(root, "forretress", "minecraft:shears", "minecraft:string", 0, 2, 1.0f, "cobblemon:occa_berry", 1, 1, 0.05f, "minecraft:iron_ball", 1, 1, 0.05f);

        addRecipe(root, "gligar", "none", "minecraft:phantom_membrane", 1, 1, 0.025f, "cobblemon:razor_fang", 1, 1, 0.025f, "cobblemon:poison_barb", 1, 1, 0.025f, "cobblemon:yache_berry", 1, 1, 0.025f);
        addRecipe(root, "steelix", "none", "minecraft:raw_iron", 2, 4, 1.0f, "cobblemon:metal_coat", 1, 1, 0.05f, "cobblemon:passho_berry", 1, 1, 0.05f);

        addRecipe(root, "snubbull", "none", "minecraft:bone", 0, 1, 1.0f, "cobblemon:babiri_berry", 1, 1, 0.025f);
        addRecipe(root, "granbull", "none", "minecraft:bone", 0, 2, 1.0f, "cobblemon:babiri_berry", 1, 1, 0.05f);

        addRecipe(root, "qwilfish", "none", "minecraft:pufferfish", 1, 1, 1.0f, "minecraft:prismarine_shard", 0, 1, 1.0f, "cobblemon:poison_barb", 1, 1, 0.025f);
        addRecipe(root, "scizor", "none", "cobblemon:wepear_berry", 1, 1, 0.05f);
        addRecipe(root, "shuckle", "none", "cobblemon:berry_juice", 1, 1, 1.0f);
        addRecipe(root, "heracross", "none", "cobblemon:coba_berry", 1, 1, 0.05f);

        addRecipe(root, "sneasel", "none", "cobblemon:razor_claw", 1, 1, 0.025f, "cobblemon:quick_claw", 1, 1, 0.025f, "cobblemon:chople_berry", 1, 1, 0.025f);
        addRecipe(root, "teddiursa", "none", "minecraft:sweet_berries", 1, 3, 1.0f, "minecraft:honey_bottle", 1, 1, 0.025f, "cobblemon:peat_block", 1, 1, 0.025f, "cobblemon:oran_berry", 1, 1, 0.025f);
        addRecipe(root, "ursaring", "none", "minecraft:sweet_berries", 2, 4, 1.0f, "minecraft:honey_bottle", 1, 1, 0.05f, "cobblemon:peat_block", 1, 1, 0.05f, "cobblemon:oran_berry", 1, 1, 0.05f);

        addRecipe(root, "slugma", "none", "minecraft:magma_cream", 0, 1, 1.0f, "cobblemon:passho_berry", 1, 1, 0.025f);
        addRecipe(root, "magcargo", "none", "minecraft:magma_cream", 0, 2, 1.0f, "minecraft:basalt", 1, 1, 1.0f, "cobblemon:passho_berry", 1, 1, 0.05f);

        addRecipe(root, "swinub", "minecraft:shears", "minecraft:porkchop", 1, 1, 1.0f, "minecraft:brown_wool", 1, 1, 1.0f, "cobblemon:never_melt_ice", 1, 1, 0.025f, "cobblemon:aspear_berry", 1, 1, 0.025f);
        addRecipe(root, "piloswine", "minecraft:shears", "minecraft:porkchop", 1, 2, 1.0f, "minecraft:brown_wool", 1, 2, 1.0f, "cobblemon:never_melt_ice", 1, 1, 0.05f, "cobblemon:aspear_berry", 1, 1, 0.05f);

        addRecipe(root, "corsola", "none", "cobblemon:rindo_berry", 1, 1, 0.05f);
        addRecipe(root, "remoraid", "none", "minecraft:cod", 1, 1, 1.0f, "minecraft:bone_meal", 1, 1, 0.05f, "cobblemon:aspear_berry", 1, 1, 0.025f);
        addRecipe(root, "octillery", "none", "minecraft:ink_sac", 1, 3, 1.0f, "cobblemon:aspear_berry", 1, 1, 0.05f);

        addRecipe(root, "delibird", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:charti_berry", 1, 1, 0.05f);
        addRecipe(root, "mantine", "none", "minecraft:cod", 0, 2, 1.0f, "cobblemon:wacan_berry", 1, 1, 0.05f);
        addRecipe(root, "skarmory", "none", "cobblemon:sharp_beak", 1, 1, 0.05f, "cobblemon:metal_coat", 1, 1, 0.05f, "cobblemon:pinap_berry", 1, 1, 0.05f);

        addRecipe(root, "kingdra", "none", "minecraft:prismarine_shard", 0, 3, 1.0f, "cobblemon:dragon_scale", 1, 1, 0.10f);
        addRecipe(root, "phanpy", "none", "minecraft:clay_ball", 0, 1, 1.0f, "cobblemon:passho_berry", 1, 1, 0.025f);
        addRecipe(root, "donphan", "none", "minecraft:clay_ball", 0, 2, 1.0f, "cobblemon:protector", 1, 1, 0.05f, "cobblemon:passho_berry", 1, 1, 0.05f);

        addRecipe(root, "stantler", "none", "minecraft:leather", 0, 1, 1.0f, "cobblemon:persim_berry", 1, 1, 0.025f);
        addRecipe(root, "tyrogue", "none", "cobblemon:black_belt", 1, 1, 0.025f, "cobblemon:payapa_berry", 1, 1, 0.025f);
        addRecipe(root, "hitmontop", "none", "cobblemon:black_belt", 1, 1, 0.05f, "cobblemon:payapa_berry", 1, 1, 0.05f);

        addRecipe(root, "smoochum", "none", "cobblemon:aspear_berry", 1, 1, 0.025f);
        addRecipe(root, "elekid", "none", "minecraft:redstone", 0, 1, 1.0f, "cobblemon:electirizer", 1, 1, 0.025f, "cobblemon:cheri_berry", 1, 1, 0.025f);
        addRecipe(root, "magby", "none", "minecraft:blaze_powder", 1, 1, 0.025f, "cobblemon:magmarizer", 1, 1, 0.025f, "cobblemon:rawst_berry", 1, 1, 0.025f);

        addRecipe(root, "miltank", "minecraft:bucket", "minecraft:milk_bucket", 1, 1, 1.0f);
        addRecipe(root, "miltank", "none", "minecraft:leather", 0, 2, 1.0f, "minecraft:beef", 1, 3, 1.0f, "cobblemon:oran_berry", 1, 1, 0.05f);

        addRecipe(root, "blissey", "none", "cobblemon:lucky_egg", 1, 1, 0.25f, "cobblemon:oval_stone", 1, 1, 0.25f, "minecraft:egg", 1, 1, 0.50f);
        addRecipe(root, "larvitar", "none", "minecraft:hard_stone", 1, 1, 0.025f, "cobblemon:chople_berry", 1, 1, 0.025f);
        addRecipe(root, "pupitar", "none", "minecraft:hard_stone", 1, 1, 0.05f, "cobblemon:chople_berry", 1, 1, 0.05f);
        addRecipe(root, "tyranitar", "none", "minecraft:hard_stone", 1, 1, 0.10f, "cobblemon:chople_berry", 1, 1, 0.10f);

        //gen3
        addRecipe(root, "treecko", "none", "minecraft:stick", 0, 1, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.05f);
        addRecipe(root, "grovyle", "none", "minecraft:stick", 0, 2, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.10f);
        addRecipe(root, "sceptile", "none", "minecraft:stick", 0, 3, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.25f);

        addRecipe(root, "torchic", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.05f);
        addRecipe(root, "combusken", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.10f);
        addRecipe(root, "blaziken", "minecraft:shears", "minecraft:feather", 0, 3, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.25f);

        addRecipe(root, "mudkip", "none", "minecraft:clay_ball", 0, 1, 1.0f, "cobblemon:mystic_water", 1, 1, 0.05f);
        addRecipe(root, "marshtomp", "none", "minecraft:clay_ball", 0, 2, 1.0f, "cobblemon:mystic_water", 1, 1, 0.10f);
        addRecipe(root, "swampert", "none", "minecraft:clay_ball", 0, 3, 1.0f, "cobblemon:mystic_water", 1, 1, 0.25f);

        addRecipe(root, "poochyena", "none", "minecraft:bone", 0, 1, 1.0f, "minecraft:rotten_flesh", 0, 1, 1.0f, "cobblemon:pecha_berry", 1, 1, 0.025f);
        addRecipe(root, "mightyena", "none", "minecraft:bone", 0, 2, 1.0f, "minecraft:rotten_flesh", 0, 2, 1.0f, "cobblemon:pecha_berry", 1, 1, 0.05f);

        addRecipe(root, "zigzagoon", "none", "cobblemon:oran_berry", 1, 1, 0.025f);
        addRecipe(root, "linoone", "none", "cobblemon:oran_berry", 1, 1, 0.05f);

        addRecipe(root, "lotad", "none", "minecraft:lily_pad", 0, 1, 1.0f, "cobblemon:mental_herb", 1, 1, 0.025f, "cobblemon:kebia_berry", 1, 1, 0.025f);
        addRecipe(root, "lombre", "none", "minecraft:lily_pad", 0, 2, 1.0f, "cobblemon:mental_herb", 1, 1, 0.05f, "cobblemon:kebia_berry", 1, 1, 0.05f);
        addRecipe(root, "ludicolo", "none", "minecraft:lily_pad", 0, 3, 1.0f, "cobblemon:mental_herb", 1, 1, 0.10f, "cobblemon:kebia_berry", 1, 1, 0.10f);

        addRecipe(root, "seedot", "none", "minecraft:dark_oak_sapling", 0, 1, 1.0f, "cobblemon:tanga_berry", 1, 1, 0.025f);
        addRecipe(root, "nuzleaf", "none", "minecraft:dark_oak_sapling", 0, 2, 1.0f, "cobblemon:tanga_berry", 1, 1, 0.05f);
        addRecipe(root, "shiftry", "none", "minecraft:dark_oak_sapling", 0, 3, 1.0f, "cobblemon:tanga_berry", 1, 1, 0.10f);

        addRecipe(root, "taillow", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:charti_berry", 1, 1, 0.025f);
        addRecipe(root, "swellow", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.025f, "cobblemon:charti_berry", 1, 1, 0.05f);

        addRecipe(root, "wingull", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:wacan_berry", 1, 1, 0.025f);
        addRecipe(root, "pelipper", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:wacan_berry", 1, 1, 0.05f);

        addRecipe(root, "ralts", "none", "minecraft:ender_pearl", 0, 1, 1.0f, "minecraft:dawn_stone", 1, 1, 0.025f, "cobblemon:twisted_spoon", 1, 1, 0.025f, "cobblemon:kasib_berry", 1, 1, 0.025f);
        addRecipe(root, "kirlia", "none", "minecraft:ender_pearl", 0, 2, 1.0f, "minecraft:dawn_stone", 1, 1, 0.05f, "cobblemon:twisted_spoon", 1, 1, 0.05f, "cobblemon:kasib_berry", 1, 1, 0.05f);
        addRecipe(root, "gardevoir", "none", "minecraft:ender_pearl", 0, 3, 1.0f, "minecraft:dawn_stone", 1, 1, 0.10f, "cobblemon:twisted_spoon", 1, 1, 0.10f, "cobblemon:kasib_berry", 1, 1, 0.10f);

        addRecipe(root, "surskit", "none", "minecraft:honey_bottle", 1, 1, 0.025f, "cobblemon:wacan_berry", 1, 1, 0.025f);
        addRecipe(root, "masquerain", "none", "minecraft:honey_bottle", 1, 1, 0.025f, "cobblemon:silver_powder", 1, 1, 0.05f, "cobblemon:wacan_berry", 1, 1, 0.05f);

        addRecipe(root, "shroomish", "none", "minecraft:brown_mushroom", 0, 1, 1.0f, "cobblemon:kebia_berry", 1, 1, 0.025f);
        addRecipe(root, "breloom", "none", "minecraft:brown_mushroom", 0, 2, 1.0f, "cobblemon:kebia_berry", 1, 1, 0.05f);

        addRecipe(root, "slakoth", "none", "cobblemon:chesto_berry", 1, 1, 0.025f);
        addRecipe(root, "vigoroth", "none", "cobblemon:chesto_berry", 1, 1, 0.05f);
        addRecipe(root, "slaking", "none", "cobblemon:chesto_berry", 1, 1, 0.10f);

        addRecipe(root, "nincada", "none", "minecraft:soft_sand", 1, 1, 0.025f);
        addRecipe(root, "whismur", "none", "cobblemon:chesto_berry", 1, 1, 0.025f);
        addRecipe(root, "loudred", "none", "cobblemon:chesto_berry", 1, 1, 0.05f);
        addRecipe(root, "exploud", "none", "cobblemon:chesto_berry", 1, 1, 0.10f);

        addRecipe(root, "makuhita", "none", "cobblemon:black_belt", 1, 1, 0.025f, "cobblemon:king_rock", 1, 1, 0.025f, "cobblemon:payapa_berry", 1, 1, 0.025f);
        addRecipe(root, "hariyama", "none", "cobblemon:black_belt", 1, 1, 0.05f, "cobblemon:king_rock", 1, 1, 0.05f, "cobblemon:payapa_berry", 1, 1, 0.05f);

        addRecipe(root, "nosepass", "none", "minecraft:flint", 0, 1, 1.0f, "cobblemon:hard_stone", 1, 1, 0.025f, "cobblemon:magnet", 1, 1, 0.025f, "cobblemon:chople_berry", 1, 1, 0.025f);
        addRecipe(root, "sableye", "none", "minecraft:amethyst_shard", 0, 2, 1.0f, "minecraft:diamond", 1, 1, 0.05f, "minecraft:emerald", 1, 1, 0.05f, "cobblemon:roseli_berry", 1, 1, 0.05f);
        addRecipe(root, "mawile", "none", "cobblemon:razor_fang", 1, 1, 0.05f, "cobblemon:occa_berry", 1, 1, 0.05f, "minecraft:iron_ball", 1, 1, 0.05f);

        addRecipe(root, "aron", "none", "minecraft:raw_iron", 0, 1, 1.0f, "cobblemon:hard_stone", 1, 1, 0.025f, "cobblemon:chople_berry", 1, 1, 0.025f);
        addRecipe(root, "lairon", "none", "minecraft:raw_iron", 0, 2, 1.0f, "cobblemon:hard_stone", 1, 1, 0.05f, "cobblemon:chople_berry", 1, 1, 0.05f);
        addRecipe(root, "aggron", "none", "minecraft:raw_iron", 0, 3, 1.0f, "cobblemon:hard_stone", 1, 1, 0.10f, "cobblemon:chople_berry", 1, 1, 0.10f);

        addRecipe(root, "plusle", "none", "cobblemon:magnet", 1, 1, 0.05f, "minecraft:cell_battery", 1, 1, 0.05f, "cobblemon:cheri_berry", 1, 1, 0.05f);
        addRecipe(root, "minun", "none", "cobblemon:magnet", 1, 1, 0.05f, "minecraft:cell_battery", 1, 1, 0.05f, "cobblemon:cheri_berry", 1, 1, 0.05f);

        addRecipe(root, "volbeat", "none", "minecraft:bright_powder", 1, 1, 0.05f, "cobblemon:razz_berry", 1, 1, 0.05f);
        addRecipe(root, "illumise", "none", "minecraft:bright_powder", 1, 1, 0.05f, "cobblemon:bluk_berry", 1, 1, 0.05f);
        addRecipe(root, "roselia", "none", "minecraft:rose_bush", 0, 1, 1.0f, "cobblemon:poison_barb", 1, 1, 0.05f, "cobblemon:absorb_bulb", 1, 1, 0.05f, "cobblemon:pecha_berry", 1, 1, 0.05f);

        addRecipe(root, "carvanha", "none", "cobblemon:deep_sea_tooth", 1, 1, 0.025f, "cobblemon:aspear_berry", 1, 1, 0.025f);
        addRecipe(root, "sharpedo", "none", "cobblemon:deep_sea_tooth", 1, 1, 0.05f, "cobblemon:aspear_berry", 1, 1, 0.05f);

        addRecipe(root, "wailmer", "none", "minecraft:bone_block", 0, 1, 1.0f, "cobblemon:chesto_berry", 1, 1, 0.025f);
        addRecipe(root, "wailord", "none", "minecraft:bone_block", 0, 2, 1.0f, "cobblemon:chesto_berry", 1, 1, 0.05f);

        addRecipe(root, "numel", "none", "minecraft:magma_cream", 1, 1, 0.025f, "cobblemon:rawst_berry", 1, 1, 0.025f);
        addRecipe(root, "camerupt", "none", "minecraft:magma_cream", 1, 1, 0.05f, "cobblemon:rawst_berry", 1, 1, 0.05f);
        addRecipe(root, "torkoal", "none", "minecraft:coal", 0, 2, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.05f, "cobblemon:smoke_ball", 1, 1, 0.05f, "cobblemon:charti_berry", 1, 1, 0.05f);

        addRecipe(root, "spinda", "none", "minecraft:bamboo", 0, 2, 1.0f, "cobblemon:chesto_berry", 1, 1, 0.05f);
        addRecipe(root, "trapinch", "none", "minecraft:soft_sand", 1, 1, 0.025f, "cobblemon:yache_berry", 1, 1, 0.025f);
        addRecipe(root, "vibrava", "none", "minecraft:soft_sand", 1, 1, 0.05f, "cobblemon:yache_berry", 1, 1, 0.05f);
        addRecipe(root, "flygon", "none", "minecraft:soft_sand", 1, 1, 0.10f, "cobblemon:yache_berry", 1, 1, 0.10f);

        addRecipe(root, "cacnea", "none", "minecraft:cactus", 0, 1, 1.0f, "cobblemon:poison_barb", 1, 1, 0.025f, "cobblemon:tanga_berry", 1, 1, 0.025f, "minecraft:sticky_barb", 1, 1, 0.025f);
        addRecipe(root, "cacturne", "none", "minecraft:cactus", 0, 2, 1.0f, "cobblemon:poison_barb", 1, 1, 0.05f, "cobblemon:tanga_berry", 1, 1, 0.05f, "minecraft:sticky_barb", 1, 1, 0.05f);

        addRecipe(root, "lunatone", "none", "minecraft:moon_stone", 1, 1, 0.05f, "cobblemon:colbur_berry", 1, 1, 0.05f);
        addRecipe(root, "solrock", "none", "minecraft:sun_stone", 1, 1, 0.05f, "cobblemon:colbur_berry", 1, 1, 0.05f);

        addRecipe(root, "barboach", "none", "minecraft:clay_ball", 0, 1, 1.0f, "cobblemon:rindo_berry", 1, 1, 0.025f);
        addRecipe(root, "whiscash", "none", "minecraft:clay_ball", 0, 2, 1.0f, "cobblemon:rindo_berry", 1, 1, 0.05f);

        addRecipe(root, "baltoy", "none", "minecraft:clay_ball", 0, 1, 1.0f, "cobblemon:light_clay", 1, 1, 0.025f, "cobblemon:colbur_berry", 1, 1, 0.025f);
        addRecipe(root, "claydol", "none", "minecraft:clay_ball", 0, 2, 1.0f, "cobblemon:light_clay", 1, 1, 0.05f, "cobblemon:colbur_berry", 1, 1, 0.05f);

        addRecipe(root, "lileep", "none", "cobblemon:big_root", 1, 1, 0.05f);
        addRecipe(root, "cradily", "none", "cobblemon:big_root", 1, 1, 0.10f);
        addRecipe(root, "anorith", "none", "cobblemon:quick_claw", 1, 1, 0.05f);
        addRecipe(root, "armaldo", "none", "cobblemon:quick_claw", 1, 1, 0.10f);

        addRecipe(root, "feebas", "none", "minecraft:salmon", 1, 1, 1.0f, "minecraft:bone_meal", 1, 1, 0.05f, "cobblemon:prism_scale", 1, 1, 0.025f);
        addRecipe(root, "milotic", "none", "minecraft:bone_meal", 1, 1, 0.10f, "cobblemon:prism_scale", 1, 1, 0.05f);

        addRecipe(root, "kecleon", "none", "cobblemon:persim_berry", 1, 1, 0.05f);
        addRecipe(root, "duskull", "none", "minecraft:phantom_membrane", 1, 1, 0.025f, "cobblemon:reaper_cloth", 1, 1, 0.025f, "cobblemon:kasib_berry", 1, 1, 0.025f);
        addRecipe(root, "dusclops", "none", "minecraft:phantom_membrane", 1, 1, 0.05f, "cobblemon:reaper_cloth", 1, 1, 0.05f, "cobblemon:kasib_berry", 1, 1, 0.05f);

        addRecipe(root, "tropius", "none", "cobblemon:nanab_berry", 0, 2, 1.0f);
        addRecipe(root, "chimecho", "none", "cobblemon:colbur_berry", 1, 1, 0.05f, "cobblemon:cleanse_tag", 1, 1, 0.05f, "minecraft:soothe_bell", 1, 1, 0.05f);
        addRecipe(root, "absol", "none", "cobblemon:razor_claw", 1, 1, 0.05f, "minecraft:life_orb", 1, 1, 0.05f, "cobblemon:roseli_berry", 1, 1, 0.05f);
        addRecipe(root, "wynaut", "none", "cobblemon:colbur_berry", 1, 1, 0.025f);

        addRecipe(root, "spheal", "none", "minecraft:cod", 0, 1, 1.0f, "cobblemon:chesto_berry", 1, 1, 0.025f);
        addRecipe(root, "sealeo", "none", "minecraft:cod", 0, 2, 1.0f, "cobblemon:chesto_berry", 1, 1, 0.05f);
        addRecipe(root, "walrein", "none", "minecraft:cod", 0, 3, 1.0f, "cobblemon:chesto_berry", 1, 1, 0.10f);

        addRecipe(root, "clamperl", "none", "cobblemon:shell_bell", 1, 1, 0.025f);
        addRecipe(root, "huntail", "none", "cobblemon:deep_sea_tooth", 1, 1, 0.05f);
        addRecipe(root, "gorebyss", "none", "cobblemon:deep_sea_scale", 1, 1, 0.05f);

        addRecipe(root, "relicanth", "none", "cobblemon:deep_sea_scale", 1, 1, 0.05f, "cobblemon:rindo_berry", 1, 1, 0.05f);
        addRecipe(root, "luvdisc", "none", "cobblemon:deep_sea_scale", 1, 1, 0.025f);

        addRecipe(root, "bagon", "none", "cobblemon:dragon_fang", 1, 1, 0.025f, "cobblemon:dragon_scale", 1, 1, 0.025f, "cobblemon:yache_berry", 1, 1, 0.025f);
        addRecipe(root, "shelgon", "none", "cobblemon:dragon_fang", 1, 1, 0.05f, "cobblemon:dragon_scale", 1, 1, 0.05f, "cobblemon:yache_berry", 1, 1, 0.05f);
        addRecipe(root, "salamence", "none", "cobblemon:dragon_fang", 1, 1, 0.10f, "cobblemon:dragon_scale", 1, 1, 0.10f, "cobblemon:yache_berry", 1, 1, 0.10f);

        addRecipe(root, "beldum", "none", "minecraft:ender_pearl", 0, 1, 1.0f, "cobblemon:metal_coat", 1, 1, 0.025f);
        addRecipe(root, "metang", "none", "minecraft:ender_pearl", 0, 2, 1.0f, "cobblemon:metal_coat", 1, 1, 0.05f);
        addRecipe(root, "metagross", "none", "minecraft:ender_pearl", 0, 3, 1.0f, "cobblemon:metal_coat", 1, 1, 0.10f);

        //gen4 why did i do this to myself?
        addRecipe(root, "turtwig", "none", "cobblemon:miracle_seed", 1, 1, 0.05f);
        addRecipe(root, "grotle", "none", "cobblemon:miracle_seed", 1, 1, 0.10f);
        addRecipe(root, "torterra", "none", "cobblemon:miracle_seed", 1, 1, 0.25f);

        addRecipe(root, "chimchar", "none", "cobblemon:charcoal_stick", 1, 1, 0.05f);
        addRecipe(root, "monferno", "none", "cobblemon:charcoal_stick", 1, 1, 0.10f);
        addRecipe(root, "infernape", "none", "cobblemon:charcoal_stick", 1, 1, 0.25f);

        addRecipe(root, "piplup", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:mystic_water", 1, 1, 0.05f);
        addRecipe(root, "prinplup", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:mystic_water", 1, 1, 0.10f);
        addRecipe(root, "empoleon", "minecraft:shears", "minecraft:feather", 0, 3, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:mystic_water", 1, 1, 0.25f);

        addRecipe(root, "starly", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:yache_berry", 1, 1, 0.025f);
        addRecipe(root, "staravia", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.025f, "cobblemon:yache_berry", 1, 1, 0.05f);
        addRecipe(root, "staraptor", "minecraft:shears", "minecraft:feather", 0, 3, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.05f, "cobblemon:yache_berry", 1, 1, 0.10f);

        addRecipe(root, "bidoof", "none", "minecraft:stick", 0, 1, 1.0f, "cobblemon:oran_berry", 1, 1, 0.025f);
        addRecipe(root, "bibarel", "none", "minecraft:stick", 0, 2, 1.0f, "cobblemon:oran_berry", 1, 1, 0.05f);

        addRecipe(root, "kricketot", "none", "cobblemon:coba_berry", 1, 1, 0.025f);
        addRecipe(root, "kricketune", "none", "cobblemon:coba_berry", 1, 1, 0.05f);

        addRecipe(root, "shinx", "none", "cobblemon:quick_claw", 1, 1, 0.025f, "cobblemon:shuca_berry", 1, 1, 0.025f);
        addRecipe(root, "luxio", "none", "cobblemon:quick_claw", 1, 1, 0.05f, "cobblemon:shuca_berry", 1, 1, 0.05f);
        addRecipe(root, "luxray", "none", "cobblemon:quick_claw", 1, 1, 0.10f, "cobblemon:shuca_berry", 1, 1, 0.10f);

        addRecipe(root, "budew", "none", "cobblemon:poison_barb", 1, 1, 0.025f, "cobblemon:absorb_bulb", 1, 1, 0.025f, "cobblemon:pecha_berry", 1, 1, 0.025f);
        addRecipe(root, "roserade", "none", "minecraft:rose_bush", 1, 2, 1.0f, "cobblemon:poison_barb", 1, 1, 0.10f, "cobblemon:absorb_bulb", 1, 1, 0.10f, "cobblemon:pecha_berry", 1, 1, 0.10f);

        addRecipe(root, "cranidos", "none", "minecraft:bone", 0, 1, 1.0f);
        addRecipe(root, "rampardos", "none", "minecraft:bone", 0, 2, 1.0f);
        addRecipe(root, "shieldon", "none", "minecraft:bone", 0, 1, 1.0f);
        addRecipe(root, "bastiodon", "none", "minecraft:bone", 0, 2, 1.0f);

        addRecipe(root, "combee", "minecraft:glass_bottle", "minecraft:honey_bottle", 0, 1, 1.0f);
        addRecipe(root, "combee", "minecraft:shears", "minecraft:honeycomb", 1, 1, 0.025f);
        addRecipe(root, "combee", "none", "cobblemon:poison_barb", 1, 1, 0.025f, "cobblemon:charti_berry", 1, 1, 0.025f);

        addRecipe(root, "pachirisu", "none", "cobblemon:cheri_berry", 1, 1, 0.05f);
        addRecipe(root, "buizel", "none", "minecraft:salmon", 0, 1, 1.0f, "cobblemon:wacan_berry", 1, 1, 0.025f);
        addRecipe(root, "floatzel", "none", "minecraft:salmon", 0, 2, 1.0f, "cobblemon:wacan_berry", 1, 1, 0.05f);

        addRecipe(root, "shellos", "none", "minecraft:clay_ball", 0, 1, 1.0f, "cobblemon:rindo_berry", 1, 1, 0.025f);
        addRecipe(root, "gastrodon", "none", "minecraft:clay_ball", 0, 2, 1.0f, "cobblemon:rindo_berry", 1, 1, 0.05f);
        addRecipe(root, "ambipom", "none", "cobblemon:nanab_berry", 1, 1, 0.05f);

        addRecipe(root, "drifloon", "none", "minecraft:phantom_membrane", 1, 1, 0.025f, "cobblemon:kasib_berry", 1, 1, 0.025f, "minecraft:air_balloon", 1, 1, 0.025f);
        addRecipe(root, "drifblim", "none", "minecraft:phantom_membrane", 1, 1, 0.05f, "cobblemon:kasib_berry", 1, 1, 0.05f, "minecraft:air_balloon", 1, 1, 0.05f);

        addRecipe(root, "buneary", "minecraft:shears", "minecraft:rabbit", 1, 1, 1.0f, "minecraft:rabbit_hide", 0, 1, 1.0f, "minecraft:rabbit_foot", 1, 1, 0.025f, "minecraft:carrot", 1, 1, 0.025f, "cobblemon:chople_berry", 1, 1, 0.025f);
        addRecipe(root, "lopunny", "minecraft:shears", "minecraft:rabbit", 1, 1, 1.0f, "minecraft:rabbit_hide", 0, 2, 1.0f, "minecraft:rabbit_foot", 1, 1, 0.05f, "minecraft:carrot", 1, 1, 0.05f, "cobblemon:chople_berry", 1, 1, 0.05f);

        addRecipe(root, "mismagius", "none", "minecraft:phantom_membrane", 1, 1, 0.05f, "cobblemon:spell_tag", 1, 1, 0.05f, "cobblemon:kasib_berry", 1, 1, 0.05f);
        addRecipe(root, "honchkrow", "minecraft:shears", "minecraft:feather", 0, 3, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:bluk_berry", 1, 1, 0.05f);
        addRecipe(root, "chingling", "none", "cobblemon:colbur_berry", 1, 1, 0.025f, "minecraft:soothe_bell", 1, 1, 0.025f, "cobblemon:cleanse_tag", 1, 1, 0.025f);

        addRecipe(root, "bonsly", "none", "minecraft:hard_stone", 1, 1, 0.025f, "cobblemon:passho_berry", 1, 1, 0.025f);
        addRecipe(root, "mimejr", "none", "cobblemon:light_clay", 1, 1, 0.025f, "cobblemon:babiri_berry", 1, 1, 0.025f);
        addRecipe(root, "happiny", "none", "cobblemon:oval_stone", 1, 1, 0.25f);

        addRecipe(root, "chatot", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:persim_berry", 1, 1, 0.05f);
        addRecipe(root, "spiritomb", "none", "cobblemon:spell_tag", 1, 1, 0.05f, "minecraft:smoke_ball", 1, 1, 0.05f);

        addRecipe(root, "gible", "none", "cobblemon:dragon_fang", 1, 1, 0.025f, "cobblemon:haban_berry", 1, 1, 0.025f);
        addRecipe(root, "gabite", "none", "cobblemon:dragon_fang", 1, 1, 0.05f, "cobblemon:haban_berry", 1, 1, 0.05f);
        addRecipe(root, "garchomp", "none", "cobblemon:dragon_fang", 1, 1, 0.10f, "cobblemon:haban_berry", 1, 1, 0.10f);

        addRecipe(root, "munchlax", "none", "minecraft:apple", 0, 3, 1.0f, "minecraft:leftovers", 1, 1, 0.025f, "cobblemon:chesto_berry", 1, 1, 0.025f);
        addRecipe(root, "riolu", "none", "cobblemon:chople_berry", 1, 1, 0.025f, "minecraft:expert_belt", 1, 1, 0.025f);
        addRecipe(root, "lucario", "none", "cobblemon:chople_berry", 1, 1, 0.05f, "minecraft:expert_belt", 1, 1, 0.05f);

        addRecipe(root, "hippopotas", "none", "minecraft:sand", 0, 1, 1.0f, "minecraft:soft_sand", 1, 1, 0.025f, "cobblemon:chesto_berry", 1, 1, 0.025f);
        addRecipe(root, "hippowdon", "none", "minecraft:sand", 0, 2, 1.0f, "minecraft:soft_sand", 1, 1, 0.05f, "cobblemon:chesto_berry", 1, 1, 0.05f);

        addRecipe(root, "carnivine", "none", "cobblemon:miracle_seed", 1, 1, 0.05f, "cobblemon:big_root", 1, 1, 0.05f, "cobblemon:kebia_berry", 1, 1, 0.05f);
        addRecipe(root, "finneon", "none", "minecraft:cod", 1, 1, 1.0f, "minecraft:bone_meal", 1, 1, 0.05f, "cobblemon:rindo_berry", 1, 1, 0.025f, "cobblemon:prism_scale", 1, 1, 0.025f);
        addRecipe(root, "lumineon", "none", "minecraft:cod", 1, 1, 1.0f, "minecraft:bone_meal", 1, 1, 0.10f, "cobblemon:rindo_berry", 1, 1, 0.05f, "cobblemon:prism_scale", 1, 1, 0.05f);
        addRecipe(root, "mantyke", "none", "minecraft:cod", 0, 1, 1.0f, "cobblemon:wacan_berry", 1, 1, 0.025f);

        addRecipe(root, "weavile", "none", "cobblemon:razor_claw", 1, 1, 0.05f, "cobblemon:quick_claw", 1, 1, 0.05f, "cobblemon:chople_berry", 1, 1, 0.05f);
        addRecipe(root, "magnezone", "none", "minecraft:raw_iron", 0, 3, 1.0f, "cobblemon:magnet", 1, 1, 0.10f, "cobblemon:metal_coat", 1, 1, 0.10f, "cobblemon:shuca_berry", 1, 1, 0.10f);
        addRecipe(root, "lickilicky", "none", "minecraft:slime_ball", 0, 2, 1.0f, "cobblemon:oran_berry", 1, 1, 0.05f);
        addRecipe(root, "rhyperior", "none", "cobblemon:protector", 1, 1, 0.10f, "cobblemon:passho_berry", 1, 1, 0.10f);
        addRecipe(root, "tangrowth", "none", "minecraft:vine", 0, 2, 1.0f, "cobblemon:big_root", 1, 1, 0.05f);
        addRecipe(root, "electivire", "none", "minecraft:redstone", 0, 3, 1.0f, "cobblemon:electirizer", 1, 1, 0.10f, "cobblemon:cheri_berry", 1, 1, 0.10f);
        addRecipe(root, "magmortar", "none", "minecraft:blaze_powder", 1, 1, 0.10f, "cobblemon:magmarizer", 1, 1, 0.10f, "cobblemon:rawst_berry", 1, 1, 0.10f);

        addRecipe(root, "yanmega", "none", "cobblemon:charti_berry", 1, 1, 0.05f);
        addRecipe(root, "leafeon", "none", "minecraft:leaf_stone", 1, 1, 0.25f, "cobblemon:miracle_seed", 1, 1, 0.10f);
        addRecipe(root, "glaceon", "none", "minecraft:ice_stone", 1, 1, 0.25f, "cobblemon:never_melt_ice", 1, 1, 0.10f);
        addRecipe(root, "gliscor", "none", "minecraft:phantom_membrane", 1, 1, 0.05f, "cobblemon:razor_fang", 1, 1, 0.05f, "cobblemon:poison_barb", 1, 1, 0.05f, "cobblemon:yache_berry", 1, 1, 0.05f);

        addRecipe(root, "mamoswine", "minecraft:shears", "minecraft:porkchop", 2, 3, 1.0f, "minecraft:brown_wool", 2, 3, 1.0f, "cobblemon:never_melt_ice", 1, 1, 0.10f, "cobblemon:aspear_berry", 1, 1, 0.10f);
        addRecipe(root, "porygonz", "none", "cobblemon:dubious_disc", 1, 1, 0.25f);
        addRecipe(root, "gallade", "none", "minecraft:ender_pearl", 0, 3, 1.0f, "minecraft:dawn_stone", 1, 1, 0.10f, "cobblemon:twisted_spoon", 1, 1, 0.10f, "cobblemon:kasib_berry", 1, 1, 0.10f);
        addRecipe(root, "probopass", "none", "minecraft:flint", 0, 3, 1.0f, "cobblemon:hard_stone", 1, 1, 0.05f, "cobblemon:magnet", 1, 1, 0.05f, "cobblemon:chople_berry", 1, 1, 0.05f);
        addRecipe(root, "dusknoir", "none", "minecraft:phantom_membrane", 1, 1, 0.10f, "cobblemon:reaper_cloth", 1, 1, 0.10f, "cobblemon:kasib_berry", 1, 1, 0.10f);

        //gen5
        addRecipe(root, "snivy", "none", "cobblemon:miracle_seed", 1, 1, 0.05f);
        addRecipe(root, "servine", "none", "cobblemon:miracle_seed", 1, 1, 0.10f);
        addRecipe(root, "serperior", "none", "cobblemon:miracle_seed", 1, 1, 0.25f);

        addRecipe(root, "tepig", "none", "minecraft:porkchop", 1, 2, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.05f);
        addRecipe(root, "pignite", "none", "minecraft:porkchop", 1, 3, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.10f);
        addRecipe(root, "emboar", "none", "minecraft:porkchop", 2, 4, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.25f);

        addRecipe(root, "oshawott", "none", "cobblemon:mystic_water", 1, 1, 0.05f, "cobblemon:shell_bell", 1, 1, 0.05f);
        addRecipe(root, "dewott", "none", "cobblemon:mystic_water", 1, 1, 0.10f, "cobblemon:shell_bell", 1, 1, 0.10f);
        addRecipe(root, "samurott", "none", "cobblemon:mystic_water", 1, 1, 0.25f, "cobblemon:shell_bell", 1, 1, 0.25f);

        addRecipe(root, "patrat", "none", "cobblemon:babiri_berry", 1, 1, 0.025f);
        addRecipe(root, "watchog", "none", "cobblemon:babiri_berry", 1, 1, 0.05f);

        addRecipe(root, "lillipup", "none", "minecraft:bone", 0, 1, 1.0f, "minecraft:blue_wool", 1, 1, 1.0f, "cobblemon:chilan_berry", 1, 1, 0.025f);
        addRecipe(root, "herdier", "none", "minecraft:bone", 0, 2, 1.0f, "minecraft:blue_wool", 1, 2, 1.0f, "cobblemon:chilan_berry", 1, 1, 0.05f);
        addRecipe(root, "stoutland", "none", "minecraft:bone", 0, 3, 1.0f, "minecraft:blue_wool", 2, 3, 1.0f, "cobblemon:chilan_berry", 1, 1, 0.10f);

        addRecipe(root, "purrloin", "none", "minecraft:rotten_flesh", 0, 1, 1.0f, "cobblemon:bluk_berry", 1, 1, 0.025f);
        addRecipe(root, "liepard", "none", "minecraft:rotten_flesh", 0, 2, 1.0f, "cobblemon:bluk_berry", 1, 1, 0.05f);

        addRecipe(root, "pidove", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:razz_berry", 1, 1, 0.025f);
        addRecipe(root, "tranquill", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.025f, "cobblemon:razz_berry", 1, 1, 0.05f);
        addRecipe(root, "unfezant", "minecraft:shears", "minecraft:feather", 0, 3, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.05f, "cobblemon:razz_berry", 1, 1, 0.10f);

        addRecipe(root, "blitzle", "none", "minecraft:leather", 0, 1, 1.0f, "cobblemon:cheri_berry", 1, 1, 0.025f);
        addRecipe(root, "zebstrika", "none", "minecraft:leather", 0, 2, 1.0f, "cobblemon:cheri_berry", 1, 1, 0.05f);

        addRecipe(root, "gigalith", "none", "minecraft:raw_copper", 0, 3, 1.0f, "minecraft:hard_stone", 1, 1, 0.10f, "cobblemon:everstone", 1, 1, 0.10f, "cobblemon:black_augurite", 1, 1, 0.10f);
        addRecipe(root, "woobat", "none", "minecraft:light_blue_wool", 1, 1, 1.0f, "cobblemon:colbur_berry", 1, 1, 0.025f);
        addRecipe(root, "swoobat", "none", "minecraft:light_blue_wool", 1, 2, 1.0f, "cobblemon:colbur_berry", 1, 1, 0.05f);

        addRecipe(root, "timburr", "none", "cobblemon:muscle_band", 1, 1, 0.025f, "cobblemon:razz_berry", 1, 1, 0.025f);
        addRecipe(root, "gurdurr", "none", "cobblemon:muscle_band", 1, 1, 0.05f, "cobblemon:razz_berry", 1, 1, 0.05f);
        addRecipe(root, "conkeldurr", "none", "cobblemon:muscle_band", 1, 1, 0.10f, "cobblemon:razz_berry", 1, 1, 0.10f);

        addRecipe(root, "venipede", "none", "cobblemon:poison_barb", 1, 1, 0.025f, "cobblemon:pecha_berry", 1, 1, 0.025f);
        addRecipe(root, "whirlipede", "none", "cobblemon:poison_barb", 1, 1, 0.05f, "cobblemon:pecha_berry", 1, 1, 0.05f);
        addRecipe(root, "scolipede", "none", "cobblemon:poison_barb", 1, 1, 0.10f, "cobblemon:pecha_berry", 1, 1, 0.10f);

        addRecipe(root, "cottonee", "minecraft:shears", "minecraft:white_wool", 1, 1, 1.0f, "cobblemon:kebia_berry", 1, 1, 0.025f, "cobblemon:absorb_bulb", 1, 1, 0.025f);
        addRecipe(root, "whimsicott", "minecraft:shears", "minecraft:white_wool", 1, 2, 1.0f, "cobblemon:kebia_berry", 1, 1, 0.05f, "cobblemon:absorb_bulb", 1, 1, 0.05f);

        addRecipe(root, "petilil", "none", "minecraft:revival_herb", 1, 1, 0.025f, "cobblemon:persim_berry", 1, 1, 0.025f, "cobblemon:absorb_bulb", 1, 1, 0.025f);
        addRecipe(root, "lilligant", "none", "minecraft:revival_herb", 1, 1, 0.05f, "cobblemon:persim_berry", 1, 1, 0.05f, "cobblemon:absorb_bulb", 1, 1, 0.05f);

        addRecipe(root, "basculin", "none", "minecraft:salmon", 1, 1, 1.0f, "minecraft:bone_meal", 1, 1, 0.05f, "cobblemon:deep_sea_tooth", 1, 1, 0.05f);
        addRecipe(root, "sandile", "none", "cobblemon:black_glasses", 1, 1, 0.025f, "cobblemon:tanga_berry", 1, 1, 0.025f);
        addRecipe(root, "krokorok", "none", "cobblemon:black_glasses", 1, 1, 0.05f, "cobblemon:tanga_berry", 1, 1, 0.05f);
        addRecipe(root, "krookodile", "none", "cobblemon:black_glasses", 1, 1, 0.10f, "cobblemon:tanga_berry", 1, 1, 0.10f);

        addRecipe(root, "darumaka", "none", "cobblemon:rawst_berry", 1, 1, 0.025f);
        addRecipe(root, "darmanitan", "none", "cobblemon:rawst_berry", 1, 1, 0.05f);
        addRecipe(root, "maractus", "none", "minecraft:cactus", 0, 2, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.05f, "minecraft:sticky_barb", 1, 1, 0.05f);

        addRecipe(root, "dwebble", "none", "minecraft:terracotta", 0, 1, 1.0f, "minecraft:hard_stone", 1, 1, 0.025f);
        addRecipe(root, "crustle", "none", "minecraft:terracotta", 0, 2, 1.0f, "minecraft:hard_stone", 1, 1, 0.05f);

        addRecipe(root, "scraggy", "none", "cobblemon:black_belt", 1, 1, 0.025f, "cobblemon:roseli_berry", 1, 1, 0.025f);
        addRecipe(root, "scrafty", "none", "cobblemon:black_belt", 1, 1, 0.05f, "cobblemon:roseli_berry", 1, 1, 0.05f);

        addRecipe(root, "sigilyph", "none", "minecraft:ender_pearl", 0, 1, 1.0f, "cobblemon:light_clay", 1, 1, 0.05f, "cobblemon:colbur_berry", 1, 1, 0.05f);
        addRecipe(root, "yamask", "none", "cobblemon:spell_tag", 1, 1, 0.025f);
        addRecipe(root, "cofagrigus", "none", "cobblemon:spell_tag", 1, 1, 0.05f);

        addRecipe(root, "tirtouga", "none", "minecraft:turtle_scute", 0, 2, 1.0f);
        addRecipe(root, "carracosta", "none", "minecraft:turtle_scute", 0, 3, 1.0f);
        addRecipe(root, "archen", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f);
        addRecipe(root, "archeops", "minecraft:shears", "minecraft:feather", 0, 3, 1.0f);

        addRecipe(root, "zorua", "none", "minecraft:sweet_berries", 1, 3, 1.0f, "cobblemon:colbur_berry", 1, 1, 0.025f);
        addRecipe(root, "zoroark", "none", "minecraft:sweet_berries", 2, 4, 1.0f, "cobblemon:roseli_berry", 1, 1, 0.05f);

        addRecipe(root, "deerling", "none", "minecraft:leather", 0, 1, 1.0f, "cobblemon:yache_berry", 1, 1, 0.025f);
        addRecipe(root, "sawsbuck", "none", "minecraft:leather", 0, 2, 1.0f, "cobblemon:yache_berry", 1, 1, 0.05f);
        addRecipe(root, "emolga", "none", "cobblemon:yache_berry", 1, 1, 0.05f);

        addRecipe(root, "frillish", "none", "minecraft:glow_ink_sac", 0, 1, 1.0f, "cobblemon:kasib_berry", 1, 1, 0.025f);
        addRecipe(root, "jellicent", "none", "minecraft:glow_ink_sac", 0, 2, 1.0f, "cobblemon:kasib_berry", 1, 1, 0.05f);
        addRecipe(root, "alomomola", "none", "minecraft:cod", 1, 1, 1.0f, "minecraft:bone_meal", 1, 1, 0.05f, "cobblemon:deep_sea_scale", 1, 1, 0.05f);

        addRecipe(root, "joltik", "minecraft:shears", "minecraft:string", 0, 1, 1.0f, "minecraft:spider_eye", 0, 1, 1.0f, "minecraft:rotten_flesh", 0, 1, 1.0f, "cobblemon:nanab_berry", 1, 1, 0.025f);
        addRecipe(root, "galvantula", "minecraft:shears", "minecraft:string", 0, 2, 1.0f, "minecraft:spider_eye", 0, 2, 1.0f, "minecraft:rotten_flesh", 0, 2, 1.0f, "cobblemon:nanab_berry", 1, 1, 0.05f);

        addRecipe(root, "ferroseed", "none", "minecraft:raw_iron", 0, 1, 1.0f, "cobblemon:occa_berry", 1, 1, 0.025f, "minecraft:sticky_barb", 1, 1, 0.025f);
        addRecipe(root, "ferrothorn", "none", "minecraft:raw_iron", 0, 2, 1.0f, "cobblemon:occa_berry", 1, 1, 0.05f, "minecraft:sticky_barb", 1, 1, 0.05f);

        addRecipe(root, "klink", "none", "minecraft:raw_iron", 0, 1, 1.0f);
        addRecipe(root, "klang", "none", "minecraft:raw_iron", 0, 2, 1.0f);
        addRecipe(root, "klinklang", "none", "minecraft:raw_iron", 0, 3, 1.0f);

        addRecipe(root, "elgyem", "none", "minecraft:ender_pearl", 0, 1, 1.0f, "cobblemon:light_clay", 1, 1, 0.025f, "cobblemon:colbur_berry", 1, 1, 0.025f);
        addRecipe(root, "beheeyem", "none", "minecraft:ender_pearl", 0, 2, 1.0f, "cobblemon:light_clay", 1, 1, 0.05f, "cobblemon:colbur_berry", 1, 1, 0.05f);

        addRecipe(root, "litwick", "none", "minecraft:candle", 0, 1, 1.0f, "minecraft:dusk_stone", 1, 1, 0.025f, "cobblemon:rawst_berry", 1, 1, 0.025f);
        addRecipe(root, "lampent", "none", "minecraft:candle", 1, 1, 1.0f, "minecraft:dusk_stone", 1, 1, 0.05f, "cobblemon:rawst_berry", 1, 1, 0.05f);
        addRecipe(root, "chandelure", "none", "minecraft:candle", 1, 3, 1.0f, "minecraft:dusk_stone", 1, 1, 0.10f, "cobblemon:rawst_berry", 1, 1, 0.10f);

        addRecipe(root, "cubchoo", "none", "minecraft:snowball", 0, 1, 1.0f, "minecraft:cod", 0, 1, 1.0f, "cobblemon:aspear_berry", 1, 1, 0.025f);
        addRecipe(root, "beartic", "none", "minecraft:snowball", 0, 2, 1.0f, "minecraft:cod", 0, 2, 1.0f, "cobblemon:aspear_berry", 1, 1, 0.05f);
        addRecipe(root, "cryogonal", "none", "cobblemon:never_melt_ice", 1, 1, 0.05f, "cobblemon:aspear_berry", 1, 1, 0.05f);

        addRecipe(root, "golett", "none", "minecraft:clay_ball", 0, 1, 1.0f, "cobblemon:light_clay", 1, 1, 0.025f, "cobblemon:colbur_berry", 1, 1, 0.025f);
        addRecipe(root, "golurk", "none", "minecraft:clay_ball", 0, 2, 1.0f, "cobblemon:light_clay", 1, 1, 0.05f, "cobblemon:colbur_berry", 1, 1, 0.05f);

        addRecipe(root, "bouffalant", "none", "minecraft:leather", 0, 2, 1.0f, "minecraft:beef", 1, 3, 1.0f, "minecraft:brown_wool", 1, 2, 1.0f, "cobblemon:wepear_berry", 1, 1, 0.05f);
        addRecipe(root, "heatmor", "none", "minecraft:flame_orb", 1, 1, 0.05f, "minecraft:blaze_powder", 1, 1, 0.05f, "cobblemon:rawst_berry", 1, 1, 0.05f);
        addRecipe(root, "durant", "none", "minecraft:raw_iron", 0, 1, 1.0f, "cobblemon:occa_berry", 1, 1, 0.025f);

        addRecipe(root, "deino", "none", "minecraft:dragon_breath", 0, 1, 1.0f, "cobblemon:roseli_berry", 1, 1, 0.025f);
        addRecipe(root, "zweilous", "none", "minecraft:dragon_breath", 0, 2, 1.0f, "cobblemon:roseli_berry", 1, 1, 0.05f);
        addRecipe(root, "hydreigon", "none", "minecraft:dragon_breath", 0, 3, 1.0f, "cobblemon:roseli_berry", 1, 1, 0.10f);

        addRecipe(root, "larvesta", "minecraft:shears", "minecraft:string", 0, 1, 1.0f, "minecraft:blaze_powder", 1, 1, 0.025f, "cobblemon:silver_powder", 1, 1, 0.025f, "cobblemon:charti_berry", 1, 1, 0.025f);
        addRecipe(root, "volcarona", "none", "minecraft:blaze_powder", 1, 1, 0.05f, "cobblemon:silver_powder", 1, 1, 0.05f, "cobblemon:charti_berry", 1, 1, 0.05f);

        //gen6
        addRecipe(root, "chespin", "none", "cobblemon:miracle_seed", 1, 1, 0.05f);
        addRecipe(root, "quilladin", "none", "cobblemon:miracle_seed", 1, 1, 0.10f);
        addRecipe(root, "chesnaught", "none", "cobblemon:miracle_seed", 1, 1, 0.25f);

        addRecipe(root, "fennekin", "none", "minecraft:blaze_powder", 0, 1, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.05f);
        addRecipe(root, "braixen", "none", "minecraft:stick", 1, 1, 1.0f, "minecraft:blaze_powder", 0, 2, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.10f);
        addRecipe(root, "delphox", "none", "minecraft:stick", 1, 1, 1.0f, "minecraft:blaze_powder", 0, 3, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.25f);

        addRecipe(root, "froakie", "none", "minecraft:pearlescent_froglight", 0, 1, 1.0f, "cobblemon:mystic_water", 1, 1, 0.05f);
        addRecipe(root, "frogadier", "none", "minecraft:pearlescent_froglight", 0, 2, 1.0f, "cobblemon:mystic_water", 1, 1, 0.10f);
        addRecipe(root, "greninja", "none", "minecraft:pearlescent_froglight", 0, 3, 1.0f, "cobblemon:mystic_water", 1, 1, 0.25f);

        addRecipe(root, "bunnelby", "minecraft:shears", "minecraft:rabbit", 1, 1, 1.0f, "minecraft:rabbit_hide", 0, 1, 1.0f, "minecraft:rabbit_foot", 1, 1, 0.025f, "minecraft:carrot", 1, 1, 0.025f, "cobblemon:chilan_berry", 1, 1, 0.025f);
        addRecipe(root, "diggersby", "minecraft:shears", "minecraft:rabbit", 1, 1, 1.0f, "minecraft:rabbit_hide", 0, 2, 1.0f, "minecraft:rabbit_foot", 1, 1, 0.05f, "minecraft:carrot", 1, 1, 0.05f, "cobblemon:chilan_berry", 1, 1, 0.05f);

        addRecipe(root, "fletchling", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:rawst_berry", 1, 1, 0.025f);
        addRecipe(root, "fletchinder", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.025f, "cobblemon:rawst_berry", 1, 1, 0.05f);
        addRecipe(root, "talonflame", "minecraft:shears", "minecraft:feather", 0, 3, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.05f, "cobblemon:rawst_berry", 1, 1, 0.10f);

        addRecipe(root, "scatterbug", "minecraft:shears", "minecraft:string", 0, 1, 1.0f, "minecraft:bright_powder", 1, 1, 0.025f, "cobblemon:cheri_berry", 1, 1, 0.025f);
        addRecipe(root, "spewpa", "minecraft:shears", "minecraft:string", 0, 2, 1.0f, "minecraft:bright_powder", 1, 1, 0.025f, "cobblemon:cheri_berry", 1, 1, 0.025f);
        addRecipe(root, "vivillon", "none", "minecraft:bright_powder", 1, 1, 0.05f, "cobblemon:cheri_berry", 1, 1, 0.05f);

        addRecipe(root, "flabebe", "none", "minecraft:poppy", 1, 1, 1.0f, "cobblemon:kebia_berry", 1, 1, 0.025f);
        addRecipe(root, "floette", "none", "minecraft:poppy", 1, 1, 1.0f, "cobblemon:kebia_berry", 1, 1, 0.05f);
        addRecipe(root, "florges", "none", "minecraft:poppy", 0, 3, 1.0f, "cobblemon:kebia_berry", 1, 1, 0.10f);

        addRecipe(root, "honedge", "none", "minecraft:iron_nugget", 0, 1, 1.0f, "minecraft:iron_sword", 1, 1, 0.025f, "minecraft:dusk_stone", 1, 1, 0.025f);
        addRecipe(root, "doublade", "none", "minecraft:iron_nugget", 0, 2, 1.0f, "minecraft:iron_sword", 1, 1, 0.05f, "minecraft:dusk_stone", 1, 1, 0.05f);
        addRecipe(root, "aegislash", "none", "minecraft:iron_nugget", 0, 3, 1.0f, "minecraft:iron_sword", 1, 1, 0.10f, "minecraft:dusk_stone", 1, 1, 0.10f, "cobblemon:ability_shield", 1, 1, 0.10f);

        addRecipe(root, "skrelp", "none", "minecraft:kelp", 0, 1, 1.0f, "cobblemon:pecha_berry", 1, 1, 0.025f);
        addRecipe(root, "dragalge", "none", "minecraft:kelp", 0, 2, 1.0f, "cobblemon:pecha_berry", 1, 1, 0.05f);

        addRecipe(root, "tyrunt", "none", "cobblemon:razor_fang", 1, 1, 0.05f);
        addRecipe(root, "tyrantrum", "none", "cobblemon:razor_fang", 1, 1, 0.10f);
        addRecipe(root, "amaura", "none", "cobblemon:never_melt_ice", 1, 1, 0.05f);
        addRecipe(root, "aurorus", "none", "cobblemon:never_melt_ice", 1, 1, 0.10f);

        addRecipe(root, "sylveon", "none", "minecraft:shiny_stone", 1, 1, 0.25f, "cobblemon:fairy_feather", 1, 1, 0.10f);
        addRecipe(root, "carbink", "none", "minecraft:diamond", 1, 1, 0.05f, "cobblemon:babiri_berry", 1, 1, 0.05f, "minecraft:float_stone", 1, 1, 0.05f);

        addRecipe(root, "goomy", "none", "minecraft:slime_ball", 0, 1, 1.0f, "cobblemon:haban_berry", 1, 1, 0.025f);
        addRecipe(root, "sliggoo", "none", "minecraft:slime_ball", 0, 2, 1.0f, "cobblemon:haban_berry", 1, 1, 0.05f);
        addRecipe(root, "goodra", "none", "minecraft:slime_ball", 0, 3, 1.0f, "cobblemon:haban_berry", 1, 1, 0.10f);

        addRecipe(root, "klefki", "none", "minecraft:name_tag", 1, 1, 0.05f);
        addRecipe(root, "phantump", "none", "minecraft:jack_o_lantern", 0, 1, 1.0f, "cobblemon:spell_tag", 1, 1, 0.025f, "cobblemon:rawst_berry", 1, 1, 0.025f);
        addRecipe(root, "trevenant", "none", "minecraft:jack_o_lantern", 1, 1, 1.0f, "cobblemon:spell_tag", 1, 1, 0.05f, "cobblemon:rawst_berry", 1, 1, 0.05f);

        addRecipe(root, "pumpkaboo", "none", "minecraft:pumpkin_seeds", 0, 1, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.025f, "cobblemon:kasib_berry", 1, 1, 0.025f);
        addRecipe(root, "gourgeist", "none", "minecraft:pumpkin_seeds", 0, 2, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.05f, "cobblemon:kasib_berry", 1, 1, 0.05f);

        addRecipe(root, "bergmite", "none", "cobblemon:never_melt_ice", 1, 1, 0.025f, "cobblemon:aspear_berry", 1, 1, 0.025f);
        addRecipe(root, "avalugg", "none", "cobblemon:never_melt_ice", 1, 1, 0.05f, "cobblemon:aspear_berry", 1, 1, 0.05f);


        //gen7 i should of left it blank my fingers hurt
        addRecipe(root, "rowlet", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.05f);
        addRecipe(root, "dartrix", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.10f);
        addRecipe(root, "decidueye", "minecraft:shears", "minecraft:feather", 0, 3, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.25f);

        addRecipe(root, "litten", "none", "minecraft:blaze_powder", 0, 1, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.05f);
        addRecipe(root, "torracat", "none", "minecraft:blaze_powder", 0, 2, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.10f);
        addRecipe(root, "incineroar", "none", "minecraft:blaze_powder", 0, 3, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.25f);

        addRecipe(root, "popplio", "none", "cobblemon:mystic_water", 1, 1, 0.05f);
        addRecipe(root, "brionne", "none", "cobblemon:mystic_water", 1, 1, 0.10f);
        addRecipe(root, "primarina", "none", "cobblemon:mystic_water", 1, 1, 0.25f);

        addRecipe(root, "pikipek", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:oran_berry", 1, 1, 0.025f);
        addRecipe(root, "trumbeak", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.025f, "cobblemon:oran_berry", 1, 1, 0.05f);
        addRecipe(root, "toucannon", "minecraft:shears", "minecraft:feather", 0, 3, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.05f, "cobblemon:oran_berry", 1, 1, 0.10f);

        addRecipe(root, "yungoos", "none", "minecraft:rotten_flesh", 0, 1, 1.0f, "cobblemon:pecha_berry", 1, 1, 0.025f);
        addRecipe(root, "gumshoos", "none", "minecraft:rotten_flesh", 0, 2, 1.0f, "cobblemon:pecha_berry", 1, 1, 0.05f);

        addRecipe(root, "grubbin", "minecraft:shears", "minecraft:string", 0, 1, 1.0f, "cobblemon:cheri_berry", 1, 1, 0.025f);
        addRecipe(root, "charjabug", "none", "minecraft:redstone", 0, 1, 1.0f, "minecraft:cell_battery", 1, 1, 0.025f, "cobblemon:cheri_berry", 1, 1, 0.025f);
        addRecipe(root, "vikavolt", "none", "minecraft:redstone", 0, 2, 1.0f, "minecraft:cell_battery", 1, 1, 0.05f, "cobblemon:cheri_berry", 1, 1, 0.05f);

        addRecipe(root, "cutiefly", "none", "minecraft:honey_bottle", 1, 1, 0.025f, "cobblemon:silver_powder", 1, 1, 0.025f, "cobblemon:honey_clump", 1, 1, 0.025f);
        addRecipe(root, "ribombee", "none", "minecraft:honey_bottle", 1, 1, 0.05f, "cobblemon:silver_powder", 1, 1, 0.05f, "cobblemon:honey_clump", 1, 1, 0.05f);

        addRecipe(root, "rockruff", "none", "minecraft:bone", 0, 1, 1.0f, "cobblemon:hard_stone", 1, 1, 0.025f);
        addRecipe(root, "lycanroc", "none", "minecraft:bone", 0, 2, 1.0f, "cobblemon:hard_stone", 1, 1, 0.05f);

        addRecipe(root, "wishiwashi", "none", "minecraft:cod", 1, 1, 1.0f, "minecraft:bone_meal", 1, 1, 0.05f);
        addRecipe(root, "mareanie", "none", "minecraft:prismarine_shard", 0, 1, 1.0f, "cobblemon:poison_barb", 1, 1, 0.025f);
        addRecipe(root, "toxapex", "none", "minecraft:prismarine_shard", 0, 2, 1.0f, "cobblemon:poison_barb", 1, 1, 0.05f);

        addRecipe(root, "mudbray", "none", "minecraft:clay_ball", 0, 1, 1.0f, "cobblemon:persim_berry", 1, 1, 0.025f);
        addRecipe(root, "mudsdale", "none", "minecraft:clay_ball", 0, 2, 1.0f, "cobblemon:persim_berry", 1, 1, 0.05f);

        addRecipe(root, "dewpider", "minecraft:shears", "minecraft:string", 0, 1, 1.0f, "cobblemon:mystic_water", 1, 1, 0.025f);
        addRecipe(root, "araquanid", "minecraft:shears", "minecraft:string", 0, 2, 1.0f, "cobblemon:mystic_water", 1, 1, 0.05f);

        addRecipe(root, "fomantis", "none", "cobblemon:miracle_seed", 1, 1, 0.025f);
        addRecipe(root, "lurantis", "none", "cobblemon:miracle_seed", 1, 1, 0.05f);

        addRecipe(root, "morelull", "none", "minecraft:brown_mushroom", 0, 1, 1.0f, "cobblemon:kebia_berry", 1, 1, 0.025f);
        addRecipe(root, "shiinotic", "none", "minecraft:brown_mushroom", 0, 2, 1.0f, "cobblemon:kebia_berry", 1, 1, 0.05f);

        addRecipe(root, "salandit", "none", "minecraft:blaze_powder", 0, 1, 1.0f, "cobblemon:poison_barb", 1, 1, 0.025f);
        addRecipe(root, "salazzle", "none", "minecraft:blaze_powder", 0, 2, 1.0f, "cobblemon:poison_barb", 1, 1, 0.05f);

        addRecipe(root, "stufful", "none", "minecraft:leather", 0, 1, 1.0f, "cobblemon:chople_berry", 1, 1, 0.025f);
        addRecipe(root, "bewear", "none", "minecraft:leather", 0, 2, 1.0f, "cobblemon:chople_berry", 1, 1, 0.05f);

        addRecipe(root, "bounsweet", "none", "cobblemon:tanga_berry", 1, 1, 0.025f);
        addRecipe(root, "steenee", "none", "cobblemon:tanga_berry", 1, 1, 0.05f);
        addRecipe(root, "tsareena", "none", "cobblemon:tanga_berry", 1, 1, 0.10f);

        addRecipe(root, "wimpod", "none", "minecraft:cod", 0, 1, 1.0f, "cobblemon:quick_claw", 1, 1, 0.025f);
        addRecipe(root, "golisopod", "none", "minecraft:cod", 0, 2, 1.0f, "cobblemon:quick_claw", 1, 1, 0.05f);

        addRecipe(root, "sandygast", "none", "minecraft:sand", 0, 1, 1.0f, "cobblemon:spell_tag", 1, 1, 0.025f);
        addRecipe(root, "palossand", "none", "minecraft:sand", 0, 2, 1.0f, "cobblemon:spell_tag", 1, 1, 0.05f);

        addRecipe(root, "pyukumuku", "none", "minecraft:slime_ball", 0, 1, 1.0f);
        addRecipe(root, "komala", "none", "minecraft:log", 0, 1, 1.0f, "cobblemon:chesto_berry", 1, 1, 0.05f);

        addRecipe(root, "togedemaru", "none", "minecraft:iron_nugget", 0, 1, 1.0f, "cobblemon:cheri_berry", 1, 1, 0.05f);
        addRecipe(root, "mimikyu", "none", "cobblemon:spell_tag", 1, 1, 0.05f, "cobblemon:roseli_berry", 1, 1, 0.05f);

        addRecipe(root, "drampa", "none", "minecraft:dragon_breath", 0, 1, 1.0f, "cobblemon:persim_berry", 1, 1, 0.05f);
        addRecipe(root, "jangmoo", "none", "minecraft:bone", 0, 1, 1.0f, "cobblemon:razor_claw", 1, 1, 0.025f);
        addRecipe(root, "hakamoo", "none", "minecraft:bone", 0, 2, 1.0f, "cobblemon:razor_claw", 1, 1, 0.05f);
        addRecipe(root, "kommoo", "none", "minecraft:bone", 0, 3, 1.0f, "cobblemon:razor_claw", 1, 1, 0.10f);

        //gen8 cries in json
        addRecipe(root, "grookey", "none", "minecraft:stick", 0, 1, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.05f);
        addRecipe(root, "thackey", "none", "minecraft:stick", 0, 2, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.10f);
        addRecipe(root, "rillaboom", "none", "minecraft:stick", 0, 3, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.25f);

        addRecipe(root, "scorbunny", "none", "minecraft:blaze_powder", 0, 1, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.05f);
        addRecipe(root, "raboot", "none", "minecraft:blaze_powder", 0, 2, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.10f);
        addRecipe(root, "cinderace", "none", "minecraft:blaze_powder", 0, 3, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.25f);

        addRecipe(root, "sobble", "none", "cobblemon:mystic_water", 1, 1, 0.05f);
        addRecipe(root, "drizzile", "none", "cobblemon:mystic_water", 1, 1, 0.10f);
        addRecipe(root, "inteleon", "none", "cobblemon:mystic_water", 1, 1, 0.25f);

        addRecipe(root, "skwovet", "none", "minecraft:sweet_berries", 0, 2, 1.0f, "cobblemon:oran_berry", 1, 1, 0.025f);
        addRecipe(root, "greedent", "none", "minecraft:sweet_berries", 1, 3, 1.0f, "cobblemon:oran_berry", 1, 1, 0.05f);

        addRecipe(root, "rookidee", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:charti_berry", 1, 1, 0.025f);
        addRecipe(root, "corvisquire", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.025f, "cobblemon:charti_berry", 1, 1, 0.05f);
        addRecipe(root, "corviknight", "minecraft:shears", "minecraft:feather", 0, 3, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.05f, "cobblemon:metal_coat", 1, 1, 0.05f, "cobblemon:charti_berry", 1, 1, 0.10f);

        addRecipe(root, "blipbug", "minecraft:shears", "minecraft:string", 0, 1, 1.0f, "cobblemon:pinap_berry", 1, 1, 0.025f);
        addRecipe(root, "dottler", "minecraft:shears", "minecraft:string", 0, 2, 1.0f, "cobblemon:pinap_berry", 1, 1, 0.05f);
        addRecipe(root, "orbeetle", "none", "minecraft:ender_pearl", 0, 1, 1.0f, "cobblemon:pinap_berry", 1, 1, 0.10f);

        addRecipe(root, "nickit", "none", "minecraft:rotten_flesh", 0, 1, 1.0f, "cobblemon:bluk_berry", 1, 1, 0.025f);
        addRecipe(root, "thievul", "none", "minecraft:rotten_flesh", 0, 2, 1.0f, "cobblemon:bluk_berry", 1, 1, 0.05f);

        addRecipe(root, "gossifleur", "none", "cobblemon:miracle_seed", 1, 1, 0.025f);
        addRecipe(root, "eldegoss", "minecraft:shears", "minecraft:white_wool", 1, 1, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.05f);

        addRecipe(root, "wooloo", "minecraft:shears", "minecraft:mutton", 1, 1, 1.0f, "minecraft:white_wool", 1, 1, 1.0f);
        addRecipe(root, "dubwool", "minecraft:shears", "minecraft:mutton", 1, 2, 1.0f, "minecraft:white_wool", 1, 2, 1.0f);

        addRecipe(root, "yamper", "none", "minecraft:leather", 0, 1, 1.0f, "cobblemon:cheri_berry", 1, 1, 0.025f);
        addRecipe(root, "boltund", "none", "minecraft:leather", 0, 2, 1.0f, "cobblemon:cheri_berry", 1, 1, 0.05f);

        addRecipe(root, "rolycoly", "none", "minecraft:coal", 1, 1, 1.0f);
        addRecipe(root, "carkol", "none", "minecraft:coal", 1, 2, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.05f);
        addRecipe(root, "coalossal", "none", "minecraft:coal", 1, 3, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.10f);

        addRecipe(root, "applin", "none", "minecraft:apple", 1, 1, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.025f);
        addRecipe(root, "flapple", "none", "minecraft:apple", 1, 2, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.05f);
        addRecipe(root, "appletun", "none", "minecraft:apple", 1, 2, 1.0f, "cobblemon:miracle_seed", 1, 1, 0.05f);

        addRecipe(root, "silicobra", "none", "minecraft:sand", 0, 1, 1.0f, "cobblemon:soft_sand", 1, 1, 0.025f);
        addRecipe(root, "sandaconda", "none", "minecraft:sand", 0, 2, 1.0f, "cobblemon:soft_sand", 1, 1, 0.05f);

        addRecipe(root, "cramorant", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:cod", 0, 1, 1.0f);
        addRecipe(root, "arrokuda", "none", "minecraft:salmon", 1, 1, 1.0f, "minecraft:bone_meal", 1, 1, 0.05f);
        addRecipe(root, "barraskewda", "none", "minecraft:salmon", 1, 1, 1.0f, "minecraft:bone_meal", 1, 1, 0.10f);

        addRecipe(root, "toxel", "none", "cobblemon:poison_barb", 1, 1, 0.025f);
        addRecipe(root, "toxtricity", "none", "cobblemon:poison_barb", 1, 1, 0.05f, "minecraft:redstone", 0, 2, 1.0f);

        addRecipe(root, "sizzlipede", "none", "minecraft:blaze_powder", 0, 1, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.025f);
        addRecipe(root, "centiskorch", "none", "minecraft:blaze_powder", 1, 2, 1.0f, "cobblemon:charcoal_stick", 1, 1, 0.05f);

        addRecipe(root, "clobbopus", "none", "minecraft:ink_sac", 1, 1, 1.0f);
        addRecipe(root, "grapploct", "none", "minecraft:ink_sac", 1, 2, 1.0f, "cobblemon:black_belt", 1, 1, 0.05f);

        addRecipe(root, "sinistea", "none", "minecraft:clay_ball", 0, 1, 1.0f, "cobblemon:kasib_berry", 1, 1, 0.025f);
        addRecipe(root, "polteageist", "none", "minecraft:clay_ball", 0, 2, 1.0f, "cobblemon:kasib_berry", 1, 1, 0.05f);

        addRecipe(root, "hatenna", "none", "cobblemon:babiri_berry", 1, 1, 0.025f);
        addRecipe(root, "hattrem", "none", "cobblemon:babiri_berry", 1, 1, 0.05f);
        addRecipe(root, "hatterene", "none", "cobblemon:babiri_berry", 1, 1, 0.10f);

        addRecipe(root, "milcery", "none", "minecraft:sugar", 1, 1, 1.0f);
        addRecipe(root, "alcremie", "none", "minecraft:sugar", 1, 2, 1.0f, "cobblemon:sweet_apple", 1, 1, 0.05f);

        addRecipe(root, "pincurchin", "none", "minecraft:ink_sac", 1, 1, 1.0f, "cobblemon:poison_barb", 1, 1, 0.025f);
        addRecipe(root, "snom", "minecraft:shears", "minecraft:string", 0, 1, 1.0f, "cobblemon:never_melt_ice", 1, 1, 0.025f);
        addRecipe(root, "frosmoth", "minecraft:shears", "minecraft:string", 0, 2, 1.0f, "cobblemon:never_melt_ice", 1, 1, 0.05f);

        addRecipe(root, "stonjourner", "none", "minecraft:stone", 1, 2, 1.0f, "cobblemon:hard_stone", 1, 1, 0.05f);
        addRecipe(root, "eiscue", "none", "minecraft:ice", 1, 1, 1.0f, "minecraft:cod", 1, 1, 1.0f);

        addRecipe(root, "dreepy", "none", "minecraft:dragon_breath", 0, 1, 1.0f, "cobblemon:kasib_berry", 1, 1, 0.025f);
        addRecipe(root, "drakloak", "none", "minecraft:dragon_breath", 0, 2, 1.0f, "cobblemon:kasib_berry", 1, 1, 0.05f);
        addRecipe(root, "dragapult", "none", "minecraft:dragon_breath", 0, 3, 1.0f, "cobblemon:kasib_berry", 1, 1, 0.10f);

        addRecipe(root, "wattrel", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:razz_berry", 1, 1, 0.025f);
        addRecipe(root, "kilowattrel", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:sharp_beak", 1, 1, 0.025f, "cobblemon:razz_berry", 1, 1, 0.05f);

        addRecipe(root, "maschiff", "none", "minecraft:bone", 0, 1, 1.0f, "minecraft:rotten_flesh", 0, 1, 1.0f, "cobblemon:tanga_berry", 1, 1, 0.025f);
        addRecipe(root, "mabosstiff", "none", "minecraft:bone", 0, 2, 1.0f, "minecraft:rotten_flesh", 0, 2, 1.0f, "cobblemon:tanga_berry", 1, 1, 0.05f);

        addRecipe(root, "flittle", "minecraft:shears", "minecraft:feather", 0, 1, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:tanga_berry", 1, 1, 0.025f);
        addRecipe(root, "espathra", "minecraft:shears", "minecraft:feather", 0, 2, 1.0f, "minecraft:chicken", 1, 1, 1.0f, "cobblemon:tanga_berry", 1, 1, 0.05f);

        addRecipe(root, "tinkaton", "none", "minecraft:raw_iron", 0, 3, 1.0f, "cobblemon:razz_berry", 1, 1, 0.10f);

        addRecipe(root, "finizen", "none", "minecraft:cod", 0, 1, 1.0f);
        addRecipe(root, "palafin", "none", "minecraft:cod", 0, 2, 1.0f);

        addRecipe(root, "varoom", "none", "minecraft:iron_nugget", 0, 1, 1.0f, "cobblemon:black_sludge", 1, 1, 0.025f, "cobblemon:shuca_berry", 1, 1, 0.025f);
        addRecipe(root, "revavroom", "none", "minecraft:iron_nugget", 0, 2, 1.0f, "cobblemon:black_sludge", 1, 1, 0.05f, "cobblemon:shuca_berry", 1, 1, 0.05f);

        //LONG LIST END

        try (Writer writer = new FileWriter(path.toFile())) {
            GSON.toJson(root, writer);
        }
    }

    private static void addRecipe(JsonObject root, String species, String tool, Object... dropData) {
        JsonArray speciesArray = root.has(species) ? root.getAsJsonArray(species) : new JsonArray();
        JsonObject recipe = new JsonObject();
        recipe.addProperty("tool", tool);
        JsonArray drops = new JsonArray();
        for (int i = 0; i < dropData.length; i += 4) {
            drops.add(createDrop((String) dropData[i], (int) dropData[i + 1], (int) dropData[i + 2], (float) dropData[i + 3]));
        }
        recipe.add("drops", drops);
        speciesArray.add(recipe);
        root.add(species, speciesArray);
    }

    private static JsonObject createRecipeObject(String tool, String item, int min, int max, float chance) {
        JsonObject obj = new JsonObject();
        obj.addProperty("tool", tool);
        JsonArray drops = new JsonArray();
        drops.add(createDrop(item, min, max, chance));
        obj.add("drops", drops);
        return obj;
    }

    private static JsonObject createDrop(String item, int min, int max, float chance) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", item);
        obj.addProperty("min", min);
        obj.addProperty("max", max);
        obj.addProperty("chance", chance);
        return obj;
    }

    public static void clearMemory() {
        RECIPES.clear();
    }

    public static void parseAndAddRecipes(JsonObject root) {
        String currentSpecies = "UNKNOWN";
        try {
            for (String species : root.keySet()) {
                currentSpecies = species;
                List<RancherRecipe> baseList = new ArrayList<>();

                if (species.startsWith("_comment")) continue;
                if (!root.get(species).isJsonArray()) {
                    CobblemonColonies.LOGGER.warn("Skipping " + species + ": Expected a JSON array but found something else.");
                    continue;
                }

                JsonArray recipesArray = root.getAsJsonArray(species);

                for (JsonElement element : recipesArray) {
                    JsonObject obj = element.getAsJsonObject();

                    Item tool = Items.AIR;
                    if (obj.has("tool")) {
                        String toolString = obj.get("tool").getAsString();
                        if (!toolString.equalsIgnoreCase("none")) {
                            tool = BuiltInRegistries.ITEM.get(ResourceLocation.parse(toolString));
                        }
                    }

                    List<Drop> drops = new ArrayList<>();

                    if (obj.has("drops") && obj.get("drops").isJsonArray()) {
                        for (JsonElement d : obj.getAsJsonArray("drops")) {
                            JsonObject dObj = d.getAsJsonObject();

                            if (dObj.has("item")) {
                                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(dObj.get("item").getAsString()));
                                int min = dObj.has("min") ? dObj.get("min").getAsInt() : 1;
                                int max = dObj.has("max") ? dObj.get("max").getAsInt() : 1;
                                float chance = dObj.has("chance") ? dObj.get("chance").getAsFloat() : 1.0f;

                                if (item != Items.AIR) drops.add(new Drop(item, min, max, chance));
                            } else {
                                CobblemonColonies.LOGGER.warn("Skipping a drop for " + species + ": No 'item' defined.");
                            }
                        }
                    }
                    baseList.add(new RancherRecipe(tool, drops));
                }

                List<RancherRecipe> expandedList = new ArrayList<>();
                for (RancherRecipe baseRecipe : baseList) {
                    expandedList.add(baseRecipe);
                    if (baseRecipe.drops().size() > 1) {
                        for (Drop singleDrop : baseRecipe.drops()) {
                            expandedList.add(new RancherRecipe(baseRecipe.tool(), Collections.singletonList(singleDrop)));
                        }
                    }
                }
                String key = species.toLowerCase();
                RECIPES.computeIfAbsent(key, k -> new ArrayList<>()).addAll(expandedList);
            }
        } catch (Exception e) {
            CobblemonColonies.LOGGER.error("Failed to parse Rancher JSON data for species: " + currentSpecies, e);
        }
    }
}