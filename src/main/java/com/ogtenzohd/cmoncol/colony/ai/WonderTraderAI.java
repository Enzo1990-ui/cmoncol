package com.ogtenzohd.cmoncol.colony.ai;

import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.ogtenzohd.cmoncol.colony.buildings.WonderTradeCentreBuilding;
import com.ogtenzohd.cmoncol.colony.job.WonderTraderJob;
import net.minecraft.core.BlockPos;

public class WonderTraderAI extends AbstractEntityAIBasic<WonderTraderJob, WonderTradeCentreBuilding> {
    
    private enum State { MOVE_TO_CENTRE, WORK_AT_CENTRE }
    private State currentState = State.MOVE_TO_CENTRE;
    private int workTimer = 0;

    public WonderTraderAI(WonderTraderJob job) { super(job); }
    
    @Override 
    public Class<WonderTradeCentreBuilding> getExpectedBuildingClass() { 
        return WonderTradeCentreBuilding.class; 
    }

    public void tick() {
        if (job.getWorkBuilding() == null || job.getColony().getWorld() == null) return;
        
        job.getCitizen().getEntity().ifPresent(entity -> {
            if (!entity.level().isDay() || entity.level().isRaining()) return;
            BlockPos buildingPos = job.getWorkBuilding().getPosition();

            switch (currentState) {
                case MOVE_TO_CENTRE:
                    if (entity.distanceToSqr(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ()) > 9) {
                        entity.getNavigation().moveTo(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ(), 1.0);
                    } else { 
                        entity.getNavigation().stop();
                        currentState = State.WORK_AT_CENTRE; 
                        workTimer = 200;
                    }
                    break;

                case WORK_AT_CENTRE:
                    workTimer--;
                    if (entity.getRandom().nextFloat() < 0.05f) {
                         entity.getLookControl().setLookAt(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ());
                    }
                    
                    if (workTimer <= 0) {
                        currentState = State.MOVE_TO_CENTRE;
                    }
                    break;
            }
        });
    }
}