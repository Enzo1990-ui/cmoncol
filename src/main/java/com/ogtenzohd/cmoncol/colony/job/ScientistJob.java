package com.ogtenzohd.cmoncol.colony.job;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.ogtenzohd.cmoncol.colony.ai.ScientistAI;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import org.jetbrains.annotations.NotNull;

public class ScientistJob extends AbstractJob<ScientistAI, ScientistJob> {

    public ScientistJob(ICitizenData citizenData) {
        super(citizenData);
        this.setRegistryEntry(CmoncolRegistries.SCIENCELAB_JOB_ENTRY);
    }

    @Override
    @NotNull
    public ScientistAI generateAI() {
        return new ScientistAI(this);
    }

    @Override 
    public String getNameTagDescription() { 
        return "Scientist"; 
    }
}
