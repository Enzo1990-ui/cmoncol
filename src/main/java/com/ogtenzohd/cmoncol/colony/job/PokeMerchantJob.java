package com.ogtenzohd.cmoncol.colony.job;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.ogtenzohd.cmoncol.colony.ai.PokeMerchantAI;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import org.jetbrains.annotations.NotNull;

public class PokeMerchantJob extends AbstractJob<PokeMerchantAI, PokeMerchantJob> {
    public PokeMerchantJob(ICitizenData citizenData) {
        super(citizenData);
        this.setRegistryEntry(CmoncolRegistries.POKEMERCHANT_JOB_ENTRY);
    }

    @Override
    @NotNull
    public PokeMerchantAI generateAI() { return new PokeMerchantAI(this); }

    @Override 
    public String getNameTagDescription() { return "PokeMerchant"; }
}