package com.ogtenzohd.cmoncol.blocks.custom.ranger;

import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RangerBlockEntity extends TileEntityColonyBuilding {
    public RangerBlockEntity(BlockPos pos, BlockState state) {
        super(CmoncolReg.RANGER_BE.get(), pos, state);
    }
}