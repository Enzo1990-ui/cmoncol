// gave up on this and moved it to rancher manager for a different format



//package com.ogtenzohd.cmoncol.config;

//import net.neoforged.neoforge.common.ModConfigSpec;
//import org.apache.commons.lang3.tuple.Pair;
//import java.util.List;
//import java.util.ArrayList;

//public class RancherConfig {
//    public static final RancherConfig INSTANCE;
//    public static final ModConfigSpec SPEC;

//    static {
//       final Pair<RancherConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(RancherConfig::new);
//        SPEC = specPair.getRight();
//        INSTANCE = specPair.getLeft();
//    }

//    public final ModConfigSpec.ConfigValue<List<? extends String>> rancher_recipes;

//    public RancherConfig(ModConfigSpec.Builder builder) {
//        builder.push("Rancher Recipes");
        
//        List<String> defaults = new ArrayList<>();

        //here i go again another wiki page meaning i write all these items 
		
//		.comment("You can find every drop here >> https://docs.google.com/spreadsheets/d/1BL5F8TSLUL6deM_4JqyF6qk7BaTFepMpJzh4-eBtFPY/edit?gid=0#gid=0")
		
		
        //gen1
//        defaults.add("bulbasaur ; none ; minecraft:melon_seeds, 1 ; cobblemon:miracle_seed, 5%");
//        defaults.add("ivysaur ; none ; minecraft:melon_seeds, 2 ; cobblemon:miracle_seed, 10%");
//        defaults.add("venusaur ; none ; minecraft:melon_seeds, 3 ; cobblemon:miracle_seed, 25%");
//        defaults.add("charmander ; none ; minecraft:blaze_powder, 1 ; cobblemon:charcoal_stick, 5%");
//        defaults.add("charmeleon ; none ; minecraft:blaze_powder, 2 ; cobblemon:charcoal_stick, 10%");
//        defaults.add("charizard ; none ; minecraft:blaze_powder, 3 ; cobblemon:charcoal_stick, 25%");
//        defaults.add("squirtle ; none ; minecraft:turtle_scute, 1 ; cobblemon:mystic_water, 5%");
//        defaults.add("wartortle ; none ; minecraft:turtle_scute, 2 ; cobblemon:mystic_water, 10%");
//        defaults.add("blastoise ; none ; minecraft:turtle_scute, 3 ; cobblemon:mystic_water, 25%");
//        defaults.add("caterpie ; minecraft:shears ; minecraft:string, 1 ; cobblemon:wepear_berry, 2.5%");
//        defaults.add("metapod ; minecraft:shears ; minecraft:string, 2 ; cobblemon:wepear_berry, 2.5%");
//        defaults.add("butterfree ; none ; cobblemon:silver_powder, 5% ; cobblemon:wepear_berry, 5%");
//        defaults.add("weedle ; minecraft:shears ; minecraft:string, 1 ; cobblemon:pinap_berry, 2.5%");
//        defaults.add("kakuna ; minecraft:shears ; minecraft:string, 2 ; cobblemon:pinap_berry, 2.5%");
//        defaults.add("beedrill ; none ; cobblemon:poison_barb, 5% ; cobblemon:pinap_berry, 5%");
//       defaults.add("pidgey ; minecraft:shears ; minecraft:feather, 1 ; minecraft:raw_chicken, 1 ; cobblemon:chilan_berry, 2.5%");
//        defaults.add("pidgeotto ; minecraft:shears ; minecraft:feather, 2 ; minecraft:raw_chicken, 1 ; cobblemon:sharp_beak, 2.5%");
//        defaults.add("pidgeot ; minecraft:shears ; minecraft:feather, 3 ; minecraft:raw_chicken, 1 ; cobblemon:sharp_beak, 5%");
//        defaults.add("rattata ; none ; minecraft:rotten_flesh, 1 ; cobblemon:chilan_berry, 2.5%");
//        defaults.add("raticate ; none ; minecraft:rotten_flesh, 2 ; cobblemon:chilan_berry, 5%");
//        defaults.add("pikachu ; none ; cobblemon:light_ball, 5% ; minecraft:thunder_stone, 5% ; cobblemon:oran_berry, 5%");
//        defaults.add("vulpix ; none ; minecraft:sweet_berries, 2 ; minecraft:charcoal, 2.5% ; cobblemon:rawst_berry, 2.5%");
//        defaults.add("paras ; none ; minecraft:red_mushroom, 1 ; cobblemon:coba_berry, 2.5%");
//        defaults.add("meowth ; none ; minecraft:gold_nugget, 1 ; cobblemon:quick_claw, 2.5%");
//        defaults.add("mankey ; none ; cobblemon:muscle_band, 2.5% ; cobblemon:payapa_berry, 2.5%");
//        defaults.add("growlithe ; none ; minecraft:bone, 1 ; cobblemon:rawst_berry, 2.5%");
//        defaults.add("abra ; none ; minecraft:ender_pearl, 1 ; cobblemon:twisted_spoon, 2.5%");
//        defaults.add("geodude ; none ; minecraft:gravel, 1 ; cobblemon:everstone, 2.5% ; cobblemon:black_augurite, 2.5%");
//        defaults.add("magnemite ; none ; minecraft:raw_iron, 1 ; minecraft:magnet, 2.5% ; cobblemon:metal_coat, 2.5%");
//        defaults.add("grimer ; none ; minecraft:slime_ball, 1 ; cobblemon:black_sludge, 2.5%");
//        defaults.add("onix ; none ; minecraft:stone, 2 ; cobblemon:hard_stone, 2.5%");
//        defaults.add("exeggcute ; none ; minecraft:egg, 2 ; cobblemon:oval_stone, 2.5%");
//        defaults.add("chansey ; none ; cobblemon:lucky_egg, 10% ; cobblemon:oval_stone, 10% ; minecraft:egg, 50%");
//        defaults.add("staryu ; none ; minecraft:glowstone_dust, 1");
//        defaults.add("magikarp ; none ; minecraft:raw_salmon, 1 ; minecraft:bone_meal, 5%");
//        defaults.add("eevee ; none ; minecraft:silk_scarf, 5% ; cobblemon:eviolite, 5%");
//        defaults.add("snorlax ; none ; minecraft:apple, 2 ; cobblemon:chesto_berry, 5% ; cobblemon:leftovers, 5%");
		//nope i give up
        //gen2
//        defaults.add("mareep ; minecraft:shears ; minecraft:raw_mutton, 1 ; minecraft:white_wool, 2");
//        defaults.add("flaaffy ; minecraft:shears ; minecraft:raw_mutton, 2 ; minecraft:white_wool, 1");
//        defaults.add("miltank ; minecraft:bucket ; minecraft:milk_bucket, 1 ; minecraft:raw_beef, 2");
//        defaults.add("shuckle ; none ; cobblemon:berry_juice, 1");

        //gen4
//        defaults.add("combee ; minecraft:glass_bottle ; minecraft:honey_bottle, 1 ; cobblemon:poison_barb, 2.5%");
//        defaults.add("combee ; minecraft:shears ; minecraft:honeycomb, 3 ; cobblemon:charti_berry, 2.5%");
//        defaults.add("vespiquen ; minecraft:glass_bottle ; minecraft:honey_bottle, 3 ; cobblemon:poison_barb, 10%");
//        defaults.add("vespiquen ; minecraft:shears ; minecraft:honeycomb, 5 ; cobblemon:charti_berry, 10%");

        //gen8
//        defaults.add("wooloo ; minecraft:shears ; minecraft:raw_mutton, 1 ; minecraft:white_wool, 1");
//        defaults.add("dubwool ; minecraft:shears ; minecraft:raw_mutton, 2 ; minecraft:white_wool, 2");

//        rancher_recipes = builder
//            .comment("Block Style Configuration")
//            .comment("Format: species ; tool ; drop, amount/chance ; drop, amount/chance ...")
//            .defineList("recipes", defaults, o -> o instanceof String);
//        builder.pop();
//    }
//}