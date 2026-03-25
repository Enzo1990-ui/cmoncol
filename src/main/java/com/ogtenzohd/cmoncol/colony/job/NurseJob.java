package com.ogtenzohd.cmoncol.colony.job;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import com.ogtenzohd.cmoncol.colony.buildings.PokeCenterBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.UUID;

public class NurseJob extends AbstractJob<NurseJob.NurseAI, NurseJob> {
    
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
        
        private int generateTimer = 0;

        public NurseAI(NurseJob job) { super(job); }
        @Override public Class<PokeCenterBuilding> getExpectedBuildingClass() { return PokeCenterBuilding.class; }
		
		private int getSkillLevel() {
        if (job.getCitizen() == null || job.getCitizen().getCitizenSkillHandler() == null) return 1;
        return job.getCitizen().getCitizenSkillHandler().getLevel(Skill.Intelligence);
		}

        @Override
        public void tick() {
            if (job.getWorkBuilding() == null || job.getCitizen().getEntity().isEmpty()) return;
            Mob entity = job.getCitizen().getEntity().get();

            BlockPos centerPos = job.getWorkBuilding().getPosition();
            net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) entity.level();

            double distToCenter = entity.distanceToSqr(centerPos.getCenter());
            if (distToCenter > 64) {
                if (entity.tickCount % 20 == 0) {
                    entity.getNavigation().moveTo(centerPos.getX(), centerPos.getY(), centerPos.getZ(), 1.0);
                }
            } else if (entity.getNavigation().isDone() && rand.nextInt(40) == 0) {
                BlockPos p = centerPos.offset(rand.nextInt(7) - 1, 0, rand.nextInt(3) - 2);
                entity.getNavigation().moveTo(p.getX(), p.getY(), p.getZ(), 0.6);
            }

            if (distToCenter <= 100) {
                generateTimer++;

                int intelligence = getSkillLevel();
                int cooldown = Math.max(60, 200 - (intelligence * 10));

                if (generateTimer >= cooldown) {
                    generateMedicalSupplies(serverLevel, centerPos);
                    generateTimer = 0;
                }
            }

            if (entity.tickCount % 200 == 0) {
                entity.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(10)).forEach(target -> {
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
                if (entity.tickCount % 40 == 0) {
                    spawnCompanion(serverLevel, entity);
                }
            } else {
                double distToNurse = companionEntity.distanceToSqr(entity);
                if (distToNurse > 100) {
                    companionEntity.teleportTo(entity.getX(), entity.getY(), entity.getZ());
                } else if (distToNurse > 4) {
                    if (companionEntity.tickCount % 10 == 0) {
                        companionEntity.getNavigation().moveTo(entity.getX(), entity.getY(), entity.getZ(), 1.2);
                    }
                } else {
                    if (companionEntity.tickCount % 10 == 0) {
                        companionEntity.getNavigation().stop();
                    }
                }
            }
        }

        private void generateMedicalSupplies(net.minecraft.server.level.ServerLevel level, BlockPos pos) {
            int bLevel = job.getWorkBuilding().getBuildingLevel();
            Item drop = getRandomDropForLevel(bLevel);

            ItemStack dropStack = new ItemStack(drop, 1);
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            if (handler != null) {
                ItemHandlerHelper.insertItemStacked(handler, dropStack, false);
            }
        }

        private Item getRandomDropForLevel(int level) {
            double roll = rand.nextDouble();
            
            if (level >= 5 && roll < 0.10) return BuiltInRegistries.ITEM.get(ResourceLocation.parse("cobblemon:max_revive"));
            if (level >= 5 && roll < 0.25) return BuiltInRegistries.ITEM.get(ResourceLocation.parse("cobblemon:full_restore"));
            
            if (level >= 4 && roll < 0.40) return BuiltInRegistries.ITEM.get(ResourceLocation.parse("cobblemon:max_potion"));
            if (level >= 4 && roll < 0.50) return BuiltInRegistries.ITEM.get(ResourceLocation.parse("cobblemon:full_heal"));
            
            if (level >= 3 && roll < 0.65) return BuiltInRegistries.ITEM.get(ResourceLocation.parse("cobblemon:revive"));
            if (level >= 3 && roll < 0.75) return BuiltInRegistries.ITEM.get(ResourceLocation.parse("cobblemon:hyper_potion"));
            
            if (level >= 2 && roll < 0.85) return BuiltInRegistries.ITEM.get(ResourceLocation.parse("cobblemon:super_potion"));
            if (level >= 2 && roll < 0.90) return BuiltInRegistries.ITEM.get(ResourceLocation.parse("cobblemon:paralyze_heal"));
            
            return BuiltInRegistries.ITEM.get(ResourceLocation.parse("cobblemon:potion"));
        }

        private void spawnCompanion(net.minecraft.server.level.ServerLevel serverLevel, Mob nurse) {
            String myTag = "nurse_companion_" + nurse.getUUID();
            serverLevel.getEntitiesOfClass(PokemonEntity.class, nurse.getBoundingBox().inflate(32), 
                e -> e.getTags().contains(myTag)).forEach(net.minecraft.world.entity.Entity::discard);

            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("cobblemon:pokemon"));
            net.minecraft.world.entity.Entity entity = type.create(serverLevel);
            if (entity instanceof PokemonEntity pokeEntity) {

                Pokemon mon = new Pokemon();
                var species = PokemonSpecies.getByName(job.getCompanionSpecies().toLowerCase());

                if (species != null) {
                    mon.setSpecies(species);
                }
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