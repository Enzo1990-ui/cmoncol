package com.ogtenzohd.cmoncol.colony.buildings;

import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.api.colony.IColony;
import com.ogtenzohd.cmoncol.blocks.custom.pasture.PastureBlockEntity;
import com.ogtenzohd.cmoncol.util.RancherRecipeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.Map;
import java.util.function.Predicate;

public class PastureBuilding extends AbstractBuilding {

    public PastureBuilding(IColony colony, BlockPos pos) { super(colony, pos); }
    @Override public String getSchematicName() { return "pasture"; }
    @Override public int getMaxBuildingLevel() { return 5; }

    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount() {
        Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> required = super.getRequiredItemsAndAmount();

        if (colony.getWorld() != null) {
            BlockEntity be = colony.getWorld().getBlockEntity(getPosition());
            if (be instanceof PastureBlockEntity pasture) {
                RegistryAccess registryAccess = colony.getWorld().registryAccess();

                for (PastureBlockEntity.PastureSlot slot : pasture.getStoredPokemon()) {
                    Pokemon storedMon = new Pokemon();
                    storedMon.loadFromNBT(registryAccess, slot.pokemonNBT);
                    String species = storedMon.getSpecies().getName().toLowerCase();

                    RancherRecipeManager.RancherRecipe recipe = RancherRecipeManager.getRecipe(species, slot.selectedRecipe);

                    if (recipe != null && recipe.tool() != net.minecraft.world.item.Items.AIR) {
                        boolean isDamageable = new ItemStack(recipe.tool()).isDamageableItem();
                        int amount = isDamageable ? 1 : 2; 
                        required.put(s -> s.is(recipe.tool()), new Tuple<>(amount, false));
                    }
                }
            }
        }
        return required;
    }
}