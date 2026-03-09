package com.ogtenzohd.cmoncol.blocks.custom.pokemonguard;

import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class PokemonGuardBuildingBlockEntity extends TileEntityColonyBuilding {
    public PokemonGuardBuildingBlockEntity(BlockPos pos, BlockState state) {
        super(CmoncolReg.POKEMON_GUARD_BE.get(), pos, state);
    }
}