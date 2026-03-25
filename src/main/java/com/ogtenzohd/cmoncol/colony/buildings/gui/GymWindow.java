package com.ogtenzohd.cmoncol.colony.buildings.gui;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.View;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.network.GymChallengePacket;
import com.ogtenzohd.cmoncol.client.ClientBadgeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class GymWindow extends BOWindow {

    private final BlockPos gymPos;
    private final int buildingLevel;

    public GymWindow(BlockPos gymPos) {
        super(ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "gui/window/gym_window.xml"));
        this.gymPos = gymPos;

        int level = 1;

        net.minecraft.world.level.Level mcLevel = net.minecraft.client.Minecraft.getInstance().level;
        if (mcLevel != null) {
            net.minecraft.world.level.block.entity.BlockEntity be = mcLevel.getBlockEntity(gymPos);
            if (be instanceof com.ogtenzohd.cmoncol.blocks.custom.gym.GymBlockEntity gym) {
                level = gym.getSyncedBuildingLevel();
            }
        }

        this.buildingLevel = level;
    }

    @Override
    public void onOpened() {
        super.onOpened();

        View[] pages = new View[] {
                findPaneOfTypeByID("page_kanto", View.class), findPaneOfTypeByID("page_johto", View.class),
                findPaneOfTypeByID("page_hoenn", View.class), findPaneOfTypeByID("page_sinnoh", View.class),
                findPaneOfTypeByID("page_unova", View.class), findPaneOfTypeByID("page_kalos", View.class),
                findPaneOfTypeByID("page_galar", View.class), findPaneOfTypeByID("page_paldea", View.class)
        };

        Button[] tabs = new Button[] {
                findPaneOfTypeByID("tab_kanto", Button.class), findPaneOfTypeByID("tab_johto", Button.class),
                findPaneOfTypeByID("tab_hoenn", Button.class), findPaneOfTypeByID("tab_sinnoh", Button.class),
                findPaneOfTypeByID("tab_unova", Button.class), findPaneOfTypeByID("tab_kalos", Button.class),
                findPaneOfTypeByID("tab_galar", Button.class), findPaneOfTypeByID("tab_paldea", Button.class)
        };

        int[] requiredLevels = new int[] { 1, 2, 3, 3, 4, 4, 5, 5 };

        for (int i = 0; i < tabs.length; i++) {
            Button tab = tabs[i];
            View targetPage = pages[i];

            if (tab == null || targetPage == null) continue;

            if (i != 0) targetPage.hide();

            if (this.buildingLevel >= requiredLevels[i]) {
                tab.setHandler(btn -> switchTab(pages, targetPage));
            } else {
                tab.setEnabled(false);
            }
        }

        setupKantoRoster();
        setupJohtoRoster();
        setupHoennRoster();
        setupSinnohRoster();
        setupUnovaRoster();
        setupKalosRoster();
        setupGalarRoster();
        setupPaldeaRoster();
        setupSupporter();
    }

    private void switchTab(View[] allPages, View activePage) {
        for (View page : allPages) {
            if (page != null) {
                if (page == activePage) page.show();
                else page.hide();
            }
        }
    }

    private void setupKantoRoster() {
        Button btnBrock = this.findPaneOfTypeByID("btn_brock", Button.class);
        Button btnMisty = this.findPaneOfTypeByID("btn_misty", Button.class);
        Button btnSurge = this.findPaneOfTypeByID("btn_surge", Button.class);
        Button btnErika = this.findPaneOfTypeByID("btn_erika", Button.class);
        Button btnKoga = this.findPaneOfTypeByID("btn_koga", Button.class);
        Button btnSabrina = this.findPaneOfTypeByID("btn_sabrina", Button.class);
        Button btnBlaine = this.findPaneOfTypeByID("btn_blaine", Button.class);
        Button btnGiovanni = this.findPaneOfTypeByID("btn_giovanni", Button.class);

        if (btnBrock != null) btnBrock.setHandler(btn -> startChallenge("brock"));
        if (btnMisty != null) { btnMisty.setHandler(btn -> startChallenge("misty")); btnMisty.setEnabled(ClientBadgeCache.hasBoulderBadge); }
        if (btnSurge != null) { btnSurge.setHandler(btn -> startChallenge("surge")); btnSurge.setEnabled(ClientBadgeCache.hasCascadeBadge); }
        if (btnErika != null) { btnErika.setHandler(btn -> startChallenge("erika")); btnErika.setEnabled(ClientBadgeCache.hasThunderBadge); }
        if (btnKoga != null) { btnKoga.setHandler(btn -> startChallenge("koga")); btnKoga.setEnabled(ClientBadgeCache.hasRainbowBadge); }
        if (btnSabrina != null) { btnSabrina.setHandler(btn -> startChallenge("sabrina")); btnSabrina.setEnabled(ClientBadgeCache.hasSoulBadge); }
        if (btnBlaine != null) { btnBlaine.setHandler(btn -> startChallenge("blaine")); btnBlaine.setEnabled(ClientBadgeCache.hasMarshBadge); }
        if (btnGiovanni != null) { btnGiovanni.setHandler(btn -> startChallenge("giovanni")); btnGiovanni.setEnabled(ClientBadgeCache.hasVolcanoBadge); }
    }

    private void setupJohtoRoster() {
        Button btnFalkner = this.findPaneOfTypeByID("btn_falkner", Button.class);
        Button btnBugsy = this.findPaneOfTypeByID("btn_bugsy", Button.class);
        Button btnWhitney = this.findPaneOfTypeByID("btn_whitney", Button.class);
        Button btnMorty = this.findPaneOfTypeByID("btn_morty", Button.class);
        Button btnChuck = this.findPaneOfTypeByID("btn_chuck", Button.class);
        Button btnJasmine = this.findPaneOfTypeByID("btn_jasmine", Button.class);
        Button btnPryce = this.findPaneOfTypeByID("btn_pryce", Button.class);
        Button btnClair = this.findPaneOfTypeByID("btn_clair", Button.class);

        if (btnFalkner != null) btnFalkner.setHandler(btn -> startChallenge("falkner"));
        if (btnBugsy != null) { btnBugsy.setHandler(btn -> startChallenge("bugsy")); btnBugsy.setEnabled(ClientBadgeCache.hasZephyrBadge); }
        if (btnWhitney != null) { btnWhitney.setHandler(btn -> startChallenge("whitney")); btnWhitney.setEnabled(ClientBadgeCache.hasHiveBadge); }
        if (btnMorty != null) { btnMorty.setHandler(btn -> startChallenge("morty")); btnMorty.setEnabled(ClientBadgeCache.hasPlainBadge); }
        if (btnChuck != null) { btnChuck.setHandler(btn -> startChallenge("chuck")); btnChuck.setEnabled(ClientBadgeCache.hasFogBadge); }
        if (btnJasmine != null) { btnJasmine.setHandler(btn -> startChallenge("jasmine")); btnJasmine.setEnabled(ClientBadgeCache.hasStormBadge); }
        if (btnPryce != null) { btnPryce.setHandler(btn -> startChallenge("pryce")); btnPryce.setEnabled(ClientBadgeCache.hasMineralBadge); }
        if (btnClair != null) { btnClair.setHandler(btn -> startChallenge("clair")); btnClair.setEnabled(ClientBadgeCache.hasGlacierBadge); }
    }

    private void setupHoennRoster() {
        Button btnRoxanne = this.findPaneOfTypeByID("btn_roxanne", Button.class);
        Button btnBrawly = this.findPaneOfTypeByID("btn_brawly", Button.class);
        Button btnWattson = this.findPaneOfTypeByID("btn_wattson", Button.class);
        Button btnFlannery = this.findPaneOfTypeByID("btn_flannery", Button.class);
        Button btnNorman = this.findPaneOfTypeByID("btn_norman", Button.class);
        Button btnWinona = this.findPaneOfTypeByID("btn_winona", Button.class);
        Button btnTateLiza = this.findPaneOfTypeByID("btn_tate_and_liza", Button.class);
        Button btnSootopolis = this.findPaneOfTypeByID("btn_sootopolis", Button.class);

        if (btnRoxanne != null) btnRoxanne.setHandler(btn -> startChallenge("roxanne"));
        if (btnBrawly != null) { btnBrawly.setHandler(btn -> startChallenge("brawly")); btnBrawly.setEnabled(ClientBadgeCache.hasStoneBadge); }
        if (btnWattson != null) { btnWattson.setHandler(btn -> startChallenge("wattson")); btnWattson.setEnabled(ClientBadgeCache.hasKnuckleBadge); }
        if (btnFlannery != null) { btnFlannery.setHandler(btn -> startChallenge("flannery")); btnFlannery.setEnabled(ClientBadgeCache.hasDynamoBadge); }
        if (btnNorman != null) { btnNorman.setHandler(btn -> startChallenge("norman")); btnNorman.setEnabled(ClientBadgeCache.hasHeatBadge); }
        if (btnWinona != null) { btnWinona.setHandler(btn -> startChallenge("winona")); btnWinona.setEnabled(ClientBadgeCache.hasBalanceBadge); }
        if (btnTateLiza != null) { btnTateLiza.setHandler(btn -> startChallenge("tate_and_liza")); btnTateLiza.setEnabled(ClientBadgeCache.hasFeatherBadge); }
        if (btnSootopolis != null) { btnSootopolis.setHandler(btn -> startChallenge("sootopolis")); btnSootopolis.setEnabled(ClientBadgeCache.hasMindBadge); }
    }

    private void setupSinnohRoster() {
        Button btnRoark = this.findPaneOfTypeByID("btn_roark", Button.class);
        Button btnGardenia = this.findPaneOfTypeByID("btn_gardenia", Button.class);
        Button btnMaylene = this.findPaneOfTypeByID("btn_maylene", Button.class);
        Button btnCrasherWake = this.findPaneOfTypeByID("btn_crasher_wake", Button.class);
        Button btnFantina = this.findPaneOfTypeByID("btn_fantina", Button.class);
        Button btnByron = this.findPaneOfTypeByID("btn_byron", Button.class);
        Button btnCandice = this.findPaneOfTypeByID("btn_candice", Button.class);
        Button btnVolkner = this.findPaneOfTypeByID("btn_volkner", Button.class);

        if (btnRoark != null) btnRoark.setHandler(btn -> startChallenge("roark"));
        if (btnGardenia != null) { btnGardenia.setHandler(btn -> startChallenge("gardenia")); btnGardenia.setEnabled(ClientBadgeCache.hasCoalBadge); }
        if (btnMaylene != null) { btnMaylene.setHandler(btn -> startChallenge("maylene")); btnMaylene.setEnabled(ClientBadgeCache.hasForestBadge); }
        if (btnCrasherWake != null) { btnCrasherWake.setHandler(btn -> startChallenge("crasher_wake")); btnCrasherWake.setEnabled(ClientBadgeCache.hasCobbleBadge); }
        if (btnFantina != null) { btnFantina.setHandler(btn -> startChallenge("fantina")); btnFantina.setEnabled(ClientBadgeCache.hasFenBadge); }
        if (btnByron != null) { btnByron.setHandler(btn -> startChallenge("byron")); btnByron.setEnabled(ClientBadgeCache.hasRelicBadge); }
        if (btnCandice != null) { btnCandice.setHandler(btn -> startChallenge("candice")); btnCandice.setEnabled(ClientBadgeCache.hasMineBadge); }
        if (btnVolkner != null) { btnVolkner.setHandler(btn -> startChallenge("volkner")); btnVolkner.setEnabled(ClientBadgeCache.hasIcicleBadge); }
    }

    private void setupUnovaRoster() {
        Button btnStriaton = this.findPaneOfTypeByID("btn_striaton", Button.class);
        Button btnLenora = this.findPaneOfTypeByID("btn_lenora", Button.class);
        Button btnBurgh = this.findPaneOfTypeByID("btn_burgh", Button.class);
        Button btnElesa = this.findPaneOfTypeByID("btn_elesa", Button.class);
        Button btnClay = this.findPaneOfTypeByID("btn_clay", Button.class);
        Button btnSkyla = this.findPaneOfTypeByID("btn_skyla", Button.class);
        Button btnBrycen = this.findPaneOfTypeByID("btn_brycen", Button.class);
        Button btnOpelucid = this.findPaneOfTypeByID("btn_opelucid", Button.class);
        Button btnCheren = this.findPaneOfTypeByID("btn_cheren", Button.class);
        Button btnRoxie = this.findPaneOfTypeByID("btn_roxie", Button.class);
        Button btnMarlon = this.findPaneOfTypeByID("btn_marlon", Button.class);

        if (btnStriaton != null) btnStriaton.setHandler(btn -> startChallenge("striaton"));

        if (btnLenora != null) { btnLenora.setHandler(btn -> startChallenge("lenora")); btnLenora.setEnabled(ClientBadgeCache.hasTrioBadge); }
        if (btnBurgh != null) { btnBurgh.setHandler(btn -> startChallenge("burgh")); btnBurgh.setEnabled(ClientBadgeCache.hasBasicBadge); }
        if (btnElesa != null) { btnElesa.setHandler(btn -> startChallenge("elesa")); btnElesa.setEnabled(ClientBadgeCache.hasInsectBadge); }
        if (btnClay != null) { btnClay.setHandler(btn -> startChallenge("clay")); btnClay.setEnabled(ClientBadgeCache.hasBoltBadge); }
        if (btnSkyla != null) { btnSkyla.setHandler(btn -> startChallenge("skyla")); btnSkyla.setEnabled(ClientBadgeCache.hasQuakeBadge); }
        if (btnBrycen != null) { btnBrycen.setHandler(btn -> startChallenge("brycen")); btnBrycen.setEnabled(ClientBadgeCache.hasJetBadge); }

        if (btnOpelucid != null) { btnOpelucid.setHandler(btn -> startChallenge("opelucid")); btnOpelucid.setEnabled(ClientBadgeCache.hasFreezeBadge); }

        if (btnCheren != null) { btnCheren.setHandler(btn -> startChallenge("cheren")); btnCheren.setEnabled(ClientBadgeCache.hasTrioBadge); }
        if (btnRoxie != null) { btnRoxie.setHandler(btn -> startChallenge("roxie")); btnRoxie.setEnabled(ClientBadgeCache.hasLegendBadge); }
        if (btnMarlon != null) { btnMarlon.setHandler(btn -> startChallenge("marlon")); btnMarlon.setEnabled(ClientBadgeCache.hasToxicBadge); }
    }

    private void setupKalosRoster() {
        Button btnViola = this.findPaneOfTypeByID("btn_viola", Button.class);
        Button btnGrant = this.findPaneOfTypeByID("btn_grant", Button.class);
        Button btnKorrina = this.findPaneOfTypeByID("btn_korrina", Button.class);
        Button btnRamos = this.findPaneOfTypeByID("btn_ramos", Button.class);
        Button btnClemont = this.findPaneOfTypeByID("btn_clemont", Button.class);
        Button btnValerie = this.findPaneOfTypeByID("btn_valerie", Button.class);
        Button btnOlympia = this.findPaneOfTypeByID("btn_olympia", Button.class);
        Button btnWulfric = this.findPaneOfTypeByID("btn_wulfric", Button.class);

        if (btnViola != null) btnViola.setHandler(btn -> startChallenge("viola"));
        if (btnGrant != null) { btnGrant.setHandler(btn -> startChallenge("grant")); btnGrant.setEnabled(ClientBadgeCache.hasBugBadge); }
        if (btnKorrina != null) { btnKorrina.setHandler(btn -> startChallenge("korrina")); btnKorrina.setEnabled(ClientBadgeCache.hasCliffBadge); }
        if (btnRamos != null) { btnRamos.setHandler(btn -> startChallenge("ramos")); btnRamos.setEnabled(ClientBadgeCache.hasRumbleBadge); }
        if (btnClemont != null) { btnClemont.setHandler(btn -> startChallenge("clemont")); btnClemont.setEnabled(ClientBadgeCache.hasPlantBadge); }
        if (btnValerie != null) { btnValerie.setHandler(btn -> startChallenge("valerie")); btnValerie.setEnabled(ClientBadgeCache.hasVoltageBadge); }
        if (btnOlympia != null) { btnOlympia.setHandler(btn -> startChallenge("olympia")); btnOlympia.setEnabled(ClientBadgeCache.hasFairyBadge); }
        if (btnWulfric != null) { btnWulfric.setHandler(btn -> startChallenge("wulfric")); btnWulfric.setEnabled(ClientBadgeCache.hasPsychicBadge); }
    }

    private void setupGalarRoster() {
        Button btnMilo = this.findPaneOfTypeByID("btn_milo", Button.class);
        Button btnNessa = this.findPaneOfTypeByID("btn_nessa", Button.class);
        Button btnKabu = this.findPaneOfTypeByID("btn_kabu", Button.class);
        Button btnStowOnSide = this.findPaneOfTypeByID("btn_stow_on_side", Button.class);
        Button btnOpal = this.findPaneOfTypeByID("btn_opal", Button.class);
        Button btnCirchester = this.findPaneOfTypeByID("btn_circhester", Button.class);
        Button btnPiers = this.findPaneOfTypeByID("btn_piers", Button.class);
        Button btnRaihan = this.findPaneOfTypeByID("btn_raihan", Button.class);

        if (btnMilo != null) btnMilo.setHandler(btn -> startChallenge("milo"));
        if (btnNessa != null) { btnNessa.setHandler(btn -> startChallenge("nessa")); btnNessa.setEnabled(ClientBadgeCache.hasGalarGrassBadge); }
        if (btnKabu != null) { btnKabu.setHandler(btn -> startChallenge("kabu")); btnKabu.setEnabled(ClientBadgeCache.hasGalarWaterBadge); }

        if (btnStowOnSide != null) { btnStowOnSide.setHandler(btn -> startChallenge("stow_on_side")); btnStowOnSide.setEnabled(ClientBadgeCache.hasGalarFireBadge); }

        if (btnOpal != null) { btnOpal.setHandler(btn -> startChallenge("opal")); btnOpal.setEnabled(ClientBadgeCache.hasGalarFightingBadge || ClientBadgeCache.hasGalarGhostBadge); }

        if (btnCirchester != null) { btnCirchester.setHandler(btn -> startChallenge("circhester")); btnCirchester.setEnabled(ClientBadgeCache.hasGalarFairyBadge); }

        if (btnPiers != null) { btnPiers.setHandler(btn -> startChallenge("piers")); btnPiers.setEnabled(ClientBadgeCache.hasGalarRockBadge || ClientBadgeCache.hasGalarIceBadge); }
        if (btnRaihan != null) { btnRaihan.setHandler(btn -> startChallenge("raihan")); btnRaihan.setEnabled(ClientBadgeCache.hasGalarDarkBadge); }
    }

    private void setupPaldeaRoster() {
        Button btnKaty = this.findPaneOfTypeByID("btn_katy", Button.class);
        Button btnBrassius = this.findPaneOfTypeByID("btn_brassius", Button.class);
        Button btnIono = this.findPaneOfTypeByID("btn_iono", Button.class);
        Button btnKofu = this.findPaneOfTypeByID("btn_kofu", Button.class);
        Button btnLarry = this.findPaneOfTypeByID("btn_larry", Button.class);
        Button btnRyme = this.findPaneOfTypeByID("btn_ryme", Button.class);
        Button btnTulip = this.findPaneOfTypeByID("btn_tulip", Button.class);
        Button btnGrusha = this.findPaneOfTypeByID("btn_grusha", Button.class);

        if (btnKaty != null) btnKaty.setHandler(btn -> startChallenge("katy"));
        if (btnBrassius != null) { btnBrassius.setHandler(btn -> startChallenge("brassius")); btnBrassius.setEnabled(ClientBadgeCache.hasPaldeaBugBadge); }
        if (btnIono != null) { btnIono.setHandler(btn -> startChallenge("iono")); btnIono.setEnabled(ClientBadgeCache.hasPaldeaGrassBadge); }
        if (btnKofu != null) { btnKofu.setHandler(btn -> startChallenge("kofu")); btnKofu.setEnabled(ClientBadgeCache.hasPaldeaElectricBadge); }
        if (btnLarry != null) { btnLarry.setHandler(btn -> startChallenge("larry")); btnLarry.setEnabled(ClientBadgeCache.hasPaldeaWaterBadge); }
        if (btnRyme != null) { btnRyme.setHandler(btn -> startChallenge("ryme")); btnRyme.setEnabled(ClientBadgeCache.hasPaldeaNormalBadge); }
        if (btnTulip != null) { btnTulip.setHandler(btn -> startChallenge("tulip")); btnTulip.setEnabled(ClientBadgeCache.hasPaldeaGhostBadge); }
        if (btnGrusha != null) { btnGrusha.setHandler(btn -> startChallenge("grusha")); btnGrusha.setEnabled(ClientBadgeCache.hasPaldeaPsychicBadge); }
    }

    private void setupSupporter() {
        Button btnSupporter = this.findPaneOfTypeByID("btn_supporter", Button.class);
        if (btnSupporter != null) {
            btnSupporter.setHandler(btn -> startChallenge("supporter"));
        }
    }

    private void startChallenge(String leaderName) {
        PacketDistributor.sendToServer(new GymChallengePacket(this.gymPos, leaderName));
        net.minecraft.client.Minecraft.getInstance().setScreen(null);
    }
}