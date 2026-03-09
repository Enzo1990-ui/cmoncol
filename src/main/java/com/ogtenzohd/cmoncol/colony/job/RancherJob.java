package com.ogtenzohd.cmoncol.colony.job;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.ogtenzohd.cmoncol.colony.ai.RancherAI;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import org.jetbrains.annotations.NotNull;

public class RancherJob extends AbstractJob<RancherAI, RancherJob> {

    public RancherJob(ICitizenData citizenData) {
        super(citizenData);
        this.setRegistryEntry(CmoncolRegistries.RANCHER_JOB_ENTRY);
    }

    @Override
    @NotNull
    public RancherAI generateAI() {
        return new RancherAI(this);
    }

    @Override 
    public String getNameTagDescription() { 
        return "Rancher"; 
    }
}