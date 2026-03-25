package com.ogtenzohd.cmoncol.colony.job;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import com.ogtenzohd.cmoncol.colony.ai.RangerAI;
import org.jetbrains.annotations.NotNull;

public class RangerJob extends AbstractJob<RangerAI, RangerJob> {

    public RangerJob(ICitizenData citizenData) {
        super(citizenData);
        this.setRegistryEntry(CmoncolRegistries.RANGER_JOB_ENTRY);
    }

    @Override
    @NotNull
    public RangerAI generateAI() {
        return new RangerAI(this);
    }

    @Override 
    public String getNameTagDescription() { 
        return "Ranger";
    }
}