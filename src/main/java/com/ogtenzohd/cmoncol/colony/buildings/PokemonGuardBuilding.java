package com.ogtenzohd.cmoncol.colony.buildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PokemonGuardBuilding extends AbstractBuildingGuards {

    private static final int BONUS_HP_SINGLE_GUARD = 30;

    public PokemonGuardBuilding(@NotNull final IColony c, final BlockPos l) {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName() {
        return "colony_pokemon_guard";
    }

    @Override
    public int getMaxBuildingLevel() {
        return 5;
    }

    @Override
    public List<IItemHandler> getHandlers() {
        List<IItemHandler> handlers = super.getHandlers();
        IItemHandler buildingInv = this.getItemHandlerCap();
        if (buildingInv != null) {
            handlers.add(buildingInv);
        }
        return handlers;
    }

    @Override
    public int getClaimRadius(int newLevel) {
        switch (newLevel) {
            case 1: return 2;
            case 2: return 3;
            case 3: return 3;
            case 4: return 4;
            case 5: return 5;
            default: return 0;
        }
    }

    @Override
    public void onUpgradeComplete(@org.jetbrains.annotations.Nullable final com.ldtteam.structurize.blueprints.v1.Blueprint blueprint, final int newLevel) {
        super.onUpgradeComplete(blueprint, newLevel);
        colony.getServerBuildingManager().guardBuildingChangedAt(this, newLevel);
    }

    @Override
    public void onDestroyed() {
        super.onDestroyed();
        colony.getServerBuildingManager().guardBuildingChangedAt(this, 0);
    }

    @Override
    public boolean requiresManualTarget() {
        return (patrolTargets == null || patrolTargets.isEmpty() || tempNextPatrolPoint != null || !shallPatrolManually()) && tempNextPatrolPoint == null;
    }

    @Override
    public int getBonusHealth() {
        return BONUS_HP_SINGLE_GUARD + super.getBonusHealth();
    }
	
	@Override
    public void deserializeNBT(@NotNull final net.minecraft.core.HolderLookup.Provider provider, final net.minecraft.nbt.CompoundTag compound) {
        super.deserializeNBT(provider, compound);
        if (!compound.contains("guard")) {
            this.setGuardPos(this.getID());
        }
    }

    @Override
    public net.minecraft.nbt.CompoundTag serializeNBT(@NotNull final net.minecraft.core.HolderLookup.Provider provider) {
        try {
            return super.serializeNBT(provider);
        } catch (NullPointerException e) {
            this.setGuardPos(this.getID());
            return super.serializeNBT(provider);
        }
    }

    public static class View extends AbstractBuildingGuards.View {
        public View(final IColonyView c, @NotNull final BlockPos l) {
            super(c, l);
        }
    }
}