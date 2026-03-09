package com.ogtenzohd.cmoncol.colony.buildings.gui;

import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.controls.Button;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.network.GymChallengePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class GymWindow extends BOWindow {

    private final BlockPos gymPos;

    public GymWindow(BlockPos gymPos) {
        super(ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "gui/window/gym_window.xml"));
        this.gymPos = gymPos;
    }

    @Override
    public void onOpened() {
        super.onOpened();
        
        Button btnBrock = this.findPaneOfTypeByID("btn_brock", Button.class);
        Button btnMisty = this.findPaneOfTypeByID("btn_misty", Button.class);
        Button btnSurge = this.findPaneOfTypeByID("btn_surge", Button.class);
        Button btnErika = this.findPaneOfTypeByID("btn_erika", Button.class);
        Button btnKoga = this.findPaneOfTypeByID("btn_koga", Button.class);
        Button btnSabrina = this.findPaneOfTypeByID("btn_sabrina", Button.class);
        Button btnBlaine = this.findPaneOfTypeByID("btn_blaine", Button.class);
        Button btnGiovanni = this.findPaneOfTypeByID("btn_giovanni", Button.class);

        boolean hasBoulder = com.ogtenzohd.cmoncol.client.ClientBadgeCache.hasBoulderBadge;
        boolean hasCascade = com.ogtenzohd.cmoncol.client.ClientBadgeCache.hasCascadeBadge;
        boolean hasThunder = com.ogtenzohd.cmoncol.client.ClientBadgeCache.hasThunderBadge;
        boolean hasRainbow = com.ogtenzohd.cmoncol.client.ClientBadgeCache.hasRainbowBadge;
        boolean hasSoul = com.ogtenzohd.cmoncol.client.ClientBadgeCache.hasSoulBadge;
        boolean hasMarsh = com.ogtenzohd.cmoncol.client.ClientBadgeCache.hasMarshBadge;
        boolean hasVolcano = com.ogtenzohd.cmoncol.client.ClientBadgeCache.hasVolcanoBadge;

        if (btnBrock != null) {
            btnBrock.setHandler(btn -> startChallenge("brock"));
        }
        
        if (btnMisty != null) {
            btnMisty.setHandler(btn -> startChallenge("misty"));
            btnMisty.setEnabled(hasBoulder); 
        }

        if (btnSurge != null) {
            btnSurge.setHandler(btn -> startChallenge("surge"));
            btnSurge.setEnabled(hasCascade);
        }

        if (btnErika != null) {
            btnErika.setHandler(btn -> startChallenge("erika"));
            btnErika.setEnabled(hasThunder);
        }

        if (btnKoga != null) {
            btnKoga.setHandler(btn -> startChallenge("koga"));
            btnKoga.setEnabled(hasRainbow);
        }

        if (btnSabrina != null) {
            btnSabrina.setHandler(btn -> startChallenge("sabrina"));
            btnSabrina.setEnabled(hasSoul);
        }

        if (btnBlaine != null) {
            btnBlaine.setHandler(btn -> startChallenge("blaine"));
            btnBlaine.setEnabled(hasMarsh);
        }

        if (btnGiovanni != null) {
            btnGiovanni.setHandler(btn -> startChallenge("giovanni"));
            btnGiovanni.setEnabled(hasVolcano);
        }
    }

    private void startChallenge(String leaderName) {
        PacketDistributor.sendToServer(new GymChallengePacket(this.gymPos, leaderName));
        net.minecraft.client.Minecraft.getInstance().setScreen(null); 
    }
}