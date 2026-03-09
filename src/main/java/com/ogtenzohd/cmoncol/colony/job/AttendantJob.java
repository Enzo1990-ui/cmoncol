package com.ogtenzohd.cmoncol.colony.job;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.ogtenzohd.cmoncol.colony.ai.AttendantAI;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import org.jetbrains.annotations.NotNull;

public class AttendantJob extends AbstractJob<AttendantAI, AttendantJob> {

    public AttendantJob(ICitizenData citizenData) {
        super(citizenData);
        this.setRegistryEntry(CmoncolRegistries.ATTENDANT_JOB_ENTRY);
    }

    @Override
    @NotNull
    public AttendantAI generateAI() {
        return new AttendantAI(this);
    }

    @Override 
    public String getNameTagDescription() { 
        return "Daycare Attendant"; 
    }
}