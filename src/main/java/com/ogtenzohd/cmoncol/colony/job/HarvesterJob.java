package com.ogtenzohd.cmoncol.colony.job;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.ogtenzohd.cmoncol.colony.ai.HarvesterAI;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import org.jetbrains.annotations.NotNull;

public class HarvesterJob extends AbstractJob<HarvesterAI, HarvesterJob> {

    public HarvesterJob(ICitizenData citizenData) {
        super(citizenData);
        this.setRegistryEntry(CmoncolRegistries.HARVESTER_JOB_ENTRY);
    }

    @Override
    @NotNull
    public HarvesterAI generateAI() {
        return new HarvesterAI(this);
    }

    @Override 
    public String getNameTagDescription() { 
        return "Apricorn Harvester"; 
    }
}
