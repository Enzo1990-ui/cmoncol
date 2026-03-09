package com.ogtenzohd.cmoncol.blocks.custom.pokeballworkshop;

import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class PokeballWorkshopBlockEntity extends TileEntityColonyBuilding {
    public PokeballWorkshopBlockEntity(BlockPos pos, BlockState state) {
        super(CmoncolReg.POKEBALLWORKSHOP_BE.get(), pos, state);
    }
}