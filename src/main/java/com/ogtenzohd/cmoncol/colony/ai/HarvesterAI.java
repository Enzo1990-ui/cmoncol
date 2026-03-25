package com.ogtenzohd.cmoncol.colony.ai;

import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.ogtenzohd.cmoncol.colony.buildings.HarvesterBuilding;
import com.ogtenzohd.cmoncol.colony.job.HarvesterJob;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import java.util.ArrayList;
import java.util.List;

public class HarvesterAI extends AbstractEntityAIBasic<HarvesterJob, HarvesterBuilding> {

    private enum State { SEARCH, MOVE_TO, HARVEST, DEPOSIT, COOLDOWN }
    private State currentState = State.SEARCH;

    private BlockPos targetApricorn = null; 
    private int workTimer = 0;
    
    private static final String[] COLORS = {"red", "pink", "yellow", "blue", "green", "white", "black"};
    private int colorIndex = 0;
    private int cooldownTimer = 0;

    private final List<BlockPos> apricornCache = new ArrayList<>();
    private long lastCacheUpdate = 0;
	private record HarvestTask(BlockPos fruitPos, BlockPos standPos) {}
    private final List<HarvestTask> shoppingList = new ArrayList<>();

    public HarvesterAI(HarvesterJob job) { super(job); }

    @Override 
    public Class<HarvesterBuilding> getExpectedBuildingClass() { return HarvesterBuilding.class; }

    @SuppressWarnings("ConstantValue")
    @Override
    public void tick() {
        if (job.getWorkBuilding() == null || job.getCitizen().getEntity().isEmpty()) return;
        LivingEntity entity = job.getCitizen().getEntity().get();
        if (!(entity instanceof Mob mob)) return;

        BlockPos buildingPos = job.getWorkBuilding().getPosition();

        switch (currentState) {
            case SEARCH:
                if (entity.level().getGameTime() - lastCacheUpdate > 100 || apricornCache.isEmpty()) {
                    scanForCurrentColor(entity, buildingPos);
                }
                
                shoppingList.clear();
                for (BlockPos fruitPos : apricornCache) {
                    if (isApricornMature(entity.level(), fruitPos)) {
                        BlockPos groundPos = findGroundBelow(entity.level(), fruitPos);
                        shoppingList.add(new HarvestTask(fruitPos, groundPos));
                        
                        if (shoppingList.size() >= 12) break;
                    }
                }

                if (!shoppingList.isEmpty()) {
                    currentState = State.MOVE_TO;
                } else if (hasItemsToDeposit(entity)) {
                    currentState = State.DEPOSIT;
                } else {
                    switchColor();
                }
                break;

            case MOVE_TO:
                if (shoppingList.isEmpty()) {
                    currentState = State.DEPOSIT;
                    return;
                }

                HarvestTask currentTask = shoppingList.getFirst();
                targetApricorn = currentTask.fruitPos();
                BlockPos standAt = currentTask.standPos();
                
                if (!isValidApricorn(entity.level().getBlockState(targetApricorn)) || !isApricornMature(entity.level(), targetApricorn)) {
                    shoppingList.removeFirst();
                    return; 
                }

                double distSq = entity.distanceToSqr(standAt.getCenter());
                
                if (distSq > 1.5) {
                    mob.getNavigation().moveTo(standAt.getX(), standAt.getY(), standAt.getZ(), 1.0);
                } else {
                    mob.getNavigation().stop();
                    currentState = State.HARVEST;
                    workTimer = 20; 
                }
                break;

            case HARVEST:
                if (targetApricorn == null) { currentState = State.SEARCH; return; }
                
                mob.getLookControl().setLookAt(targetApricorn.getX(), targetApricorn.getY(), targetApricorn.getZ());
                workTimer--;
                
                if (workTimer <= 0) {
                    entity.swing(InteractionHand.MAIN_HAND);
                    harvestApricorn(entity, targetApricorn);
                    
                    shoppingList.removeFirst();
                    targetApricorn = null;
                    
                    currentState = shoppingList.isEmpty() ? State.DEPOSIT : State.MOVE_TO;
                }
                break;

            case DEPOSIT:
                if (entity.distanceToSqr(buildingPos.getCenter()) > 9) {
                    mob.getNavigation().moveTo(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ(), 1.0);
                } else {
                    mob.getNavigation().stop();
                    depositItems(entity);
                    
                    // Tea break!
                    cooldownTimer = 1200; 
                    currentState = State.COOLDOWN;
                }
                break;

            case COOLDOWN:
                if (cooldownTimer > 0) {
                    cooldownTimer--;
                    mob.getNavigation().stop();
                } else {
                    switchColor();
                    currentState = State.SEARCH;
                }
                break;
        }
	}

    @SuppressWarnings("deprecation")
    private BlockPos findGroundBelow(net.minecraft.world.level.Level level, BlockPos target) {
        BlockPos.MutableBlockPos pos = target.mutable().move(0, -1, 0);
        for (int i = 0; i < 5; i++) {
            if (!level.getBlockState(pos).isAir() && level.getBlockState(pos).isSolid()) {
                return pos.above().immutable(); 
            }
            pos.move(0, -1, 0);
        }
        return target.below(); 
    }

    private void switchColor() {
        colorIndex = (colorIndex + 1) % COLORS.length;
        apricornCache.clear();
        lastCacheUpdate = 0; 
    }

    private void scanForCurrentColor(LivingEntity entity, BlockPos center) {
        apricornCache.clear();
        
        int minX, maxX, minY, maxY, minZ, maxZ;
        Tuple<BlockPos, BlockPos> corners = job.getWorkBuilding() != null ? job.getWorkBuilding().getCorners() : null;

        if (corners != null) {
             BlockPos a = corners.getA();
             BlockPos b = corners.getB();
             minX = Math.min(a.getX(), b.getX());
             maxX = Math.max(a.getX(), b.getX());
             minY = Math.min(a.getY(), b.getY());
             maxY = Math.max(a.getY(), b.getY());
             minZ = Math.min(a.getZ(), b.getZ());
             maxZ = Math.max(a.getZ(), b.getZ());
             maxY += 5; 
        } else {
             int r = 15;
             minX = center.getX() - r; maxX = center.getX() + r;
             minY = center.getY(); maxY = center.getY() + 10;
             minZ = center.getZ() - r; maxZ = center.getZ() + r;
        }

        String targetColor = COLORS[colorIndex];

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = entity.level().getBlockState(pos);
                    if (isColorApricorn(state, targetColor)) {
                        apricornCache.add(pos);
                    }
                }
            }
        }
        lastCacheUpdate = entity.level().getGameTime();
    }

    private boolean isColorApricorn(BlockState state, String color) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return id.getNamespace().equals("cobblemon") 
            && id.getPath().contains(color)
            && id.getPath().contains("apricorn") 
            && !id.getPath().contains("leaves") 
            && !id.getPath().contains("sapling");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isValidApricorn(BlockState state) {
        return isColorApricorn(state, COLORS[colorIndex]);
    }

    private boolean isApricornMature(net.minecraft.world.level.Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!isValidApricorn(state)) return false;
        
        for (Property<?> prop : state.getProperties()) {
            if (prop.getName().equals("age") && prop instanceof IntegerProperty intProp) {
                int currentAge = state.getValue(intProp);
                return currentAge == 3;
            }
        }
        return false;
    }

    private void harvestApricorn(LivingEntity entity, BlockPos pos) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            BlockState state = serverLevel.getBlockState(pos);
            if (!isApricornMature(serverLevel, pos)) return;

            ItemStack drop = new ItemStack(state.getBlock().asItem(), 1);
            if (!drop.isEmpty()) insertItemIntoPockets(entity, drop);

            for (Property<?> prop : state.getProperties()) {
                if (prop.getName().equals("age") && prop instanceof IntegerProperty intProp) {
                    serverLevel.setBlockAndUpdate(pos, state.setValue(intProp, 0));
                    break;
                }
            }
        }
    }
	
	private boolean hasItemsToDeposit(LivingEntity entity) {
    IItemHandler citizenInv = entity.getCapability(Capabilities.ItemHandler.ENTITY, null);
    if (citizenInv != null) {
        for (int i = 0; i < citizenInv.getSlots(); i++) {
            if (!citizenInv.getStackInSlot(i).isEmpty()) return true;
        }
    }
    return false;
	}

    private void depositItems(LivingEntity entity) {
    IItemHandler citizenInv = entity.getCapability(Capabilities.ItemHandler.ENTITY, null);
    IItemHandler buildingInv = job.getWorkBuilding().getItemHandlerCap();
    
    if (citizenInv != null && buildingInv != null) {
        for (int i = 0; i < citizenInv.getSlots(); i++) {
            ItemStack stack = citizenInv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemStack remaining = ItemHandlerHelper.insertItemStacked(buildingInv, stack, false);
                if (remaining.getCount() < stack.getCount()) {
                    citizenInv.extractItem(i, stack.getCount() - remaining.getCount(), false);
                }
            }
        }
    }
	}

    private void insertItemIntoPockets(LivingEntity entity, ItemStack stack) {
        IItemHandler citizenInv = entity.getCapability(Capabilities.ItemHandler.ENTITY, null);
        if (citizenInv != null) {
            ItemStack remaining = ItemHandlerHelper.insertItemStacked(citizenInv, stack, false);
            if (!remaining.isEmpty()) net.minecraft.world.Containers.dropItemStack(entity.level(), entity.getX(), entity.getY(), entity.getZ(), remaining);
        }
    }
}