package com.ogtenzohd.cmoncol.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.ogtenzohd.cmoncol.colony.buildings.gui.ScienceLabWindow;
import com.ogtenzohd.cmoncol.blocks.custom.sciencelab.ScienceLabBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ScienceLabProxyModuleView extends AbstractBuildingModuleView {
    
    public String currentSite = "Loading...";
    public boolean isExpeditionActive = false;

    public ScienceLabProxyModuleView() { super(); }

    @Override public BOWindow getWindow() { return new ScienceLabWindow(this); }
    @Override public Component getDesc() { return Component.literal("Science Lab Settings"); }

    public BlockPos getTargetPos() {
        if (getBuildingView() != null) {
            return getBuildingView().getPosition();
        }
        return null;
    }

    public void syncFromBlockEntity() {
        BlockPos pos = getTargetPos();
        if (pos != null) {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof ScienceLabBlockEntity lab) {
                    this.currentSite = lab.getDigSiteName();
                    this.isExpeditionActive = lab.isExpeditionActive();
                }
            }
        }
    }

    @Override public void deserialize(RegistryFriendlyByteBuf buf) { }
}