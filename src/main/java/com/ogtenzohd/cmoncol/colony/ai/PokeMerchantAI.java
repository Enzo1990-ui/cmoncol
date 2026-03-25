package com.ogtenzohd.cmoncol.colony.ai;

import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.ogtenzohd.cmoncol.colony.buildings.PokemartBuilding;
import com.ogtenzohd.cmoncol.colony.job.PokeMerchantJob;
import net.minecraft.core.BlockPos;

public class PokeMerchantAI extends AbstractEntityAIBasic<PokeMerchantJob, PokemartBuilding> {
    
    private enum State { MOVE_TO_SHOP, TEND_SHOP }
    private State currentState = State.MOVE_TO_SHOP;
    private int wanderDelay = 0;

    public PokeMerchantAI(PokeMerchantJob job) { super(job); }
    
    @Override 
    public Class<PokemartBuilding> getExpectedBuildingClass() { 
        return PokemartBuilding.class; 
    }

    public void tick() {
        if (job.getWorkBuilding() == null || job.getColony().getWorld() == null) return;
        
        job.getCitizen().getEntity().ifPresent(entity -> {
            if (!entity.level().isDay() || entity.level().isRaining()) return;
            BlockPos buildingPos = job.getWorkBuilding().getPosition();

            switch (currentState) {
                case MOVE_TO_SHOP:
                    if (entity.distanceToSqr(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ()) > 4) {
                        entity.getNavigation().moveTo(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ(), 1.0);
                    } else { 
                        entity.getNavigation().stop();
                        currentState = State.TEND_SHOP; 
                        wanderDelay = 100;
                    }
                    break;

                case TEND_SHOP:
                    wanderDelay--;
                    if (wanderDelay <= 0) {
                        double offsetX = (entity.getRandom().nextDouble() - 0.5) * 3;
                        double offsetZ = (entity.getRandom().nextDouble() - 0.5) * 3;
                        entity.getNavigation().moveTo(buildingPos.getX() + offsetX, buildingPos.getY(), buildingPos.getZ() + offsetZ, 0.6);
                        wanderDelay = 200 + entity.getRandom().nextInt(200);
                    }
                    
                    if (entity.distanceToSqr(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ()) > 25) {
                        currentState = State.MOVE_TO_SHOP;
                    }
                    break;
            }
        });
    }
}