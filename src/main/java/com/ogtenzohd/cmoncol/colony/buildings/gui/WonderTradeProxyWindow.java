package com.ogtenzohd.cmoncol.colony.buildings.gui;

import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.Button;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.ogtenzohd.cmoncol.colony.buildings.moduleviews.WonderTradeProxyModuleView;
import com.ogtenzohd.cmoncol.network.CmoncolPackets;
import com.ogtenzohd.cmoncol.network.ProxyActionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class WonderTradeProxyWindow extends AbstractModuleWindow<WonderTradeProxyModuleView> {
    
    private int selectedPartySlot = 0;
    private final WonderTradeProxyModuleView view;

    public WonderTradeProxyWindow(WonderTradeProxyModuleView moduleView) {
        super(moduleView, ResourceLocation.fromNamespaceAndPath("cmoncol", "gui/window/wonder_trade_proxy.xml"));
        this.view = moduleView;
        moduleView.syncFromBlockEntity();
        
        updateLabels();

        registerButton("prevPartyBtn", btn -> {
            selectedPartySlot = (selectedPartySlot + 5) % 6;
            updateLabels();
        });
        
        registerButton("nextPartyBtn", btn -> {
            selectedPartySlot = (selectedPartySlot + 1) % 6;
            updateLabels();
        });

        registerButton("depositBtn", btn -> {
            BlockPos pos = moduleView.getTargetPos();
            if (pos != null) { CmoncolPackets.sendToServer(new ProxyActionPacket(pos, 10, selectedPartySlot)); }
        });
        
        registerButton("claimBtn", btn -> {
            BlockPos pos = moduleView.getTargetPos();
            if (pos != null) { CmoncolPackets.sendToServer(new ProxyActionPacket(pos, 11, 0)); }
        });
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getGameTime() % 5 == 0) {
            view.syncFromBlockEntity();
            updateLabels();
        }
    }

    private void updateLabels() {
        Text partyLabel = window.findPaneOfTypeByID("partySelectionLabel", Text.class);
        if (partyLabel != null) {
            String name = getPartyPokemonName(selectedPartySlot);
            partyLabel.setText(Component.literal("Slot " + (selectedPartySlot + 1) + ": " + name));
        }

        Text statusLabel = window.findPaneOfTypeByID("statusLabel", Text.class);
        Button claimBtn = window.findPaneOfTypeByID("claimBtn", Button.class);
        Button depositBtn = window.findPaneOfTypeByID("depositBtn", Button.class);

        if (view.hasReadyPokemon) {
            statusLabel.setText(Component.literal("§aStatus: Trade Complete!"));
            if(claimBtn != null) claimBtn.enable();
            if(depositBtn != null) depositBtn.disable();
        } else if (view.hasDepositedPokemon) {
            int secondsLeft = view.tradeTimer / 20;
            boolean isVip = false;
            if (Minecraft.getInstance().player != null) {
                isVip = com.ogtenzohd.cmoncol.util.CmoncolPerks.hasVIPPerks(Minecraft.getInstance().player.getUUID());
            }

            String prefix;
            if (isVip && view.isBoosted) {
                prefix = "§6VIP Boosted Trade: ";
            } else if (isVip) {
                prefix = "§2VIP Trade: ";
            } else if (view.isBoosted) {
                prefix = "§dBoosted Trade: ";
            } else {
                prefix = "§8Trading: ";
            }

            statusLabel.setText(Component.literal(prefix + secondsLeft + "s"));

            if(claimBtn != null) claimBtn.disable();
            if(depositBtn != null) depositBtn.disable();
        } else {
            statusLabel.setText(Component.literal("§8Status: Waiting for Pokemon"));
            if(claimBtn != null) claimBtn.disable();
            if(depositBtn != null) depositBtn.enable();
        }
    }

    private String getPartyPokemonName(int index) {
        try {
            net.minecraft.world.entity.player.Player player = Minecraft.getInstance().player;
            net.minecraft.client.multiplayer.ClientLevel level = Minecraft.getInstance().level;
            if (player != null && level != null) {
                com.cobblemon.mod.common.api.storage.party.PlayerPartyStore party = 
                    com.cobblemon.mod.common.Cobblemon.INSTANCE.getStorage().getParty(player.getUUID(), level.registryAccess());
                com.cobblemon.mod.common.pokemon.Pokemon p = party.get(index);
                if (p != null) {
                    return p.getDisplayName(true).getString();
                }
            }
        } catch(Exception e) {
        }
        return "Empty";
    }
}