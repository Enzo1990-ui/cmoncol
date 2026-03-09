package com.ogtenzohd.cmoncol.colony.ai;

import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.ogtenzohd.cmoncol.colony.buildings.GymBuilding;
import com.ogtenzohd.cmoncol.colony.job.GymJob;
import com.ogtenzohd.cmoncol.entity.GhostReceptionistEntity;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class ReceptionistAI extends AbstractEntityAIBasic<GymJob, GymBuilding> {
    private enum State { GO_TO_DESK, HIDE }
    private State currentState = State.GO_TO_DESK;
    private UUID ghostUUID = null;

    public ReceptionistAI(GymJob job) { super(job); }

    @Override 
    public Class<GymBuilding> getExpectedBuildingClass() { return GymBuilding.class; }

    @Override
    public void tick() {
        if (job.getWorkBuilding() == null) return;

        job.getCitizen().getEntity().ifPresent(entity -> {
            BlockPos buildingPos = job.getWorkBuilding().getPosition();
            ServerLevel level = (ServerLevel) entity.level();

            switch (currentState) {
                case GO_TO_DESK:
                    if (ghostUUID != null) {
                        cleanUpOldGhosts(entity, level);
                        ghostUUID = null;
                    }

                    if (entity.distanceToSqr(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ()) > 9) {
                        entity.getNavigation().moveTo(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ(), 1.0);
                    } else {
                        entity.getNavigation().stop();
                        currentState = State.HIDE;
                    }
                    break;

                case HIDE:
                    if (ghostUUID == null || level.getEntity(ghostUUID) == null) {
                        cleanUpOldGhosts(entity, (ServerLevel)level);
                        
                        GhostReceptionistEntity ghost = com.ogtenzohd.cmoncol.registration.CmoncolReg.GHOST_RECEPTIONIST.get().create(level);
                        if (ghost != null) {
                            ghost.setPos(entity.getX(), entity.getY(), entity.getZ());
                            ghost.addTag("gym_ghost_for_" + entity.getUUID());
                            level.addFreshEntity(ghost);
                            this.ghostUUID = ghost.getUUID();
                        }
                    } else {
                        net.minecraft.world.entity.Entity ghost = level.getEntity(ghostUUID);
                        if (ghost != null) {
                            ghost.setPos(entity.getX(), entity.getY(), entity.getZ());
                        }
                    }

                    if (entity.distanceToSqr(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ()) > 25) {
                        cleanUpOldGhosts(entity, (ServerLevel)level);
                        this.ghostUUID = null;
                        currentState = State.GO_TO_DESK;
                    }
                    break;
            }
        });
    }

    private void cleanUpOldGhosts(LivingEntity worker, ServerLevel level) {
        String targetTag = "gym_ghost_for_" + worker.getUUID();
        java.util.List<GhostReceptionistEntity> orphans = level.getEntitiesOfClass(
            GhostReceptionistEntity.class, 
            worker.getBoundingBox().inflate(10.0D), 
            e -> e.getTags().contains(targetTag)
        );
        for (GhostReceptionistEntity orphan : orphans) {
            orphan.discard(); 
        }
    }
}