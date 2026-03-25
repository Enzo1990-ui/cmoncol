package com.ogtenzohd.cmoncol.colony.buildings.gui;

import com.ldtteam.blockui.controls.Text;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.ogtenzohd.cmoncol.colony.buildings.moduleviews.PokemartProxyModuleView;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PokemartProxyWindow extends AbstractModuleWindow<PokemartProxyModuleView> {

    public PokemartProxyWindow(PokemartProxyModuleView moduleView) {
        super(moduleView, ResourceLocation.fromNamespaceAndPath("cmoncol", "gui/window/pokemart_proxy.xml"));
        moduleView.syncFromBlockEntity();
        
        Text levelLabel = window.findPaneOfTypeByID("levelLabel", Text.class);
        Text unlocksLabel = window.findPaneOfTypeByID("unlocksLabel", Text.class);
        
        int bldLevel = moduleView.getBuildingLevel();
        if (levelLabel != null) {
            levelLabel.setText(Component.literal("Mart Level: " + bldLevel));
        }
        
        if (unlocksLabel != null) {
            String unlocks = "Lv 1: Poke Ball, Potion";
            if(bldLevel >= 2) unlocks = "Lv 2: Great Ball, Super Potion, Heals...";
            if(bldLevel >= 3) unlocks = "Lv 3: Hyper Potion, Revive, Super Repel...";
            if(bldLevel >= 4) unlocks = "Lv 4: Ultra Ball, Max Repel, Full Heal";
            if(bldLevel >= 5) unlocks = "Lv 5: Full Restore, Max Potion";
            
            unlocksLabel.setText(Component.literal("Highest Unlocks: \n" + unlocks));
        }
    }
}