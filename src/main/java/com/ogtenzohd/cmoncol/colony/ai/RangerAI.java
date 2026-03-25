package com.ogtenzohd.cmoncol.colony.ai;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.ogtenzohd.cmoncol.colony.buildings.WatchTowerBuilding;
import com.ogtenzohd.cmoncol.colony.job.RangerJob;
import com.ogtenzohd.cmoncol.entity.RangerEntity;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class RangerAI extends AbstractEntityAIBasic<RangerJob, WatchTowerBuilding> {
    private UUID spawnedGhostUUID = null;
    private UUID targetPokemonUUID = null;
    private int stareTimer = 0;

    public RangerAI(@NotNull RangerJob job) {
        super(job);
    }

    public void tick() {
        if (job.getWorkBuilding() == null || job.getCitizen().getEntity().isEmpty()) return;
        Mob ranger = job.getCitizen().getEntity().get();
        ServerLevel serverLevel = (ServerLevel) ranger.level();

        long timeOfDay = serverLevel.getDayTime() % 24000;

        if (timeOfDay > 0 && timeOfDay < 4000) {
            BlockPos deskPos = job.getWorkBuilding().getPosition();

            if (ranger.distanceToSqr(deskPos.getX(), deskPos.getY(), deskPos.getZ()) > 9) {
                ranger.getNavigation().moveTo(deskPos.getX(), deskPos.getY(), deskPos.getZ(), 1.0);
            } else {
                ranger.getNavigation().stop();

                if (spawnedGhostUUID == null) {
                    RangerEntity ghost = new RangerEntity(CmoncolReg.RANGER_ENTITY.get(), serverLevel);
                    ghost.setPos(deskPos.getX() + 0.5, deskPos.getY(), deskPos.getZ() + 0.5);
                    ghost.setBuildingLevel(job.getWorkBuilding().getBuildingLevel());

                    serverLevel.addFreshEntity(ghost);
                    spawnedGhostUUID = ghost.getUUID();
                }
            }
        } else {
            cleanUpOldGhosts(ranger, serverLevel);

            if (targetPokemonUUID != null) {
                Entity target = serverLevel.getEntity(targetPokemonUUID);
                if (target instanceof PokemonEntity pokeTarget && pokeTarget.isAlive()) {

                    if (ranger.distanceToSqr(target) > 16) {
                        ranger.getNavigation().moveTo(target, 1.0);
                    } else {
                        ranger.getNavigation().stop();
                        ranger.getLookControl().setLookAt(target, 30.0F, 30.0F);

                        stareTimer++;
                        if (stareTimer > 100) {
                            targetPokemonUUID = null;
                            stareTimer = 0;
                        }
                    }
                } else {
                    targetPokemonUUID = null;
                }
            }
            else if (ranger.tickCount % 60 == 0) {
                List<PokemonEntity> wildMons = serverLevel.getEntitiesOfClass(
                        PokemonEntity.class,
                        ranger.getBoundingBox().inflate(32),
                        e -> !e.getTags().contains("cmoncol_dummy")
                );

                if (!wildMons.isEmpty()) {
                    targetPokemonUUID = wildMons.get(serverLevel.random.nextInt(wildMons.size())).getUUID();
                } else {
                    wanderRandomly(ranger);
                }
            }
        }
    }

    private void wanderRandomly(Mob ranger) {
        if (ranger.getNavigation().isDone()) {
            if (ranger instanceof net.minecraft.world.entity.PathfinderMob pathfinder) {
                net.minecraft.world.phys.Vec3 randomSpot = net.minecraft.world.entity.ai.util.DefaultRandomPos.getPos(pathfinder, 10, 7);

                if (randomSpot != null) {
                    ranger.getNavigation().moveTo(randomSpot.x, randomSpot.y, randomSpot.z, 1.0);
                }
            }
        }
    }

    private void cleanUpOldGhosts(Mob ranger, ServerLevel serverLevel) {
        if (spawnedGhostUUID != null) {
            Entity ghost = serverLevel.getEntity(spawnedGhostUUID);
            if (ghost != null) {
                ghost.discard();
            }
            spawnedGhostUUID = null;
        }

        if (job.getWorkBuilding() != null) {
            BlockPos deskPos = job.getWorkBuilding().getPosition();

            net.minecraft.world.phys.AABB searchArea = new net.minecraft.world.phys.AABB(deskPos).inflate(5);

            java.util.List<RangerEntity> orphanedGhosts = serverLevel.getEntitiesOfClass(RangerEntity.class, searchArea);

            for (RangerEntity orphan : orphanedGhosts) {
                orphan.discard();
            }
        }
    }

    @Override
    public Class<WatchTowerBuilding> getExpectedBuildingClass() {
        return WatchTowerBuilding.class;
    }
}
