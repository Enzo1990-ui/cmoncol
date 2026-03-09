package com.ogtenzohd.cmoncol.colony.buildings.gui;

import com.ldtteam.blockui.controls.Text;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.ogtenzohd.cmoncol.colony.buildings.moduleviews.DaycareProxyModuleView;
import com.ogtenzohd.cmoncol.network.CmoncolPackets;
import com.ogtenzohd.cmoncol.network.ProxyActionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class DaycareProxyWindow extends AbstractModuleWindow<DaycareProxyModuleView> {
    
    private int selectedPartySlot = 0;
    private final DaycareProxyModuleView view;

    public DaycareProxyWindow(DaycareProxyModuleView moduleView) {
        super(moduleView, ResourceLocation.fromNamespaceAndPath("cmoncol", "gui/window/daycare_proxy.xml"));
        this.view = moduleView;
        moduleView.syncFromBlockEntity();
        
        Text s1Label = window.findPaneOfTypeByID("slot1Label", Text.class);
        Text s2Label = window.findPaneOfTypeByID("slot2Label", Text.class);
        
        if (s1Label != null) s1Label.setText(Component.literal(moduleView.slot1Text));
        if (s2Label != null) s2Label.setText(Component.literal(moduleView.slot2Text));
        
        updatePartyLabel();

        registerButton("prevPartyBtn", btn -> {
            selectedPartySlot = (selectedPartySlot + 5) % 6;
            updatePartyLabel();
        });
        
        registerButton("nextPartyBtn", btn -> {
            selectedPartySlot = (selectedPartySlot + 1) % 6;
            updatePartyLabel();
        });

        registerButton("depositBtn", btn -> {
            BlockPos pos = moduleView.getTargetPos();
            if (pos != null) { CmoncolPackets.sendToServer(new ProxyActionPacket(pos, 2, selectedPartySlot)); }
        });
        
        registerButton("withdraw1Btn", btn -> {
            BlockPos pos = moduleView.getTargetPos();
            if (pos != null) { CmoncolPackets.sendToServer(new ProxyActionPacket(pos, 3, 0)); }
        });
        
        registerButton("withdraw2Btn", btn -> {
            BlockPos pos = moduleView.getTargetPos();
            if (pos != null) { CmoncolPackets.sendToServer(new ProxyActionPacket(pos, 6, 0)); }
        });
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getGameTime() % 10 == 0) {
            view.syncFromBlockEntity();
            
            Text s1Label = window.findPaneOfTypeByID("slot1Label", Text.class);
            Text s2Label = window.findPaneOfTypeByID("slot2Label", Text.class);
            
            if (s1Label != null) s1Label.setText(Component.literal(view.slot1Text));
            if (s2Label != null) s2Label.setText(Component.literal(view.slot2Text));
            
            updatePartyLabel();
        }
    }

    private void updatePartyLabel() {
        Text partyLabel = window.findPaneOfTypeByID("partySelectionLabel", Text.class);
        if (partyLabel != null) {
            String name = getPartyPokemonName(selectedPartySlot);
            partyLabel.setText(Component.literal("Slot " + (selectedPartySlot + 1) + ": " + name));
        }
    }

    private String getPartyPokemonName(int index) {
        try {
            net.minecraft.world.entity.player.Player player = Minecraft.getInstance().player;
            net.minecraft.client.multiplayer.ClientLevel level = Minecraft.getInstance().level;
            if (player != null && level != null) {
                com.cobblemon.mod.common.api.storage.party.PlayerPartyStore party = 
                    com.cobblemon.mod.common.Cobblemon.INSTANCE.getStorage().getParty(player.getUUID(), level.registryAccess());
                if (party != null) {
                    com.cobblemon.mod.common.pokemon.Pokemon p = party.get(index);
                    if (p != null) {
                        return p.getDisplayName(true).getString(); 
                    }
                }
            }
        } catch(Exception e) {}
        return "Empty";
    }
}