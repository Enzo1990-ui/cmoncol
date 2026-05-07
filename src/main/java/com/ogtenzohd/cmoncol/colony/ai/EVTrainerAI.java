package com.ogtenzohd.cmoncol.colony.ai;

import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.ogtenzohd.cmoncol.blocks.custom.traineracadamy.TrainerAcadamyBlockEntity;
import com.ogtenzohd.cmoncol.colony.buildings.TrainerAcadamyBuilding;
import com.ogtenzohd.cmoncol.colony.job.EVTrainerJob;
import com.ogtenzohd.cmoncol.config.CCConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    public EVTrainerAI(EVTrainerJob job) { super(job); }

    @Override
    public Class<TrainerAcadamyBuilding> getExpectedBuildingClass() { return TrainerAcadamyBuilding.class; }

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
                        if (gym.isHyperTrain() && gym.getHyperTrainDaysRemaining() == 0) {
                            currentState = State.REQUEST_BOTTLE_CAP;
                        } else {
                            workTimer = 100;
                            currentState = State.WORK_OUT;
                        }
                    }
                    break;
                case REQUEST_BOTTLE_CAP:
                    if (hasBottleCap(entity)) {
                        workTimer = 100;
                        currentState = State.WORK_OUT;
                    } else {
                        Item capToRequest = getAvailableBottleCapItem();
                        if (capToRequest != null) {
                            checkIfRequestForItemExistOrCreateAsync(new ItemStack(capToRequest, 1));
                        }
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

                            if (gym.getHyperTrainDaysRemaining() > 0 && targetPokemon != null) {
                                if (gym.getCurrentTrainingUUID() == null || !gym.getCurrentTrainingUUID().equals(targetPokemon.getUUID())) {
                                    gym.setHyperTrainDaysRemaining(0);
                                }
                            }

                            if (gym.isHyperTrain()) {
                                ItemStack consumedItem = consumeBottleCap(entity);
                                if (!consumedItem.isEmpty()) {
                                    gym.setHyperTrainDaysRemaining(6);
                                    if (targetPokemon != null) {
                                        gym.setCurrentTrainingUUID(targetPokemon.getUUID());
                                    }
                                    gym.setLastHyperTrainDay(-1);
                                }
                                gym.setHyperTrain(false);
                            }

                            long currentDay = entity.level().getDayTime() / 24000L;
                            boolean didIVTrainToday = false;
                            if (gym.getHyperTrainDaysRemaining() > 0) {
                                if (gym.getLastHyperTrainDay() < currentDay) {

                                    Stat nextUnmaxedStat = getNextUnmaxedIV(storedMon);
                                    if (nextUnmaxedStat != null) {
                                        storedMon.getIvs().set(nextUnmaxedStat, 31);
                                        gym.setLastHyperTrainDay(currentDay);
                                        gym.setHyperTrainDaysRemaining(gym.getHyperTrainDaysRemaining() - 1);
                                        didIVTrainToday = true;
                                    } else {
                                        gym.setHyperTrainDaysRemaining(0);
                                    }
                                }
                            }

                            if (!didIVTrainToday) {
                                int currentEV = storedMon.getEvs().getOrDefault(targetStat);
                                int toAdd = Math.min(CCConfig.INSTANCE.evsPerCycle.get(), 252 - currentEV);

                                int totalEVs = storedMon.getEvs().getOrDefault(Stats.HP) +
                                        storedMon.getEvs().getOrDefault(Stats.ATTACK) +
                                        storedMon.getEvs().getOrDefault(Stats.DEFENCE) +
                                        storedMon.getEvs().getOrDefault(Stats.SPECIAL_ATTACK) +
                                        storedMon.getEvs().getOrDefault(Stats.SPECIAL_DEFENCE) +
                                        storedMon.getEvs().getOrDefault(Stats.SPEED);

                                toAdd = Math.min(toAdd, 510 - totalEVs);
                                if (toAdd > 0) {
                                    storedMon.getEvs().set(targetStat, currentEV + toAdd);
                                }
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

    private Stat getNextUnmaxedIV(Pokemon mon) {
        Stat[] order = {Stats.HP, Stats.ATTACK, Stats.DEFENCE, Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED};
        for (Stat s : order) {
            if (mon.getIvs().getOrDefault(s) < 31) return s;
        }
        return null;
    }

    private Stat getStatFromString(String statName) {
        return switch (statName.toLowerCase()) {
            case "hp" -> Stats.HP; case "attack" -> Stats.ATTACK; case "defense" -> Stats.DEFENCE;
            case "spatk" -> Stats.SPECIAL_ATTACK; case "spdef" -> Stats.SPECIAL_DEFENCE; case "speed" -> Stats.SPEED;
            default -> null;
        };
    }

    private boolean isBottleCap(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && id.toString().equals(CCConfig.INSTANCE.bottleCapItem.get());
    }

    private Item getAvailableBottleCapItem() {
        String configuredId = CCConfig.INSTANCE.bottleCapItem.get();
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(configuredId.trim()));

        if (item != Items.AIR) {
            return item;
        }
        return null;
    }

    private boolean hasBottleCap(LivingEntity entity) {
        IItemHandler citizenInv = entity.getCapability(Capabilities.ItemHandler.ENTITY, null);
        if (citizenInv != null) {
            for (int i = 0; i < citizenInv.getSlots(); i++) {
                if (isBottleCap(citizenInv.getStackInSlot(i))) return true;
            }
        }

        IItemHandler bldInv = job.getWorkBuilding().getItemHandlerCap();
        if (bldInv != null) {
            for (int i = 0; i < bldInv.getSlots(); i++) {
                if (isBottleCap(bldInv.getStackInSlot(i))) return true;
            }
        }
        return false;
    }

    private ItemStack consumeBottleCap(LivingEntity entity) {
        IItemHandler citizenInv = entity.getCapability(Capabilities.ItemHandler.ENTITY, null);
        if (citizenInv != null) {
            for (int i = 0; i < citizenInv.getSlots(); i++) {
                ItemStack slotStack = citizenInv.getStackInSlot(i);
                if (isBottleCap(slotStack)) {
                    ItemStack extracted = citizenInv.extractItem(i, 1, false);
                    if (!extracted.isEmpty()) return extracted;
                    else if (slotStack.getCount() > 0) {
                        ItemStack copy = slotStack.copyWithCount(1);
                        slotStack.shrink(1);
                        return copy;
                    }
                }
            }
        }

        IItemHandler bldInv = job.getWorkBuilding().getItemHandlerCap();
        if (bldInv != null) {
            for (int i = 0; i < bldInv.getSlots(); i++) {
                ItemStack slotStack = bldInv.getStackInSlot(i);
                if (isBottleCap(slotStack)) {
                    ItemStack extracted = bldInv.extractItem(i, 1, false);
                    if (!extracted.isEmpty()) return extracted;
                    else if (slotStack.getCount() > 0) {
                        ItemStack copy = slotStack.copyWithCount(1);
                        slotStack.shrink(1);
                        return copy;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }
}