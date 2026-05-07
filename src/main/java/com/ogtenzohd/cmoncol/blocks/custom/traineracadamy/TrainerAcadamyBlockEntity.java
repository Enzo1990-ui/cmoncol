package com.ogtenzohd.cmoncol.blocks.custom.traineracadamy;

import com.ogtenzohd.cmoncol.blocks.entity.CobblemonProxyBlockEntity;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class TrainerAcadamyBlockEntity extends CobblemonProxyBlockEntity {

    private String targetStat = "hp";
    private boolean hyperTrain = false;
    private int hyperTrainDaysRemaining = 0;
    private long lastHyperTrainDay = -1;
    private UUID currentTrainingUUID = null;

    public TrainerAcadamyBlockEntity(BlockPos pos, BlockState state) {
        super(CmoncolReg.TRAINER_ACADAMY_BE.get(), pos, state);
    }

    public String getTargetStat() { return targetStat; }
    public void setTargetStat(String targetStat) {
        this.targetStat = targetStat.toLowerCase();
        syncData();
    }

    public boolean isHyperTrain() { return hyperTrain; }
    public void setHyperTrain(boolean hyperTrain) {
        this.hyperTrain = hyperTrain;
        syncData();
    }

    public int getHyperTrainDaysRemaining() { return hyperTrainDaysRemaining; }
    public void setHyperTrainDaysRemaining(int days) {
        this.hyperTrainDaysRemaining = days;
        syncData();
    }

    public long getLastHyperTrainDay() { return lastHyperTrainDay; }
    public void setLastHyperTrainDay(long day) {
        this.lastHyperTrainDay = day;
        syncData();
    }

    public UUID getCurrentTrainingUUID() { return currentTrainingUUID; }
    public void setCurrentTrainingUUID(UUID uuid) {
        this.currentTrainingUUID = uuid;
        syncData();
    }

    private void syncData() {
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
        tag.putInt("HyperTrainDaysRemaining", hyperTrainDaysRemaining);
        tag.putLong("LastHyperTrainDay", lastHyperTrainDay);
        if (currentTrainingUUID != null) {
            tag.putUUID("CurrentTrainingUUID", currentTrainingUUID);
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("TargetStat")) this.targetStat = tag.getString("TargetStat");
        if (tag.contains("HyperTrain")) this.hyperTrain = tag.getBoolean("HyperTrain");
        if (tag.contains("HyperTrainDaysRemaining")) this.hyperTrainDaysRemaining = tag.getInt("HyperTrainDaysRemaining");
        if (tag.contains("LastHyperTrainDay")) this.lastHyperTrainDay = tag.getLong("LastHyperTrainDay");
        if (tag.hasUUID("CurrentTrainingUUID")) {
            this.currentTrainingUUID = tag.getUUID("CurrentTrainingUUID");
        } else {
            this.currentTrainingUUID = null;
        }
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider provider) {
        net.minecraft.nbt.CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);
        return tag;
    }
}