package com.ogtenzohd.cmoncol.blocks.custom.pokeballworkshop;

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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PokeballWorkshopBlock extends AbstractBlockHut<PokeballWorkshopBlock> {

    public PokeballWorkshopBlock(Properties properties) { super(properties); }

    @Override
    public String getHutName() { return "colony_ball_workshop"; }

    @Override
    public ResourceLocation getRegistryName() { return ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "colony_ball_workshop"); }

    @Override
    public BuildingEntry getBuildingEntry() { return CmoncolRegistries.POKEBALLWORKSHOP_BUILDING_ENTRY; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity( BlockPos pos, BlockState state) { return new PokeballWorkshopBlockEntity(pos, state); }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker( Level level, BlockState state, BlockEntityType<T> type) {
        if (type == CmoncolReg.POKEBALLWORKSHOP_BE.get()) {
            return (BlockEntityTicker<T>) (BlockEntityTicker<PokeballWorkshopBlockEntity>) (l, p, s, be) -> be.tick();
        }
        return null;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem( BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof PokeballWorkshopBlockEntity hutBE) {
                player.openMenu(hutBE, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}