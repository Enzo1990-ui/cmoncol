package com.ogtenzohd.cmoncol.colony.buildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PokeballWorkshopBuilding extends AbstractBuilding {

    public PokeballWorkshopBuilding(IColony colony, BlockPos pos) {
        super(colony, pos);
    }

    @NotNull
    @Override
    public String getSchematicName() {
        return "colony_pokeballworkshop";
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Crafting {

        public CraftingModule(final JobEntry jobEntry) {
            super(jobEntry);
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe) {
            if (!super.isRecipeCompatible(recipe)) return false;

            return recipe.matchesOutput(com.minecolonies.api.util.OptionalPredicate.passIf(output -> {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(output.getItem());
                if (id.getNamespace().equals("minecraft")) return false;
                return id.getPath().contains("ball");
            })).orElse(false);
        }
    }
}