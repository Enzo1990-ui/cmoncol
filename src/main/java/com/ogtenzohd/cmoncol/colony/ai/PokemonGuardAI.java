package com.ogtenzohd.cmoncol.colony.ai;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.minecolonies.api.entity.ai.combat.threat.IThreatTableEntity;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.core.entity.ai.workers.guard.AbstractEntityAIGuard;
import com.minecolonies.core.entity.ai.workers.guard.KnightCombatAI;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.ogtenzohd.cmoncol.colony.buildings.PokemonGuardBuilding;
import com.ogtenzohd.cmoncol.colony.job.PokemonGuardJob;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.UUID;

public class PokemonGuardAI extends AbstractEntityAIGuard<PokemonGuardJob, PokemonGuardBuilding> {
    
    private UUID partnerUUID = null;
    private int spawnTimer = 60;

    public PokemonGuardAI(PokemonGuardJob job) { 
        super(job); 
        toolsNeeded.add(ModEquipmentTypes.sword.get());
        new KnightCombatAI((EntityCitizen) worker, getStateAI(), this);
    }

    @Override 
    public Class<PokemonGuardBuilding> getExpectedBuildingClass() { 
        return PokemonGuardBuilding.class; 
    }


    @Override
    public void tick() {
        if (job.getWorkBuilding() == null || job.getColony().getWorld() == null) {
            return;
        }

        super.tick(); 

        job.getCitizen().getEntity().ifPresent(guard -> {
            if (guard.level() instanceof ServerLevel serverLevel) {
                if (partnerUUID == null || serverLevel.getEntity(partnerUUID) == null) {
                    spawnTimer--;
                    if (spawnTimer <= 0) {
                        cleanUpOldPartners(guard, serverLevel);
                        spawnPartner(guard, serverLevel);
                        spawnTimer = 20; 
                    }
                }
            }
        });
    }

    private void spawnPartner(LivingEntity guard, ServerLevel level) {
        Pokemon mon = PokemonProperties.Companion.parse("species=growlithe level=30 uncatchable=true").create();
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("cobblemon:pokemon"));

        Entity newEntity = type.create(level);
        if (newEntity instanceof PokemonEntity pokeEntity) {
            pokeEntity.setPokemon(mon);
            pokeEntity.setPos(guard.getX(), guard.getY(), guard.getZ());
            pokeEntity.setInvulnerable(false);
            pokeEntity.setPersistenceRequired();
            pokeEntity.getTags().add("guard_partner_" + guard.getUUID());

            injectGuardGoals(pokeEntity, guard);

            level.addFreshEntity(pokeEntity);
            this.partnerUUID = pokeEntity.getUUID();
        }
    }
    
    private void cleanUpOldPartners(LivingEntity guard, ServerLevel level) {
        String targetTag = "guard_partner_" + guard.getUUID();
        java.util.List<PokemonEntity> orphans = level.getEntitiesOfClass(
            PokemonEntity.class, 
            guard.getBoundingBox().inflate(64.0D), 
            entity -> entity.getTags().contains(targetTag)
        );
        for (PokemonEntity orphan : orphans) {
            orphan.discard(); 
        }
    }

    private void injectGuardGoals(PokemonEntity pokemon, LivingEntity guard) {
        pokemon.goalSelector.addGoal(2, new Goal() {
            { this.setFlags(EnumSet.of(Goal.Flag.MOVE)); }
            @Override public boolean canUse() { return guard.isAlive() && pokemon.distanceToSqr(guard) > 25; }
            @Override public void tick() { pokemon.getNavigation().moveTo(guard, 1.2D); }
        });

        pokemon.targetSelector.addGoal(1, new Goal() {
            @Override public boolean canUse() { return guard.getLastHurtMob() != null && guard.getLastHurtMob().isAlive(); }
            @Override public void start() { pokemon.setTarget(guard.getLastHurtMob()); }
        });

        pokemon.targetSelector.addGoal(2, new Goal() {
            @Override public boolean canUse() { return guard.getLastHurtByMob() != null && guard.getLastHurtByMob().isAlive(); }
            @Override public void start() { pokemon.setTarget(guard.getLastHurtByMob()); }
        });

        pokemon.targetSelector.addGoal(3, new Goal() {
            @Override public boolean canUse() { 
                if (guard instanceof IThreatTableEntity threatEntity) {
                    return threatEntity.getThreatTable().getTargetMob() != null && threatEntity.getThreatTable().getTargetMob().isAlive();
                }
                return false; 
            }
            @Override public void start() { 
                if (guard instanceof IThreatTableEntity threatEntity) {
                    pokemon.setTarget(threatEntity.getThreatTable().getTargetMob()); 
                }
            }
        });

        
        pokemon.goalSelector.addGoal(1, new Goal() {
            private int attackDelay = 0;
            { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

            @Override
            public boolean canUse() {
                return pokemon.getTarget() != null && pokemon.getTarget().isAlive() && pokemon.distanceToSqr(pokemon.getTarget()) < 64.0D; 
            }

            @Override
            public void tick() {
                LivingEntity target = pokemon.getTarget();
                if (target == null) return;

                pokemon.getLookControl().setLookAt(target, 30.0F, 30.0F);
                pokemon.getNavigation().moveTo(target, 1.0D);

                if (attackDelay > 0) attackDelay--;

                if (attackDelay <= 0 && pokemon.distanceToSqr(target) < 16.0D) { 
                    
                    target.hurt(pokemon.damageSources().mobAttack(pokemon), 10.0F);
                    
                    //just the basic attack looks like a tackle so ill add some effects maybe look like flamethrower or ember (hopefully?)
                    if (pokemon.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.FLAME, 
                            target.getX(), target.getY() + 1, target.getZ(), 
                            15, 0.5D, 0.5D, 0.5D, 0.1D);
                    }
                    
                    attackDelay = 15; //might be a bit to slow but ill leave for player feedback when the mod goes live
                }
            }
        });
    }
}