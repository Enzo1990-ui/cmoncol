package com.ogtenzohd.cmoncol.colony.buildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public class PokemonGuardBuilding extends AbstractBuildingGuards {

    private static final int MAX_LEVEL = 1;
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
    public int getClaimRadius(final int newLevel) {
        switch (newLevel) {
            default: return 5;
        }
    }
	
	@Override
    public void onUpgradeComplete(final int newLevel) {
        super.onUpgradeComplete(newLevel);
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