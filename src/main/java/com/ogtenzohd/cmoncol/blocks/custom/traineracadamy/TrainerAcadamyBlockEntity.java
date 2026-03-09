package com.ogtenzohd.cmoncol.blocks.custom.traineracadamy;

import com.ogtenzohd.cmoncol.blocks.entity.CobblemonProxyBlockEntity;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class TrainerAcadamyBlockEntity extends CobblemonProxyBlockEntity {

    private String targetStat = "hp";
    private boolean hyperTrain = false;

    public TrainerAcadamyBlockEntity(BlockPos pos, BlockState state) {
        super(CmoncolReg.TRAINER_ACADAMY_BE.get(), pos, state);
    }

    public String getTargetStat() {
        return targetStat;
    }

    public void setTargetStat(String targetStat) {
        this.targetStat = targetStat.toLowerCase();
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isHyperTrain() {
        return hyperTrain;
    }

    public void setHyperTrain(boolean hyperTrain) {
        this.hyperTrain = hyperTrain;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putString("TargetStat", targetStat);
        tag.putBoolean("HyperTrain", hyperTrain);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("TargetStat")) {
            this.targetStat = tag.getString("TargetStat");
        }
        if (tag.contains("HyperTrain")) {
            this.hyperTrain = tag.getBoolean("HyperTrain");
        }
    }
}