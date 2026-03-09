package com.ogtenzohd.cmoncol.colony.buildings.gui;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.ogtenzohd.cmoncol.colony.buildings.moduleviews.ScienceLabProxyModuleView;
import com.ogtenzohd.cmoncol.blocks.custom.sciencelab.ScienceLabBlockEntity;
import com.ogtenzohd.cmoncol.network.CmoncolPackets;
import com.ogtenzohd.cmoncol.network.UpdateDigSitePacket;
import com.ogtenzohd.cmoncol.network.ProxyActionPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ScienceLabWindow extends AbstractModuleWindow<ScienceLabProxyModuleView> {

    public ScienceLabWindow(ScienceLabProxyModuleView moduleView) {
        super(moduleView, ResourceLocation.fromNamespaceAndPath("cmoncol", "gui/window/science_lab_gui.xml"));
        
        moduleView.syncFromBlockEntity();
        updateSiteLabel(moduleView.currentSite);

        registerButton("cycleBtn", btn -> {
            BlockPos pos = moduleView.getTargetPos();
            if (pos != null) {
                CmoncolPackets.sendToServer(new UpdateDigSitePacket(pos));
                Level level = Minecraft.getInstance().level;
                if (level != null) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof ScienceLabBlockEntity lab) {
                        lab.cycleDigSite(); 
                        updateSiteLabel(lab.getDigSiteName()); 
                    }
                }
            }
        });

        Button startBtn = window.findPaneOfTypeByID("startBtn", Button.class);
        Button cancelBtn = window.findPaneOfTypeByID("cancelBtn", Button.class);

        if (startBtn != null && cancelBtn != null) {
            startBtn.setEnabled(!moduleView.isExpeditionActive);
            cancelBtn.setEnabled(moduleView.isExpeditionActive);

            startBtn.setHandler(btn -> {
                BlockPos pos = moduleView.getTargetPos();
                if (pos != null) {
                    CmoncolPackets.sendToServer(new ProxyActionPacket(pos, 8, 0));
                    startBtn.setEnabled(false);
                    cancelBtn.setEnabled(true);
                    moduleView.isExpeditionActive = true;
                }
            });

            cancelBtn.setHandler(btn -> {
                BlockPos pos = moduleView.getTargetPos();
                if (pos != null) {
                    CmoncolPackets.sendToServer(new ProxyActionPacket(pos, 9, 0));
                    startBtn.setEnabled(true);
                    cancelBtn.setEnabled(false);
                    moduleView.isExpeditionActive = false;
                }
            });
        }
    }

    private void updateSiteLabel(String text) {
        Text siteLabel = window.findPaneOfTypeByID("siteLabel", Text.class);
        if (siteLabel != null) {
            siteLabel.setText(Component.literal(text));
        }
    }
}