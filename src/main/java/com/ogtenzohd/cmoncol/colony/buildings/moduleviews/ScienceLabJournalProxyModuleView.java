package com.ogtenzohd.cmoncol.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.ogtenzohd.cmoncol.colony.buildings.gui.ScienceLabJournalWindow;
import com.ogtenzohd.cmoncol.blocks.custom.sciencelab.ScienceLabBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class ScienceLabJournalProxyModuleView extends AbstractBuildingModuleView {
    
    public List<String> journalEntries = new ArrayList<>();
    public String liveStory = "";

    public ScienceLabJournalProxyModuleView() { super(); }

    @Override public BOWindow getWindow() { return new ScienceLabJournalWindow(this); }
    @Override public Component getDesc() { return Component.literal("Journal"); }

    public void syncFromBlockEntity() {
        if (getBuildingView() != null && Minecraft.getInstance().level != null) {
            BlockPos pos = getBuildingView().getPosition();
            BlockEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
            
            if (be instanceof ScienceLabBlockEntity lab) {
                this.journalEntries = new ArrayList<>(lab.getJournal());
                this.liveStory = lab.getLiveExpeditionStory();
            }
        }
    }

    @Override public void deserialize(RegistryFriendlyByteBuf buf) { }
}