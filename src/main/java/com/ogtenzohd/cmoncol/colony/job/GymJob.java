package com.ogtenzohd.cmoncol.colony.job;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.ogtenzohd.cmoncol.colony.ai.ReceptionistAI;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import org.jetbrains.annotations.NotNull;

public class GymJob extends AbstractJob<ReceptionistAI, GymJob> {

    public GymJob(ICitizenData citizenData) {
        super(citizenData);
        this.setRegistryEntry(CmoncolRegistries.GYM_JOB_ENTRY);
    }

    @Override
    @NotNull
    public ReceptionistAI generateAI() {
        return new ReceptionistAI(this);
    }

    @Override 
    public String getNameTagDescription() { 
        return "Gym Receptionist"; 
    }
}