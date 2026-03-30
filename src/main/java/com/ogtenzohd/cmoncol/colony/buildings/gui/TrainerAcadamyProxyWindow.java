package com.ogtenzohd.cmoncol.colony.buildings.gui;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.ogtenzohd.cmoncol.colony.buildings.moduleviews.TrainerAcadamyProxyModuleView;
import com.ogtenzohd.cmoncol.network.CmoncolPackets;
import com.ogtenzohd.cmoncol.network.ProxyActionPacket;
import com.ogtenzohd.cmoncol.network.UpdateAcadamySettingsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TrainerAcadamyProxyWindow extends AbstractModuleWindow<TrainerAcadamyProxyModuleView> {
    private int statIndex = 0;
    private int selectedPartySlot = 0;
    private final TrainerAcadamyProxyModuleView view;
    private static final String[] STATS = {"hp", "attack", "defense", "spatk", "spdef", "speed"};
    
    public TrainerAcadamyProxyWindow(TrainerAcadamyProxyModuleView moduleView) {
        super(moduleView, ResourceLocation.fromNamespaceAndPath("cmoncol", "gui/window/trainer_acadamy_proxy.xml"));
        this.view = moduleView;
        moduleView.syncFromBlockEntity();
        
        for (int i = 0; i < STATS.length; i++) { 
            if (STATS[i].equalsIgnoreCase(moduleView.currentStat)) statIndex = i; 
        }

        Text pokeLabel = window.findPaneOfTypeByID("pokemonStatusLabel", Text.class);
        if (pokeLabel != null) pokeLabel.setText(Component.literal(moduleView.storedPokemonName));

        Button statBtn = window.findPaneOfTypeByID("statBtn", Button.class);
        if (statBtn != null) statBtn.setText(Component.literal(STATS[statIndex].toUpperCase()));

        Button hyperBtn = window.findPaneOfTypeByID("hyperBtn", Button.class);
        if (hyperBtn != null) hyperBtn.setText(Component.literal(moduleView.currentHyper ? "ON" : "OFF"));

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
            if(pos != null) CmoncolPackets.sendToServer(new ProxyActionPacket(pos, 4, selectedPartySlot));
        });
        
        registerButton("withdrawBtn", btn -> {
            BlockPos pos = moduleView.getTargetPos();
            if(pos != null) CmoncolPackets.sendToServer(new ProxyActionPacket(pos, 5, 0));
        });

        registerButton("statBtn", btn -> {
            statIndex = (statIndex + 1) % STATS.length;
            btn.setText(Component.literal(STATS[statIndex].toUpperCase()));
        });
        
        registerButton("hyperBtn", btn -> {
            moduleView.currentHyper = !moduleView.currentHyper;
            btn.setText(Component.literal(moduleView.currentHyper ? "ON" : "OFF"));
        });
        
        registerButton("saveBtn", btn -> {
            BlockPos pos = moduleView.getTargetPos();
            if(pos != null) CmoncolPackets.sendToServer(new UpdateAcadamySettingsPacket(pos, STATS[statIndex], moduleView.currentHyper));
        });
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getGameTime() % 10 == 0) {
            view.syncFromBlockEntity();
            
            Text pokeLabel = window.findPaneOfTypeByID("pokemonStatusLabel", Text.class);
            if (pokeLabel != null) pokeLabel.setText(Component.literal(view.storedPokemonName));

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