package com.ogtenzohd.cmoncol.blocks.custom.pokemart;

import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class PokemartBlockEntity extends TileEntityColonyBuilding {
    public PokemartBlockEntity(BlockPos pos, BlockState state) {
        super(CmoncolReg.POKEMART_BE.get(), pos, state);
    }
}