package com.ogtenzohd.cmoncol.colony.buildings.gui;

import com.ldtteam.blockui.controls.Text;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.ogtenzohd.cmoncol.colony.buildings.moduleviews.PokemonGuardProxyModuleView;
import com.ogtenzohd.cmoncol.network.CmoncolPackets;
import com.ogtenzohd.cmoncol.network.UpdateGuardSettingsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class PokemonGuardProxyWindow extends AbstractModuleWindow<PokemonGuardProxyModuleView> {
    private final PokemonGuardProxyModuleView view;
    private final List<String> availablePokemon = new ArrayList<>();

    private int currentPokemonIndex = 0;
    private int selectedGuardIndex = 0;

    public PokemonGuardProxyWindow(PokemonGuardProxyModuleView moduleView) {
        super(moduleView, ResourceLocation.fromNamespaceAndPath("cmoncol", "gui/window/pokemon_guard_proxy.xml"));
        this.view = moduleView;
        moduleView.syncFromBlockEntity();

        populateAvailablePokemon(moduleView.buildingLevel);
        refreshSelectionForNewGuard();
        updateLabels();

        registerButton("prevGuardBtn", btn -> {
            if (!view.guardIds.isEmpty()) {
                selectedGuardIndex = (selectedGuardIndex - 1 + view.guardIds.size()) % view.guardIds.size();
                refreshSelectionForNewGuard();
            }
        });

        registerButton("nextGuardBtn", btn -> {
            if (!view.guardIds.isEmpty()) {
                selectedGuardIndex = (selectedGuardIndex + 1) % view.guardIds.size();
                refreshSelectionForNewGuard();
            }
        });

        registerButton("prevBtn", btn -> {
            if (!availablePokemon.isEmpty() && !view.guardIds.isEmpty()) {
                currentPokemonIndex = (currentPokemonIndex - 1 + availablePokemon.size()) % availablePokemon.size();
                updateLabels();
            }
        });

        registerButton("nextBtn", btn -> {
            if (!availablePokemon.isEmpty() && !view.guardIds.isEmpty()) {
                currentPokemonIndex = (currentPokemonIndex + 1) % availablePokemon.size();
                updateLabels();
            }
        });

        registerButton("saveBtn", btn -> {
            BlockPos pos = moduleView.getTargetPos();
            if (pos != null && !availablePokemon.isEmpty() && !view.guardIds.isEmpty()) {
                int citizenId = view.guardIds.get(selectedGuardIndex);
                String selected = availablePokemon.get(currentPokemonIndex);

                CmoncolPackets.sendToServer(new UpdateGuardSettingsPacket(pos, citizenId, selected));
                view.assignments.put(citizenId, selected);
                updateLabels();
            }
        });
    }

    private void refreshSelectionForNewGuard() {
        if (view.guardIds.isEmpty()) return;
        int citizenId = view.guardIds.get(selectedGuardIndex);
        String currentSaved = view.assignments.getOrDefault(citizenId, "growlithe");
        currentPokemonIndex = Math.max(0, availablePokemon.indexOf(currentSaved));
        updateLabels();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getGameTime() % 10 == 0) {
            int previousLevel = view.buildingLevel;
            int previousGuardCount = view.guardIds.size();

            view.syncFromBlockEntity();

            if (previousLevel != view.buildingLevel || previousGuardCount != view.guardIds.size()) {
                populateAvailablePokemon(view.buildingLevel);
                selectedGuardIndex = 0;
                refreshSelectionForNewGuard();
            }
            updateLabels();
        }
    }

    private void populateAvailablePokemon(int level) {
        availablePokemon.clear();
        if (level == 1) { availablePokemon.add("growlithe"); }
        else if (level == 2) { availablePokemon.add("growlithe"); availablePokemon.add("pikachu"); availablePokemon.add("corphish"); }
        else if (level == 3) { availablePokemon.add("growlithe"); availablePokemon.add("pikachu"); availablePokemon.add("corphish"); availablePokemon.add("weepinbell"); availablePokemon.add("scyther"); availablePokemon.add("croagunk"); }
        else if (level == 4) { availablePokemon.add("arcanine"); availablePokemon.add("pikachu"); availablePokemon.add("corphish"); availablePokemon.add("victreebel"); availablePokemon.add("scyther"); availablePokemon.add("croagunk"); availablePokemon.add("sealeo"); availablePokemon.add("magneton"); }
        else if (level >= 5) { availablePokemon.add("arcanine"); availablePokemon.add("raichu"); availablePokemon.add("crawdaunt"); availablePokemon.add("victreebel"); availablePokemon.add("scizor"); availablePokemon.add("toxicroak"); availablePokemon.add("walrein"); availablePokemon.add("magnezone"); availablePokemon.add("stoutland"); availablePokemon.add("gigalith"); }
    }

    private void updateLabels() {
        Text guardSlotLabel = window.findPaneOfTypeByID("guardSlotLabel", Text.class);
        Text currentLabel = window.findPaneOfTypeByID("currentSelectionLabel", Text.class);
        Text previewLabel = window.findPaneOfTypeByID("previewLabel", Text.class);

        if (view.guardIds.isEmpty()) {
            if (guardSlotLabel != null) guardSlotLabel.setText(Component.literal("Editing: No Guards Hired"));
            if (currentLabel != null) currentLabel.setText(Component.literal("Current: None"));
            if (previewLabel != null) previewLabel.setText(Component.literal("None"));
        } else {
            int citizenId = view.guardIds.get(selectedGuardIndex);
            String citizenName = view.guardNames.getOrDefault(citizenId, "Unknown");

            if (guardSlotLabel != null) guardSlotLabel.setText(Component.literal("Editing: " + citizenName));

            if (currentLabel != null) {
                String savedPartner = view.assignments.getOrDefault(citizenId, "growlithe");
                currentLabel.setText(Component.literal("Current: " + capitalize(savedPartner)));
            }
            if (previewLabel != null && !availablePokemon.isEmpty()) {
                previewLabel.setText(Component.literal(capitalize(availablePokemon.get(currentPokemonIndex))));
            }
        }

        Text levelLabel = window.findPaneOfTypeByID("levelLabel", Text.class);
        if (levelLabel != null) levelLabel.setText(Component.literal("Building Level: " + view.buildingLevel));
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}