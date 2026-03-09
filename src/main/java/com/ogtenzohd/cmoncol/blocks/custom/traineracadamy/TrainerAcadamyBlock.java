package com.ogtenzohd.cmoncol.blocks.custom.traineracadamy;

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
import org.jetbrains.annotations.Nullable;

public class TrainerAcadamyBlock extends AbstractBlockHut<TrainerAcadamyBlock> {

    public TrainerAcadamyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public String getHutName() {
        return "colony_traineracadamy";
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "colony_traineracadamy");
    }

    @Override
    public BuildingEntry getBuildingEntry() {
        return CmoncolRegistries.TRAINER_ACADAMY_BUILDING_ENTRY;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrainerAcadamyBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == CmoncolReg.TRAINER_ACADAMY_BE.get()) {
            return (BlockEntityTicker<T>) (BlockEntityTicker<TrainerAcadamyBlockEntity>) (l, p, s, be) -> be.tick();
        }
        return null;
    }
	
	@Override
    public void onRemove(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof com.ogtenzohd.cmoncol.blocks.entity.CobblemonProxyBlockEntity proxy) {
                proxy.emergencyRecoverPokemon();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof TrainerAcadamyBlockEntity hutBE) {
                player.openMenu(hutBE, pos);
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}