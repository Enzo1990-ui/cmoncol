package com.ogtenzohd.cmoncol.blocks.custom.gym;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class GymBlock extends AbstractBlockHut<GymBlock> {
    
    public GymBlock(Properties properties) { super(properties); }

    @Override
    public String getHutName() { return "colony_gym"; }

    @Override
    public ResourceLocation getRegistryName() { return ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "colony_gym"); }

    @Override
    public BuildingEntry getBuildingEntry() { return CmoncolRegistries.GYM_BUILDING_ENTRY; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new GymBlockEntity(pos, state); }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == CmoncolReg.GYM_BE.get()) {
            return (BlockEntityTicker<T>) (BlockEntityTicker<GymBlockEntity>) (l, p, s, be) -> be.tick();
        }
        return null;
    }
}