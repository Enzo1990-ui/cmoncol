package com.ogtenzohd.cmoncol.util; // Or wherever you placed this file

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.ogtenzohd.cmoncol.items.ItemPokemonEgg; // Import your Egg Class!
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RangerRewardGenerator {

    public static ItemStack generateRangerEgg(ServerLevel level, int buildingLevel) {
        boolean isShiny = false;
        boolean isLegendary = false;
        int perfectIvs = 0;

        int totalRolls = 1;
        for (int i = 2; i <= buildingLevel; i++) {
            if (level.random.nextDouble() < 0.10) {
                totalRolls++;
            }
        }

        for (int i = 0; i < totalRolls; i++) {
            int roll = level.random.nextInt(100);
            if (roll < 75) {
                perfectIvs++;
            } else if (roll < 90) {
                isShiny = true;
            } else {
                isLegendary = true;
            }
        }

        List<Species> allImplemented = new ArrayList<>(PokemonSpecies.getImplemented());
        List<Species> legendaryPool = new ArrayList<>();
        List<Species> standardPool = new ArrayList<>();

        for (Species species : allImplemented) {
            if (species.getLabels().contains("legendary") ||
                    species.getLabels().contains("mythical") ||
                    species.getLabels().contains("ultra_beast")) {
                legendaryPool.add(species);
            } else {
                standardPool.add(species);
            }
        }

        if (legendaryPool.isEmpty()) {
            legendaryPool.addAll(standardPool);
        }

        List<Species> activePool = isLegendary ? legendaryPool : standardPool;
        Species selectedSpecies = activePool.get(level.random.nextInt(activePool.size()));

        Pokemon eggMon = selectedSpecies.create(1);

        if (isShiny) {
            eggMon.setShiny(true);
        }

        List<Stat> statsToMax = new ArrayList<>(List.of(Stats.HP, Stats.ATTACK, Stats.DEFENCE, Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED));
        Collections.shuffle(statsToMax);

        int maxIvsToApply = Math.min(perfectIvs, 6);
        for (int i = 0; i < maxIvsToApply; i++) {
            eggMon.getIvs().set(statsToMax.get(i), 31);
        }
        ItemStack eggStack = ItemPokemonEgg.createEgg(eggMon);
        net.minecraft.nbt.CompoundTag eggData = eggMon.saveToNBT(level.registryAccess(), new net.minecraft.nbt.CompoundTag());
        ItemPokemonEgg.setPokemonData(eggStack, eggData);

        return eggStack;
    }
}