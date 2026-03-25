package com.ogtenzohd.cmoncol.colony.buildings.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.ogtenzohd.cmoncol.colony.buildings.moduleviews.ScienceLabJournalProxyModuleView;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ScienceLabJournalWindow extends AbstractModuleWindow<ScienceLabJournalProxyModuleView> {

    private final ScienceLabJournalProxyModuleView moduleView;
    private int currentPage = 0;
    private final List<String> pages = new ArrayList<>();
    private final ScrollingList journalList;

    public ScienceLabJournalWindow(ScienceLabJournalProxyModuleView moduleView) {
        super(moduleView, ResourceLocation.fromNamespaceAndPath("cmoncol", "gui/window/science_lab_journal.xml"));
        this.moduleView = moduleView;
        
        this.journalList = window.findPaneOfTypeByID("journalList", ScrollingList.class);
        
        this.moduleView.syncFromBlockEntity();
        this.updatePagesList();
        
        if (!this.pages.isEmpty()) {
            this.currentPage = 0;
        }

        registerButton("prevBtn", btn -> {
            if (currentPage > 0) {
                currentPage--;
                renderPage();
            }
        });

        registerButton("nextBtn", btn -> {
            if (currentPage < pages.size() - 1) {
                currentPage++;
                renderPage();
            }
        });
    }

    private void updatePagesList() {
        this.pages.clear();
        
        if (this.moduleView.liveStory != null && !this.moduleView.liveStory.isEmpty()) {
            this.pages.add(this.moduleView.liveStory);
        }
        
        if (this.moduleView.journalEntries != null) {
            for (int i = this.moduleView.journalEntries.size() - 1; i >= 0; i--) {
                this.pages.add(this.moduleView.journalEntries.get(i));
            }
        }
    }

    @Override
    public void onOpened() {
        super.onOpened();
        renderPage();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        
        String oldLiveStory = this.moduleView.liveStory;
        int oldSize = this.moduleView.journalEntries.size();
        
        this.moduleView.syncFromBlockEntity();
        
        if (!this.moduleView.liveStory.equals(oldLiveStory) || this.moduleView.journalEntries.size() != oldSize) {
            updatePagesList();
            
            if (currentPage >= pages.size()) {
                currentPage = Math.max(0, pages.size() - 1);
            }
            renderPage();
        }
    }

    private void renderPage() {
        if (currentPage < 0) currentPage = 0;
        if (currentPage >= pages.size()) currentPage = Math.max(0, pages.size() - 1);

        Text pageLabel = window.findPaneOfTypeByID("pageLabel", Text.class);
        if (pageLabel != null) {
            pageLabel.setText(Component.literal("Page " + (currentPage + 1) + "/" + Math.max(1, pages.size())));
        }

        if (journalList != null) {
            String rawText = pages.isEmpty() ? "No expeditions recorded yet. Send the Scientist to work!" : pages.get(currentPage);
            
            List<String> textLines = wrapText(rawText);

            journalList.setDataProvider(new ScrollingList.DataProvider() {
                @Override
                public int getElementCount() {
                    return textLines.size();
                }

                @Override
                public void updateElement(int index, Pane rowPane) {
                    if (index < 0 || index >= textLines.size()) return;
                    
                    Text text = rowPane.findPaneOfTypeByID("pageText", Text.class);
                    if (text != null) {
                        text.setText(Component.literal(textLines.get(index)));
                    }
                }
            });
        }
    }

    private List<String> wrapText(String text) {
        List<String> lines = new ArrayList<>();
        String[] words = text.replace("\n", " \n ").split(" "); 
        StringBuilder currentLine = new StringBuilder();
        
        net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;

        for (String word : words) {
            if (word.equals("\n")) {
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder();
                continue;
            }

            if (font.width(currentLine + word) < 145) {
                currentLine.append(word).append(" ");
            } else {
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder(word + " ");
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString().trim());
        }
        return lines;
    }
}