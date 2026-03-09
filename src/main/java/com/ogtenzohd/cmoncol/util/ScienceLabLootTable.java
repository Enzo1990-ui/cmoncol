package com.ogtenzohd.cmoncol.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.*;

public class ScienceLabLootTable {

    public static final Map<String, List<WeightedItem>> LOOT_TABLES = new LinkedHashMap<>();
    public static final List<String> SITE_NAMES = new ArrayList<>();
	
	//why does this wiki https://wiki.cobblemon.com/index.php/Fossil_Dig_Sites exist now i feel like i have to add them all!!!

    static {
		//regretting life choices already
        //Prehistoric Birch Tree
        List<WeightedItem> birch = new ArrayList<>();
        birch.add(new WeightedItem("cobblemon:plume_fossil", 5));
        addUncommon(birch, "cobblemon:heat_rock", "cobblemon:everstone", "cobblemon:charcoal_stick", "cobblemon:peat_block", "cobblemon:skull_fossil", "cobblemon:fossilized_bird", "cobblemon:fossilized_drake");
        addCommon(birch, "cobblemon:big_root", "cobblemon:energy_root", "minecraft:charcoal", "minecraft:raw_gold", "cobblemon:leaf_stone", "cobblemon:mirror_herb");
        register("Prehistoric Birch Tree", birch);

        //Prehistoric Dripstone Oasis
        List<WeightedItem> drip = new ArrayList<>();
        drip.add(new WeightedItem("cobblemon:claw_fossil", 5));
        addUncommon(drip, "cobblemon:damp_rock", "cobblemon:everstone", "cobblemon:prism_scale", "cobblemon:dragon_fang", "cobblemon:dome_fossil", "cobblemon:fossilized_fish", "cobblemon:helix_fossil", "cobblemon:claw_fossil");
        addCommon(drip, "cobblemon:big_root", "cobblemon:energy_root", "cobblemon:water_stone", "minecraft:raw_gold", "cobblemon:power_herb", "cobblemon:mirror_herb", "cobblemon:mental_herb", "cobblemon:white_herb");
        register("Prehistoric Dripstone Oasis", drip);

        //Prehistoric Enhydro Agate
        List<WeightedItem> agate = new ArrayList<>();
        agate.add(new WeightedItem("cobblemon:fossilized_fish", 5));
        addUncommon(agate, "cobblemon:damp_rock", "cobblemon:everstone", "cobblemon:kings_rock", "cobblemon:dome_fossil", "cobblemon:fossilized_fish", "cobblemon:helix_fossil", "cobblemon:claw_fossil");
        addCommon(agate, "cobblemon:big_root", "cobblemon:energy_root", "cobblemon:water_stone", "minecraft:raw_gold", "cobblemon:power_herb", "cobblemon:mirror_herb", "cobblemon:mental_herb", "cobblemon:white_herb");
        register("Prehistoric Enhydro Agate", agate);

        //Prehistoric Eroded Pillar
        List<WeightedItem> pillar = new ArrayList<>();
        pillar.add(new WeightedItem("cobblemon:jaw_fossil", 5));
        addUncommon(pillar, "cobblemon:heat_rock", "cobblemon:everstone", "cobblemon:smooth_rock", "cobblemon:black_augurite", "cobblemon:armor_fossil", "cobblemon:sail_fossil", "cobblemon:helix_fossil", "cobblemon:jaw_fossil");
        addCommon(pillar, "cobblemon:big_root", "cobblemon:energy_root", "cobblemon:sun_stone", "minecraft:raw_gold", "cobblemon:power_herb", "cobblemon:mirror_herb", "cobblemon:mental_herb", "cobblemon:white_herb");
        register("Prehistoric Eroded Pillar", pillar);

        //Prehistoric Frozen Pond
        List<WeightedItem> frozenPond = new ArrayList<>();
        frozenPond.add(new WeightedItem("cobblemon:sail_fossil", 5));
        addUncommon(frozenPond, "cobblemon:icy_rock", "cobblemon:everstone", "cobblemon:never_melt_ice", "cobblemon:old_amber_fossil", "cobblemon:cover_fossil", "cobblemon:fossilized_dino", "cobblemon:sail_fossil");
        addCommon(frozenPond, "cobblemon:big_root", "cobblemon:energy_root", "cobblemon:ice_stone", "minecraft:raw_gold", "cobblemon:power_herb", "cobblemon:mirror_herb", "cobblemon:mental_herb", "cobblemon:white_herb");
        register("Prehistoric Frozen Pond", frozenPond);

        //Prehistoric Frozen Spike
        List<WeightedItem> frozenSpike = new ArrayList<>();
        frozenSpike.add(new WeightedItem("cobblemon:fossilized_dino", 5));
        addUncommon(frozenSpike, "cobblemon:icy_rock", "cobblemon:everstone", "cobblemon:never_melt_ice", "cobblemon:razor_claw", "cobblemon:fossilized_fish", "cobblemon:claw_fossil", "cobblemon:fossilized_dino", "cobblemon:sail_fossil");
        addCommon(frozenSpike, "cobblemon:ice_stone", "minecraft:raw_gold");
        register("Prehistoric Frozen Spike", frozenSpike);

        //Prehistoric Hydrothermal Vents
        List<WeightedItem> vents = new ArrayList<>();
        vents.add(new WeightedItem("cobblemon:dome_fossil", 5));
        addUncommon(vents, "cobblemon:damp_rock", "cobblemon:everstone", "cobblemon:deep_sea_tooth", "cobblemon:deep_sea_scale", "cobblemon:prism_scale", "cobblemon:root_fossil", "cobblemon:cover_fossil", "cobblemon:fossilized_fish", "cobblemon:dome_fossil");
        addCommon(vents, "cobblemon:big_root", "cobblemon:energy_root", "minecraft:raw_gold", "cobblemon:power_herb", "cobblemon:mirror_herb", "cobblemon:mental_herb", "cobblemon:white_herb");
        register("Prehistoric Hydrothermal Vents", vents);

        //Prehistoric Lush Den
        List<WeightedItem> lush = new ArrayList<>();
        lush.add(new WeightedItem("cobblemon:jaw_fossil", 5));
        addUncommon(lush, "cobblemon:damp_rock", "cobblemon:everstone", "cobblemon:peat_block", "cobblemon:dragon_fang", "cobblemon:fairy_feather", "cobblemon:absorb_bulb", "cobblemon:old_amber_fossil", "cobblemon:armor_fossil", "cobblemon:fossilized_dino", "cobblemon:jaw_fossil");
        addCommon(lush, "cobblemon:big_root", "cobblemon:energy_root", "minecraft:raw_gold", "cobblemon:power_herb", "cobblemon:mirror_herb", "cobblemon:mental_herb", "cobblemon:white_herb", "cobblemon:leaf_stone");
        register("Prehistoric Lush Den", lush);

        //Prehistoric Mossy Pond
        List<WeightedItem> mossy = new ArrayList<>();
        mossy.add(new WeightedItem("cobblemon:claw_fossil", 5));
        addUncommon(mossy, "cobblemon:damp_rock", "cobblemon:everstone", "cobblemon:prism_scale", "cobblemon:dragon_fang", "cobblemon:absorb_bulb", "cobblemon:plume_fossil", "cobblemon:skull_fossil", "cobblemon:fossilized_bird", "cobblemon:claw_fossil");
        addCommon(mossy, "cobblemon:big_root", "cobblemon:energy_root", "minecraft:raw_gold", "cobblemon:power_herb", "cobblemon:mirror_herb", "cobblemon:mental_herb", "cobblemon:white_herb", "cobblemon:leaf_stone", "cobblemon:water_stone");
        register("Prehistoric Mossy Pond", mossy);

        //Prehistoric Mud Pit
        List<WeightedItem> mud = new ArrayList<>();
        mud.add(new WeightedItem("cobblemon:skull_fossil", 5));
        addUncommon(mud, "cobblemon:damp_rock", "cobblemon:everstone", "cobblemon:peat_block", "cobblemon:dragon_fang", "cobblemon:absorb_bulb", "cobblemon:jaw_fossil", "cobblemon:armor_fossil", "cobblemon:fossilized_drake", "cobblemon:skull_fossil");
        addCommon(mud, "cobblemon:big_root", "cobblemon:energy_root", "minecraft:raw_gold", "cobblemon:power_herb", "cobblemon:mirror_herb", "cobblemon:mental_herb", "cobblemon:white_herb", "cobblemon:water_stone");
        register("Prehistoric Mud Pit", mud);

        //Prehistoric Oak Tree
        List<WeightedItem> oak = new ArrayList<>();
        oak.add(new WeightedItem("cobblemon:old_amber_fossil", 5));
        addUncommon(oak, "cobblemon:heat_rock", "cobblemon:everstone", "cobblemon:charcoal_stick", "cobblemon:peat_block", "cobblemon:fairy_feather", "cobblemon:skull_fossil", "cobblemon:fossilized_bird", "cobblemon:fossilized_dino", "cobblemon:old_amber_fossil");
        addCommon(oak, "cobblemon:big_root", "cobblemon:energy_root", "minecraft:charcoal", "minecraft:raw_gold", "cobblemon:leaf_stone", "cobblemon:mirror_herb");
        register("Prehistoric Oak Tree", oak);

        //Prehistoric Powdered Deposi
        List<WeightedItem> powder = new ArrayList<>();
        powder.add(new WeightedItem("cobblemon:sail_fossil", 5));
        addUncommon(powder, "cobblemon:icy_rock", "cobblemon:everstone", "cobblemon:never_melt_ice", "cobblemon:armor_fossil", "cobblemon:fossilized_bird", "cobblemon:fossilized_dino", "cobblemon:sail_fossil");
        addCommon(powder, "cobblemon:big_root", "cobblemon:energy_root", "minecraft:raw_gold", "cobblemon:power_herb", "cobblemon:mirror_herb", "cobblemon:mental_herb", "cobblemon:white_herb", "cobblemon:ice_stone");
        register("Prehistoric Powdered Deposit", powder);

        //Prehistoric Preserved Skeleton
        List<WeightedItem> skeleton = new ArrayList<>();
        skeleton.add(new WeightedItem("cobblemon:fossilized_bird", 3));
        skeleton.add(new WeightedItem("cobblemon:fossilized_fish", 3));
        skeleton.add(new WeightedItem("cobblemon:fossilized_drake", 3));
        skeleton.add(new WeightedItem("cobblemon:fossilized_dino", 3));
        addUncommon(skeleton, "cobblemon:icy_rock", "cobblemon:everstone", "cobblemon:never_melt_ice", "cobblemon:prism_scale", "cobblemon:deep_sea_scale", "cobblemon:deep_sea_tooth", "cobblemon:cover_fossil", "cobblemon:claw_fossil", "cobblemon:fossilized_dino", "cobblemon:helix_fossil");
        addCommon(skeleton, "cobblemon:big_root", "cobblemon:energy_root", "minecraft:raw_gold", "cobblemon:ice_stone");
        register("Prehistoric Preserved Skeleton", skeleton);

        //Prehistoric Rooted Pit
        List<WeightedItem> rooted = new ArrayList<>();
        rooted.add(new WeightedItem("cobblemon:root_fossil", 5));
        addUncommon(rooted, "cobblemon:damp_rock", "cobblemon:everstone", "cobblemon:peat_block", "cobblemon:prism_scale", "cobblemon:armor_fossil", "cobblemon:fossilized_drake", "cobblemon:plume_fossil", "cobblemon:root_fossil");
        addCommon(rooted, "cobblemon:big_root", "cobblemon:energy_root", "minecraft:raw_gold", "cobblemon:power_herb", "cobblemon:mirror_herb", "cobblemon:mental_herb", "cobblemon:white_herb", "cobblemon:ice_stone");
        register("Prehistoric Rooted Pit", rooted);

        //Prehistoric Sandy Den
        List<WeightedItem> sandy = new ArrayList<>();
        sandy.add(new WeightedItem("cobblemon:helix_fossil", 5));
        addUncommon(sandy, "cobblemon:heat_rock", "cobblemon:everstone", "cobblemon:smooth_rock", "cobblemon:razor_fang", "cobblemon:black_augurite", "cobblemon:root_fossil", "cobblemon:fossilized_dino", "cobblemon:armor_fossil", "cobblemon:helix_fossil");
        addCommon(sandy, "cobblemon:sun_stone", "minecraft:raw_gold");
        register("Prehistoric Sandy Den", sandy);

        //Prehistoric Spruce Tree
        List<WeightedItem> spruce = new ArrayList<>();
        spruce.add(new WeightedItem("cobblemon:old_amber_fossil", 5));
        addUncommon(spruce, "cobblemon:icy_rock", "cobblemon:everstone", "cobblemon:charcoal_stick", "cobblemon:peat_block", "cobblemon:fairy_feather", "cobblemon:fossilized_bird", "cobblemon:plume_fossil", "cobblemon:sail_fossil", "cobblemon:old_amber_fossil");
        addCommon(spruce, "cobblemon:big_root", "cobblemon:energy_root", "cobblemon:never_melt_ice", "minecraft:raw_gold", "cobblemon:ice_stone", "cobblemon:mirror_herb");
        register("Prehistoric Spruce Tree", spruce);

        //Prehistoric Submerged Impact
        List<WeightedItem> impact = new ArrayList<>();
        impact.add(new WeightedItem("cobblemon:fossilized_fish", 5));
        addUncommon(impact, "cobblemon:damp_rock", "cobblemon:everstone", "cobblemon:prism_scale", "cobblemon:deep_sea_tooth", "cobblemon:deep_sea_scale", "cobblemon:root_fossil", "cobblemon:fossilized_fish", "cobblemon:claw_fossil", "cobblemon:cover_fossil");
        addCommon(impact, "cobblemon:water_stone", "minecraft:raw_gold");
        register("Prehistoric Submerged Impact", impact);

        //Prehistoric Submerged Spike
        List<WeightedItem> subSpike = new ArrayList<>();
        subSpike.add(new WeightedItem("cobblemon:cover_fossil", 5));
        addUncommon(subSpike, "cobblemon:damp_rock", "cobblemon:icy_rock", "cobblemon:everstone", "cobblemon:prism_scale", "cobblemon:deep_sea_tooth", "cobblemon:deep_sea_scale", "cobblemon:root_fossil", "cobblemon:fossilized_fish", "cobblemon:claw_fossil", "cobblemon:cover_fossil");
        addCommon(subSpike, "cobblemon:water_stone", "minecraft:raw_gold");
        register("Prehistoric Submerged Spike", subSpike);

        //Prehistoric Sunscorched Den
        List<WeightedItem> sunDen = new ArrayList<>();
        sunDen.add(new WeightedItem("cobblemon:armor_fossil", 5));
        addUncommon(sunDen, "cobblemon:heat_rock", "cobblemon:smooth_rock", "cobblemon:everstone", "cobblemon:razor_fang", "cobblemon:dome_fossil", "cobblemon:fossilized_drake", "cobblemon:jaw_fossil", "cobblemon:armor_fossil");
        addCommon(sunDen, "cobblemon:big_root", "cobblemon:energy_root", "cobblemon:sun_stone", "minecraft:raw_gold", "cobblemon:power_herb", "cobblemon:mirror_herb", "cobblemon:mental_herb", "cobblemon:white_herb");
        register("Prehistoric Sunscorched Den", sunDen);

        //Prehistoric Sunscorched Remains
        List<WeightedItem> sunRemains = new ArrayList<>();
        sunRemains.add(new WeightedItem("cobblemon:fossilized_drake", 5));
        addUncommon(sunRemains, "cobblemon:heat_rock", "cobblemon:smooth_rock", "cobblemon:everstone", "cobblemon:razor_fang", "cobblemon:root_fossil", "cobblemon:fossilized_drake", "cobblemon:jaw_fossil", "cobblemon:skull_fossil");
        addCommon(sunRemains, "cobblemon:big_root", "cobblemon:energy_root", "cobblemon:sun_stone", "minecraft:raw_gold", "cobblemon:power_herb", "cobblemon:mirror_herb", "cobblemon:mental_herb", "cobblemon:white_herb");
        register("Prehistoric Sunscorched Remains", sunRemains);

        //Prehistoric Suspicious Mound
        List<WeightedItem> mound = new ArrayList<>();
        mound.add(new WeightedItem("cobblemon:fossilized_bird", 5));
        addUncommon(mound, "cobblemon:heat_rock", "cobblemon:damp_rock", "cobblemon:everstone", "cobblemon:dome_fossil", "cobblemon:fossilized_bird", "cobblemon:skull_fossil", "cobblemon:plume_fossil");
        addCommon(mound, "cobblemon:big_root", "cobblemon:energy_root", "cobblemon:leaf_stone", "minecraft:raw_gold", "cobblemon:power_herb", "cobblemon:mirror_herb", "cobblemon:mental_herb", "cobblemon:white_herb");
        register("Prehistoric Suspicious Mound", mound);

        //Prehistoric Underwater Fissure
        List<WeightedItem> fissure = new ArrayList<>();
        fissure.add(new WeightedItem("cobblemon:cover_fossil", 5));
        addUncommon(fissure, "cobblemon:damp_rock", "cobblemon:everstone", "cobblemon:prism_scale", "cobblemon:deep_sea_scale", "cobblemon:deep_sea_tooth", "cobblemon:root_fossil", "cobblemon:fossilized_fish", "cobblemon:claw_fossil");
        addCommon(fissure, "cobblemon:water_stone", "minecraft:raw_gold");
        register("Prehistoric Underwater Fissure", fissure);

        //Prehistoric Vibrant Hydrothermal Vents
        List<WeightedItem> vibrant = new ArrayList<>();
        vibrant.add(new WeightedItem("cobblemon:cover_fossil", 5));
        addUncommon(vibrant, "cobblemon:damp_rock", "cobblemon:everstone", "cobblemon:prism_scale", "cobblemon:deep_sea_scale", "cobblemon:deep_sea_tooth", "cobblemon:root_fossil", "cobblemon:fossilized_fish", "cobblemon:helix_fossil", "cobblemon:cover_fossil");
        addCommon(vibrant, "cobblemon:mirror_herb", "cobblemon:water_stone", "minecraft:raw_gold");
        register("Prehistoric Vibrant Hydrothermal Vents", vibrant);
    }

    private static void register(String name, List<WeightedItem> items) {
        addGlobals(items);
        LOOT_TABLES.put(name, items);
        SITE_NAMES.add(name);
    }

    
    private static void addUncommon(List<WeightedItem> list, String... items) {
        for (String item : items) list.add(new WeightedItem(item, 4));
    }

    private static void addCommon(List<WeightedItem> list, String... items) {
        for (String item : items) list.add(new WeightedItem(item, 10));
    }

    private static void addGlobals(List<WeightedItem> list) {
        // Uncommon in all places
        list.add(new WeightedItem("cobblemon:vivichoke_seeds", 4));
        
        // Common in all places
        list.add(new WeightedItem("minecraft:raw_copper", 10));
        list.add(new WeightedItem("minecraft:coal", 10));
        list.add(new WeightedItem("minecraft:bone", 10));
        list.add(new WeightedItem("minecraft:raw_iron", 10));
    }

    public static ItemStack getRandomLoot(String siteName) {
        List<WeightedItem> table = LOOT_TABLES.getOrDefault(siteName, LOOT_TABLES.get("Prehistoric Birch Tree"));
        if (table == null || table.isEmpty()) return ItemStack.EMPTY;

        int totalWeight = table.stream().mapToInt(i -> i.weight).sum();
        int roll = new Random().nextInt(totalWeight);
        int current = 0;

        for (WeightedItem item : table) {
            current += item.weight;
            if (roll < current) {
                return getItemFromId(item.id);
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack getItemFromId(String id) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
        return new ItemStack(item == null ? Items.AIR : item);
    }

    private static class WeightedItem {
        String id;
        int weight;
        public WeightedItem(String id, int weight) { this.id = id; this.weight = weight; }
    }
}