package com.ogtenzohd.cmoncol.colony.buildings.gui;

import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.ogtenzohd.cmoncol.colony.buildings.moduleviews.PastureProxyModuleView;
import com.ogtenzohd.cmoncol.network.CmoncolPackets;
import com.ogtenzohd.cmoncol.network.ProxyActionPacket;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PastureProxyWindow extends AbstractModuleWindow<PastureProxyModuleView> {
    
    private int selectedPartySlot = 0;
    private final PastureProxyModuleView view;

    public PastureProxyWindow(PastureProxyModuleView moduleView) {
        super(moduleView, ResourceLocation.fromNamespaceAndPath("cmoncol", "gui/window/pasture_proxy.xml"));
        this.view = moduleView;
        refreshUI();

        registerButton("prevPartyBtn", btn -> { selectedPartySlot = (selectedPartySlot + 5) % 6; updatePartyLabel(); });
        registerButton("nextPartyBtn", btn -> { selectedPartySlot = (selectedPartySlot + 1) % 6; updatePartyLabel(); });

        registerButton("depositBtn", btn -> {
            BlockPos pos = moduleView.getTargetPos();
            if (pos != null) { CmoncolPackets.sendToServer(new ProxyActionPacket(pos, 0, selectedPartySlot)); }
        });

        for (int i = 0; i < 10; i++) {
            final int index = i;
            registerButton("withdrawBtn" + i, btn -> {
                BlockPos pos = moduleView.getTargetPos();
                if (pos != null) { CmoncolPackets.sendToServer(new ProxyActionPacket(pos, 1, index)); }
            });
            registerButton("toggleBtn" + i, btn -> {
                BlockPos pos = moduleView.getTargetPos();
                if (pos != null) { CmoncolPackets.sendToServer(new ProxyActionPacket(pos, 7, index)); }
            });
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getGameTime() % 20 == 0) {
            refreshUI();
        }
    }

    private void refreshUI() {
        view.syncFromBlockEntity();
        for (int i = 0; i < 10; i++) {
            Text label = window.findPaneOfTypeByID("slotLabel" + i, Text.class);
            Button withdrawBtn = window.findPaneOfTypeByID("withdrawBtn" + i, Button.class);
            Button toggleBtn = window.findPaneOfTypeByID("toggleBtn" + i, Button.class);
            
            if (label != null && withdrawBtn != null && toggleBtn != null) {
                if (i >= view.maxSlots) {
                    label.setVisible(false);
                    withdrawBtn.setVisible(false);
                    toggleBtn.setVisible(false);
                } else {
                    label.setVisible(true);
                    withdrawBtn.setVisible(true);
                    toggleBtn.setVisible(true);
                    
                    if (i < view.slotNames.size()) {
                        label.setText(Component.literal(view.slotNames.get(i)));
                        withdrawBtn.setEnabled(true);
                        
                        toggleBtn.setText(Component.literal(view.slotModes.get(i)));
                        toggleBtn.setEnabled(view.slotToggleable.get(i));
                    } else {
                        label.setText(Component.literal("Empty"));
                        withdrawBtn.setEnabled(false);
                        toggleBtn.setText(Component.literal("-"));
                        toggleBtn.setEnabled(false);
                    }
                }
            }
        }
        updatePartyLabel();
    }

    private void updatePartyLabel() {
        Text partyLabel = window.findPaneOfTypeByID("partySelectionLabel", Text.class);
        if (partyLabel != null) {
            partyLabel.setText(Component.literal("Slot " + (selectedPartySlot + 1) + ": " + getPartyPokemonName(selectedPartySlot)));
        }
    }

    private String getPartyPokemonName(int index) {
        try {
            var player = Minecraft.getInstance().player;
            var level = Minecraft.getInstance().level;
            if (player != null && level != null) {
                var party = com.cobblemon.mod.common.Cobblemon.INSTANCE.getStorage().getParty(player.getUUID(), level.registryAccess());
                var p = party.get(index);
                if (p != null) return p.getDisplayName(true).getString();
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return "Empty";
    }
}