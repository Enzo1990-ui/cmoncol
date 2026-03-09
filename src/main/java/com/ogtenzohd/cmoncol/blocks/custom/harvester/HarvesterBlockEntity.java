package com.ogtenzohd.cmoncol.blocks.custom.harvester;

import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class HarvesterBlockEntity extends TileEntityColonyBuilding {
    public HarvesterBlockEntity(BlockPos pos, BlockState state) {
        super(CmoncolReg.HARVESTER_BE.get(), pos, state);
    }
}