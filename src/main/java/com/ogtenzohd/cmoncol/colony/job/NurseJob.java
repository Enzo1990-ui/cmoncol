package com.ogtenzohd.cmoncol.colony.job;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import com.ogtenzohd.cmoncol.colony.buildings.PokeCenterBuilding;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import net.minecraft.resources.ResourceLocation;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;
import java.util.Random;
import java.util.UUID;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class NurseJob extends AbstractJob<NurseJob.NurseAI, NurseJob> {
    
	//i think thats the only companions nurse joy has!
    private static final String[] COMPANIONS = {"chansey", "blissey", "comfey"};

    public NurseJob(ICitizenData citizenData) {
        super(citizenData);
        this.setRegistryEntry(CmoncolRegistries.NURSE_JOB_ENTRY);
    }

    public String getCompanionSpecies() {
        String citizenName = this.getCitizen().getName();
        int index = Math.abs(citizenName.hashCode()) % COMPANIONS.length;
        return COMPANIONS[index];
    }

    @Override
    @NotNull
    public NurseAI generateAI() { return new NurseAI(this); }

    public static class NurseAI extends AbstractEntityAIBasic<NurseJob, PokeCenterBuilding> {
        private UUID companionUUID = null;
        private final Random rand = new Random();

        public NurseAI(NurseJob job) { super(job); }
        @Override public Class<PokeCenterBuilding> getExpectedBuildingClass() { return PokeCenterBuilding.class; }

        @Override
        public void tick() {
            if (job.getWorkBuilding() == null || !job.getCitizen().getEntity().isPresent()) return;
            LivingEntity entity = job.getCitizen().getEntity().get();
            if (!(entity instanceof Mob mob)) return;
            
            BlockPos centerPos = job.getWorkBuilding().getPosition();
            net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) entity.level();

            double distToCenter = mob.distanceToSqr(centerPos.getCenter());
            if (distToCenter > 64) {
                if (mob.tickCount % 20 == 0) {
                    mob.getNavigation().moveTo(centerPos.getX(), centerPos.getY(), centerPos.getZ(), 1.0);
                }
            } else if (mob.getNavigation().isDone() && rand.nextInt(40) == 0) {
                BlockPos p = centerPos.offset(rand.nextInt(7) - 1, 0, rand.nextInt(3) - 2);
                mob.getNavigation().moveTo(p.getX(), p.getY(), p.getZ(), 0.6);
            }

            if (mob.tickCount % 200 == 0) {
                mob.level().getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(10)).forEach(target -> {
                    target.removeEffect(MobEffects.POISON);
                    target.heal(1.0f);
                });
            }

            PokemonEntity companionEntity = null;
            if (companionUUID != null) {
                net.minecraft.world.entity.Entity e = serverLevel.getEntity(companionUUID);
                if (e instanceof PokemonEntity pe) {
                    companionEntity = pe;
                }
            }

            if (companionEntity == null || !companionEntity.isAlive()) {
                if (mob.tickCount % 40 == 0) {
                    spawnCompanion(serverLevel, mob);
                }
            } else {
                double distToNurse = companionEntity.distanceToSqr(mob);
                if (distToNurse > 100) {
                    companionEntity.teleportTo(mob.getX(), mob.getY(), mob.getZ());
                } else if (distToNurse > 4) {
                    if (companionEntity.tickCount % 10 == 0) {
                        companionEntity.getNavigation().moveTo(mob.getX(), mob.getY(), mob.getZ(), 1.2);
                    }
                } else {
                    if (companionEntity.tickCount % 10 == 0) {
                        companionEntity.getNavigation().stop();
                    }
                }
            }
        }

        private void spawnCompanion(net.minecraft.server.level.ServerLevel serverLevel, Mob nurse) {
            String myTag = "nurse_companion_" + nurse.getUUID().toString();
            serverLevel.getEntitiesOfClass(PokemonEntity.class, nurse.getBoundingBox().inflate(32), 
                e -> e.getTags().contains(myTag)).forEach(net.minecraft.world.entity.Entity::discard);

            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("cobblemon:pokemon"));
            if (type != null) {
                net.minecraft.world.entity.Entity entity = type.create(serverLevel);
                if (entity instanceof PokemonEntity pokeEntity) {
                    
                    Pokemon mon = new Pokemon();
                    
                    var species = com.cobblemon.mod.common.api.pokemon.PokemonSpecies.INSTANCE.getByName(job.getCompanionSpecies().toLowerCase());
                                        
                    mon.setSpecies(species);
                    pokeEntity.setPokemon(mon);
                    
                    pokeEntity.setPos(nurse.getX(), nurse.getY(), nurse.getZ());
                    
                    pokeEntity.getPokemon().setUuid(UUID.randomUUID());
                    pokeEntity.getTags().add("cmoncol_dummy");
                    pokeEntity.getTags().add(myTag);
                    pokeEntity.setInvulnerable(true); 
                    pokeEntity.setPersistenceRequired(); 
                    
                    pokeEntity.setCustomName(Component.literal("Nurse's " + mon.getDisplayName(true).getString()));
                    pokeEntity.setCustomNameVisible(true);
                    
                    serverLevel.addFreshEntity(pokeEntity);
                    companionUUID = pokeEntity.getUUID();
                }
            }
        }
    }
}