package com.ogtenzohd.cmoncol.blocks.custom.pokemart;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PokemartBlock extends AbstractBlockHut<PokemartBlock> {
    public PokemartBlock(Properties properties) { super(properties); }

    @Override
    public String getHutName() { return "colony_pokemart"; }

    @Override
    public ResourceLocation getRegistryName() { return ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "colony_pokemart"); }

    @Override
    public BuildingEntry getBuildingEntry() { return CmoncolRegistries.POKEMART_BUILDING_ENTRY; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PokemartBlockEntity(pos, state); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof PokemartBlockEntity hutBE) {
                player.openMenu(hutBE, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}