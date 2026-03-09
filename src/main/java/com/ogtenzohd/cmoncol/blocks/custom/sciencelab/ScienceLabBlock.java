package com.ogtenzohd.cmoncol.blocks.custom.sciencelab;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ScienceLabBlock extends AbstractBlockHut<ScienceLabBlock> {

    public ScienceLabBlock(Properties properties) {
        super(properties);
    }

    @Override
    public String getHutName() {
        return "colony_science_lab";
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "colony_science_lab");
    }

    @Override
    public BuildingEntry getBuildingEntry() {
        return CmoncolRegistries.SCIENCELAB_BUILDING_ENTRY;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ScienceLabBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!level.isClientSide && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.BRUSH) {
            if (level.getBlockEntity(pos) instanceof ScienceLabBlockEntity be) {
                be.cycleDigSite();
                String siteName = be.getDigSiteName();
                player.displayClientMessage(Component.literal("Dig Site changed to: " + siteName), true);
                return InteractionResult.SUCCESS;
            }
        }

        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof ScienceLabBlockEntity hutBE) {
                player.openMenu(hutBE, pos);
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}