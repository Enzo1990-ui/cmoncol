package com.ogtenzohd.cmoncol.blocks.custom.sciencelab;

import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import com.ogtenzohd.cmoncol.util.ScienceLabLootTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.List;

public class ScienceLabBlockEntity extends TileEntityColonyBuilding {
    
    private int digSiteIndex = 0;
    private boolean expeditionActive = false;
    private int totalExpeditions = 0;
    
    private String liveExpeditionStory = ""; 
    
    private final LinkedList<String> journal = new LinkedList<>();

    public ScienceLabBlockEntity(BlockPos pos, BlockState state) {
        super(CmoncolReg.SCIENCELAB_BE.get(), pos, state);
    }

    public String getDigSiteName() {
        if (ScienceLabLootTable.SITE_NAMES.isEmpty()) return "Unknown";
        if (digSiteIndex < 0 || digSiteIndex >= ScienceLabLootTable.SITE_NAMES.size()) {
            digSiteIndex = 0;
        }
        return ScienceLabLootTable.SITE_NAMES.get(digSiteIndex);
    }
    
    public void cycleDigSite() {
        if (ScienceLabLootTable.SITE_NAMES.isEmpty()) return;
        digSiteIndex = (digSiteIndex + 1) % ScienceLabLootTable.SITE_NAMES.size();
        this.setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    public boolean isExpeditionActive() { return expeditionActive; }
    
    public void setExpeditionActive(boolean active) {
        this.expeditionActive = active;
        if (active) this.totalExpeditions++; 
        this.setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    public int getTotalExpeditions() { return totalExpeditions; }

    public String getLiveExpeditionStory() { return liveExpeditionStory; }

    public void updateLiveStory(String currentStory) {
        this.liveExpeditionStory = currentStory;
        this.setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    public void clearLiveStory() {
        this.liveExpeditionStory = "";
        this.setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    public void addJournalEntry(String fullExpeditionStory) {
        journal.addFirst(fullExpeditionStory);
        if (journal.size() > 10) { 
            journal.removeLast();
        }
        this.setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    public List<String> getJournal() {
        return journal;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("CmoncolDigSiteIdx", digSiteIndex);
        tag.putBoolean("ExpeditionActive", expeditionActive);
        tag.putInt("TotalExpeditions", totalExpeditions);
        tag.putString("LiveStory", liveExpeditionStory); 
        
        ListTag journalList = new ListTag();
        for (String s : journal) {
            journalList.add(StringTag.valueOf(s));
        }
        tag.put("SciJournal", journalList);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.digSiteIndex = tag.getInt("CmoncolDigSiteIdx");
        this.expeditionActive = tag.getBoolean("ExpeditionActive");
        this.totalExpeditions = tag.getInt("TotalExpeditions");
        
        if (tag.contains("LiveStory")) {
            this.liveExpeditionStory = tag.getString("LiveStory");
        }
        
        journal.clear();
        if (tag.contains("SciJournal")) {
            ListTag journalList = tag.getList("SciJournal", Tag.TAG_STRING);
            for (int i = 0; i < journalList.size(); i++) {
                journal.add(journalList.getString(i));
            }
        }
    }
}