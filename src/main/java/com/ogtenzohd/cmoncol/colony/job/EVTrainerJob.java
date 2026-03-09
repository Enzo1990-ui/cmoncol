package com.ogtenzohd.cmoncol.colony.job;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.ogtenzohd.cmoncol.colony.ai.EVTrainerAI;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import org.jetbrains.annotations.NotNull;

public class EVTrainerJob extends AbstractJob<EVTrainerAI, EVTrainerJob> {

    public EVTrainerJob(ICitizenData citizenData) {
        super(citizenData);
        this.setRegistryEntry(CmoncolRegistries.EV_TRAINER_JOB_ENTRY);
    }

    @Override
    @NotNull
    public EVTrainerAI generateAI() {
        return new EVTrainerAI(this);
    }

    @Override 
    public String getNameTagDescription() { 
        return "EV Trainer"; 
    }
}