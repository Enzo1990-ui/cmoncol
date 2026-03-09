package com.ogtenzohd.cmoncol.colony.ai;

import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.ogtenzohd.cmoncol.blocks.custom.pasture.PastureBlockEntity;
import com.ogtenzohd.cmoncol.colony.buildings.PastureBuilding;
import com.ogtenzohd.cmoncol.colony.job.RancherJob;
import com.ogtenzohd.cmoncol.util.RancherRecipeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.Containers;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;

public class RancherAI extends AbstractEntityAIBasic<RancherJob, PastureBuilding> {
    private enum State { MOVE_TO_TARGET, CHECK_POKEMON, CHECK_TOOL, WORK, RETURN_TO_HUT, DEPOSIT_ITEMS }
    private State currentState = State.MOVE_TO_TARGET;
    private int workTimer = 0;
    private Item requiredTool = Items.AIR;
    private net.minecraft.world.entity.Entity currentTargetPokemon = null;
    
    private int lastSlotIndex = -1;
    
    private RancherRecipeManager.RancherRecipe activeRecipe = null;

    public RancherAI(RancherJob job) { super(job); }
    @Override public Class<PastureBuilding> getExpectedBuildingClass() { return PastureBuilding.class; }

    public void tick() {
        if (job.getWorkBuilding() == null || job.getColony().getWorld() == null) return;

        job.getCitizen().getEntity().ifPresent(entity -> {
            if (!entity.level().isDay() || entity.level().isRaining()) return;
            BlockPos buildingPos = job.getWorkBuilding().getPosition();

            switch (currentState) {
                case MOVE_TO_TARGET:
                    BlockPos movePos = (currentTargetPokemon != null) ? currentTargetPokemon.blockPosition() : buildingPos;
                    if (entity.distanceToSqr(movePos.getX(), movePos.getY(), movePos.getZ()) > 9) {
                        entity.getNavigation().moveTo(movePos.getX(), movePos.getY(), movePos.getZ(), 1.0);
                    } else { 
                        entity.getNavigation().stop();
                        currentState = (currentTargetPokemon != null) ? State.WORK : State.CHECK_POKEMON;
                    }
                    break;
				//add round robin as its just grabbing the first one! --done
                case CHECK_POKEMON:
                    BlockEntity be = entity.level().getBlockEntity(buildingPos);
                    if (be instanceof PastureBlockEntity pasture) {
                        RegistryAccess registryAccess = entity.level().registryAccess();
                        int size = pasture.getStoredPokemon().size();
                        if (size == 0) return;

                        int startIndex = (lastSlotIndex + 1) % size;
                        
                        for (int i = 0; i < size; i++) {
                            int currentIndex = (startIndex + i) % size;
                            
                            PastureBlockEntity.PastureSlot slot = pasture.getStoredPokemon().get(currentIndex);
                            Pokemon storedMon = new Pokemon();
                            storedMon.loadFromNBT(registryAccess, slot.pokemonNBT);
                            String speciesName = storedMon.getSpecies().getName().toLowerCase();
                            
                            RancherRecipeManager.RancherRecipe recipe = RancherRecipeManager.getRecipe(speciesName, slot.selectedRecipe);
                            if (recipe != null) {
                                net.minecraft.world.entity.Entity foundEntity = null;
                                if (slot.spawnedEntityUUID != null && entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                                    foundEntity = serverLevel.getEntity(slot.spawnedEntityUUID);
                                }
                                
                                if (foundEntity != null && foundEntity.isAlive()) {
                                    this.activeRecipe = recipe;
                                    this.requiredTool = recipe.tool();
                                    this.currentTargetPokemon = foundEntity;
                                    this.lastSlotIndex = currentIndex;
                                    
                                    currentState = State.CHECK_TOOL;
                                    workTimer = 100;
                                    return; 
                                }
                            }
                        }
                    }
                    break;

                case CHECK_TOOL:
                    if (hasToolInBuilding(requiredTool, entity)) { 
                        currentState = State.MOVE_TO_TARGET; 
                    } else {
                        checkIfRequestForItemExistOrCreateAsync(new ItemStack(requiredTool));
                        entity.getLookControl().setLookAt(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ());
                    }
                    break;

                case WORK:
                    workTimer--;
                    if (currentTargetPokemon != null) {
                        entity.getLookControl().setLookAt(currentTargetPokemon, 30.0F, 30.0F);
                        entity.getNavigation().stop();
                    }
                    if (workTimer % 20 == 0) entity.swing(InteractionHand.MAIN_HAND);
                    if (workTimer <= 0) {
                        RancherRecipeManager.RancherRecipe finalRecipe = activeRecipe;
                        if (finalRecipe == null && currentTargetPokemon != null) {
                             finalRecipe = findRecipeForEntity(buildingPos, currentTargetPokemon, entity.level().registryAccess());
                        }
                        
                        if (finalRecipe != null && handleToolUsage(requiredTool, entity)) {
                            executeWork(finalRecipe, entity);
                        }
                        currentState = State.RETURN_TO_HUT;
                    }
                    break;

                case RETURN_TO_HUT:
                    if (entity.distanceToSqr(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ()) > 4) {
                        entity.getNavigation().moveTo(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ(), 1.0);
                    } else {
                        entity.getNavigation().stop();
                        currentState = State.DEPOSIT_ITEMS;
                    }
                    break;

                case DEPOSIT_ITEMS:
                    IItemHandler citizenInv = entity.getCapability(Capabilities.ItemHandler.ENTITY, null);
                    IItemHandler buildingInv = job.getWorkBuilding().getItemHandlerCap();
                    
                    if (citizenInv != null && buildingInv != null) {
                        for (int i = 0; i < citizenInv.getSlots(); i++) {
                            ItemStack stack = citizenInv.getStackInSlot(i);
                            if (!stack.isEmpty() && stack.getItem() != requiredTool) {
                                ItemStack remaining = ItemHandlerHelper.insertItemStacked(buildingInv, stack, false);
                                if (remaining.getCount() < stack.getCount()) {
                                    citizenInv.extractItem(i, stack.getCount() - remaining.getCount(), false);
                                }
                            }
                        }
                    }
                    
                    activeRecipe = null;
                    requiredTool = Items.AIR;
                    currentTargetPokemon = null; 
                    currentState = State.MOVE_TO_TARGET;
                    break;
            }
        });
    }

    private RancherRecipeManager.RancherRecipe findRecipeForEntity(BlockPos buildingPos, net.minecraft.world.entity.Entity target, RegistryAccess access) {
        BlockEntity be = job.getColony().getWorld().getBlockEntity(buildingPos);
        if (be instanceof PastureBlockEntity pasture) {
            for (PastureBlockEntity.PastureSlot slot : pasture.getStoredPokemon()) {
                if (slot.spawnedEntityUUID != null && slot.spawnedEntityUUID.equals(target.getUUID())) {
                    Pokemon storedMon = new Pokemon();
                    storedMon.loadFromNBT(access, slot.pokemonNBT);
                    return RancherRecipeManager.getRecipe(storedMon.getSpecies().getName().toLowerCase(), slot.selectedRecipe);
                }
            }
        }
        return null;
    }

    private void executeWork(RancherRecipeManager.RancherRecipe recipe, LivingEntity entity) {
        if (recipe == null) return;
        for (RancherRecipeManager.Drop drop : recipe.drops()) {
            if (entity.getRandom().nextFloat() <= drop.chance()) {
                int count = drop.min() + entity.getRandom().nextInt(Math.max(1, drop.max() - drop.min() + 1));
                if (count > 0) {
                    insertItemIntoPockets(new ItemStack(drop.item(), count));
                }
            }
        }
    }

    private void insertItemIntoPockets(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (job.getCitizen().getEntity().isPresent()) {
            LivingEntity citizen = job.getCitizen().getEntity().get();
            IItemHandler citizenInv = citizen.getCapability(Capabilities.ItemHandler.ENTITY, null);
            if (citizenInv != null) {
                ItemStack remaining = ItemHandlerHelper.insertItemStacked(citizenInv, stack, false);
                if (!remaining.isEmpty()) {
                    Containers.dropItemStack(job.getColony().getWorld(), citizen.getX(), citizen.getY(), citizen.getZ(), remaining);
                }
            }
        }
    }

    private boolean hasToolInBuilding(Item tool, LivingEntity entity) {
        if (tool == Items.AIR) return true;
        if (entity.getMainHandItem().getItem() == tool) return true;
        IItemHandler citizenInv = entity.getCapability(Capabilities.ItemHandler.ENTITY, null);
        if (citizenInv != null) {
            for (int i = 0; i < citizenInv.getSlots(); i++) {
                if (citizenInv.getStackInSlot(i).getItem() == tool) return true;
            }
        }
        IItemHandler buildingInv = job.getWorkBuilding().getItemHandlerCap();
        if (buildingInv != null) {
            for (int i = 0; i < buildingInv.getSlots(); i++) {
                if (buildingInv.getStackInSlot(i).getItem() == tool) return true;
            }
        }
        return false;
    }

    private boolean handleToolUsage(Item tool, LivingEntity entity) {
        if (tool == Items.AIR) return true;
        ItemStack mainHand = entity.getMainHandItem();
        if (mainHand.getItem() == tool) {
             if (mainHand.isDamageableItem()) {
                 mainHand.hurtAndBreak(1, entity, EquipmentSlot.MAINHAND);
             } else {
                 mainHand.shrink(1); 
             }
             return true;
        }
        IItemHandler citizenInv = entity.getCapability(Capabilities.ItemHandler.ENTITY, null);
        if (citizenInv != null) {
            for (int i = 0; i < citizenInv.getSlots(); i++) {
                ItemStack stack = citizenInv.getStackInSlot(i);
                if (stack.getItem() == tool) {
                    if (stack.isDamageableItem()) {
                        stack.hurtAndBreak(1, entity, EquipmentSlot.MAINHAND);
                    } else {
                        citizenInv.extractItem(i, 1, false); 
                    }
                    return true;
                }
            }
        }
        IItemHandler buildingInv = job.getWorkBuilding().getItemHandlerCap();
        if (buildingInv != null) {
            for (int i = 0; i < buildingInv.getSlots(); i++) {
                ItemStack stack = buildingInv.getStackInSlot(i);
                if (stack.getItem() == tool) {
                    if (!stack.isDamageableItem()) {
                        buildingInv.extractItem(i, 1, false); 
                    }
                    return true;
                }
            }
        }
        return false; 
    }
}