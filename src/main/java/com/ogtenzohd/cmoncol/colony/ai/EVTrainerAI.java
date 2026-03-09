package com.ogtenzohd.cmoncol.colony.ai;

import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.ogtenzohd.cmoncol.blocks.custom.traineracadamy.TrainerAcadamyBlockEntity;
import com.ogtenzohd.cmoncol.colony.buildings.TrainerAcadamyBuilding;
import com.ogtenzohd.cmoncol.colony.job.EVTrainerJob;
import com.ogtenzohd.cmoncol.config.CCConfig;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;

public class EVTrainerAI extends AbstractEntityAIBasic<EVTrainerJob, TrainerAcadamyBuilding> {
    private enum State { MOVE_TO_GYM, CHECK_TRAINING_GOALS, REQUEST_BOTTLE_CAP, WORK_OUT }
    private State currentState = State.MOVE_TO_GYM;
    private int workTimer = 0;
    public static final TagKey<Item> BOTTLE_CAP_TAG = ItemTags.create(ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "bottle_caps"));

    public EVTrainerAI(EVTrainerJob job) { super(job); }
    @Override public Class<TrainerAcadamyBuilding> getExpectedBuildingClass() { return TrainerAcadamyBuilding.class; }

    public void tick() {
        if (job.getWorkBuilding() == null || job.getColony().getWorld() == null) return;

        job.getCitizen().getEntity().ifPresent(entity -> {
            if (!entity.level().isDay() || entity.level().isRaining()) return;
            
            BlockPos buildingPos = job.getWorkBuilding().getPosition();
            BlockPos targetPos = buildingPos;
            net.minecraft.world.entity.Entity targetPokemon = null;

            if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                BlockEntity be = serverLevel.getBlockEntity(buildingPos);
                if (be instanceof TrainerAcadamyBlockEntity gym && gym.getSpawnedEntityUUID() != null) {
                    net.minecraft.world.entity.Entity p = serverLevel.getEntity(gym.getSpawnedEntityUUID());
                    if (p != null) {
                        targetPokemon = p;
                        targetPos = p.blockPosition();
                    }
                }
            }

            switch (currentState) {
                case MOVE_TO_GYM:
                    if (entity.distanceToSqr(targetPos.getX(), targetPos.getY(), targetPos.getZ()) > 9) {
                        entity.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);
                    } else { 
                        entity.getNavigation().stop();
                        currentState = State.CHECK_TRAINING_GOALS; 
                    }
                    break;
                case CHECK_TRAINING_GOALS:
                    BlockEntity be = entity.level().getBlockEntity(buildingPos);
                    if (be instanceof TrainerAcadamyBlockEntity gym && gym.hasPokemon()) {
                        RegistryAccess registryAccess = entity.level().registryAccess();
                        Pokemon storedMon = new Pokemon();
                        storedMon.loadFromNBT(registryAccess, gym.getStoredPokemonData());
                        Stat targetStat = getStatFromString(gym.getTargetStat());
                        if (targetStat == null) return;
                        if (gym.isHyperTrain()) {
                            if (storedMon.getIvs().getOrDefault(targetStat) < 31) { currentState = State.REQUEST_BOTTLE_CAP; } 
                            else { currentState = State.MOVE_TO_GYM; }
                        } else {
                            int currentEV = storedMon.getEvs().getOrDefault(targetStat);
                            if (currentEV < 252) {
                                workTimer = 100; 
                                currentState = State.WORK_OUT;
                            } else { currentState = State.MOVE_TO_GYM; }
                        }
                    }
                    break;
                case REQUEST_BOTTLE_CAP:
                    if (hasBottleCap(entity)) {
                        workTimer = 100; 
                        currentState = State.WORK_OUT;
                    } else {
                        checkIfRequestForTagExistOrCreateAsync(BOTTLE_CAP_TAG, 1);
                        entity.getLookControl().setLookAt(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ());
                    }
                    break;
                case WORK_OUT:
                    workTimer--;
                    
                    if (targetPokemon != null) {
                        entity.getLookControl().setLookAt(targetPokemon, 30.0F, 30.0F);
                    } else {
                        entity.getLookControl().setLookAt(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ());
                    }
                    
                    if (workTimer % 20 == 0) entity.swing(InteractionHand.MAIN_HAND);

                    if (workTimer <= 0) {
                        BlockEntity gymBe = entity.level().getBlockEntity(buildingPos);
                        if (gymBe instanceof TrainerAcadamyBlockEntity gym) {
                            RegistryAccess registryAccess = entity.level().registryAccess();
                            Pokemon storedMon = new Pokemon();
                            storedMon.loadFromNBT(registryAccess, gym.getStoredPokemonData());
                            Stat targetStat = getStatFromString(gym.getTargetStat());

                            if (gym.isHyperTrain()) {
                                consumeBottleCap(entity);
                                storedMon.getIvs().set(targetStat, 31);
                                gym.setHyperTrain(false); 
                            } else {
                                int currentEV = storedMon.getEvs().getOrDefault(targetStat);
                                int toAdd = Math.min(CCConfig.INSTANCE.evsPerCycle.get(), 252 - currentEV);
                                
                                int totalEVs = storedMon.getEvs().getOrDefault(Stats.HP) +
                                               storedMon.getEvs().getOrDefault(Stats.ATTACK) +
                                               storedMon.getEvs().getOrDefault(Stats.DEFENCE) +
                                               storedMon.getEvs().getOrDefault(Stats.SPECIAL_ATTACK) +
                                               storedMon.getEvs().getOrDefault(Stats.SPECIAL_DEFENCE) +
                                               storedMon.getEvs().getOrDefault(Stats.SPEED);
                                               
                                toAdd = Math.min(toAdd, 510 - totalEVs);
                                storedMon.getEvs().set(targetStat, currentEV + toAdd);
                            }
                            
                            CompoundTag newTag = storedMon.saveToNBT(registryAccess, new CompoundTag());
                            gym.setProxyData(gym.getOwnerUUID(), gym.getOwnerName(), newTag);
                        }
                        currentState = State.MOVE_TO_GYM; 
                    }
                    break;
            }
        });
    }

    private Stat getStatFromString(String statName) {
        return switch (statName.toLowerCase()) {
            case "hp" -> Stats.HP; case "attack" -> Stats.ATTACK; case "defense" -> Stats.DEFENCE;
            case "spatk" -> Stats.SPECIAL_ATTACK; case "spdef" -> Stats.SPECIAL_DEFENCE; case "speed" -> Stats.SPEED;
            default -> null;
        };
    }

    private boolean hasBottleCap(LivingEntity entity) {
        IItemHandler citizenInv = entity.getCapability(Capabilities.ItemHandler.ENTITY, null);
        if (citizenInv != null) {
            for (int i = 0; i < citizenInv.getSlots(); i++) {
                if (citizenInv.getStackInSlot(i).is(BOTTLE_CAP_TAG)) return true;
            }
        }
        
        IItemHandler bldInv = job.getWorkBuilding().getItemHandlerCap();
        if (bldInv != null) {
            for (int i = 0; i < bldInv.getSlots(); i++) {
                if (bldInv.getStackInSlot(i).is(BOTTLE_CAP_TAG)) return true;
            }
        }
        return false;
    }

    private void consumeBottleCap(LivingEntity entity) {
        IItemHandler citizenInv = entity.getCapability(Capabilities.ItemHandler.ENTITY, null);
        if (citizenInv != null) {
            for (int i = 0; i < citizenInv.getSlots(); i++) {
                if (citizenInv.getStackInSlot(i).is(BOTTLE_CAP_TAG)) {
                    citizenInv.extractItem(i, 1, false);
                    return;
                }
            }
        }
        
        IItemHandler bldInv = job.getWorkBuilding().getItemHandlerCap();
        if (bldInv != null) {
            for (int i = 0; i < bldInv.getSlots(); i++) {
                if (bldInv.getStackInSlot(i).is(BOTTLE_CAP_TAG)) {
                    bldInv.extractItem(i, 1, false);
                    return;
                }
            }
        }
    }
}