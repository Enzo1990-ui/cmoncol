package com.ogtenzohd.cmoncol.colony.job;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.AbstractJobCrafter;
// THE NEW IMPORT YOU FOUND!
import com.minecolonies.core.entity.ai.workers.crafting.AbstractEntityAICrafting;
import com.ogtenzohd.cmoncol.colony.buildings.PokeballWorkshopBuilding;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import net.minecraft.core.BlockPos;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import org.jetbrains.annotations.NotNull;

public class PokeballWorkshopJob extends AbstractJobCrafter<PokeballWorkshopJob.PokeballWorkshopAI, PokeballWorkshopJob> {

    public PokeballWorkshopJob(ICitizenData citizenData) {
        super(citizenData);
        this.setRegistryEntry(CmoncolRegistries.POKEBALLWORKSHOP_JOB_ENTRY);
    }

    @Override
    @NotNull
    public PokeballWorkshopAI generateAI() {
        return new PokeballWorkshopAI(this);
    }

    @Override
    public String getNameTagDescription() {
        return "Ball Smith";
    }

    public static class PokeballWorkshopAI extends AbstractEntityAICrafting<PokeballWorkshopJob, PokeballWorkshopBuilding> {

        public PokeballWorkshopAI(@NotNull PokeballWorkshopJob job) {
            super(job);
        }

        @Override
        public Class<PokeballWorkshopBuilding> getExpectedBuildingClass() {
            return PokeballWorkshopBuilding.class;
        }
    }

    @Override
    public void playSound(BlockPos blockPos, EntityCitizen worker) {
    }
}