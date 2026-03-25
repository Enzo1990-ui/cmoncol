package com.ogtenzohd.cmoncol.colony.job;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.ogtenzohd.cmoncol.colony.ai.WonderTraderAI;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import org.jetbrains.annotations.NotNull;

public class WonderTraderJob extends AbstractJob<WonderTraderAI, WonderTraderJob> {
    public WonderTraderJob(ICitizenData citizenData) {
        super(citizenData);
        this.setRegistryEntry(CmoncolRegistries.WONDER_TRADER_JOB_ENTRY);
    }

    @Override
    @NotNull
    public WonderTraderAI generateAI() { return new WonderTraderAI(this); }

    @Override 
    public String getNameTagDescription() { return "Wonder Trader"; }
}