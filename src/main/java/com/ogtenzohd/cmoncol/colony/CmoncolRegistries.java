package com.ogtenzohd.cmoncol.colony;

import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.colony.buildings.moduleviews.WorkerBuildingModuleView;
import com.minecolonies.core.colony.jobs.views.CrafterJobView;
import com.minecolonies.core.colony.jobs.views.DefaultJobView;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.colony.buildings.*;
import com.ogtenzohd.cmoncol.colony.buildings.modules.*;
import com.ogtenzohd.cmoncol.colony.buildings.moduleviews.*;
import com.ogtenzohd.cmoncol.colony.job.*;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = CobblemonColonies.MODID)
public class CmoncolRegistries {

    //Registry Keys
    private static final ResourceKey<Registry<JobEntry>> JOB_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("minecolonies", "jobs"));
    private static final ResourceKey<Registry<BuildingEntry>> BLD_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("minecolonies", "buildings"));

    //Job Entries
    public static JobEntry RANCHER_JOB_ENTRY;
    public static JobEntry ATTENDANT_JOB_ENTRY;
    public static JobEntry EV_TRAINER_JOB_ENTRY;
    public static JobEntry HARVESTER_JOB_ENTRY;
    public static JobEntry POKEBALLWORKSHOP_JOB_ENTRY;
    public static JobEntry SCIENCELAB_JOB_ENTRY;
    public static JobEntry NURSE_JOB_ENTRY;
    public static JobEntry POKEMON_GUARD_JOB_ENTRY;
    public static JobEntry GYM_JOB_ENTRY;
    public static JobEntry WONDER_TRADER_JOB_ENTRY;
    public static JobEntry POKEMERCHANT_JOB_ENTRY;
    public static JobEntry RANGER_JOB_ENTRY;

    //Building Entries
    public static BuildingEntry PASTURE_BUILDING_ENTRY;
    public static BuildingEntry DAYCARE_BUILDING_ENTRY;
    public static BuildingEntry TRAINER_ACADAMY_BUILDING_ENTRY;
    public static BuildingEntry HARVESTER_BUILDING_ENTRY;
    public static BuildingEntry POKEBALLWORKSHOP_BUILDING_ENTRY;
    public static BuildingEntry SCIENCELAB_BUILDING_ENTRY;
    public static BuildingEntry POKECENTER_BUILDING_ENTRY;
    public static BuildingEntry POKEMON_GUARD_BUILDING_ENTRY;
    public static BuildingEntry GYM_BUILDING_ENTRY;
    public static BuildingEntry WONDER_TRADE_CENTRE_BUILDING_ENTRY;
    public static BuildingEntry POKEMART_BUILDING_ENTRY;
    public static BuildingEntry RANGER_BUILDING_ENTRY;

    //Modules
    public static BuildingEntry.ModuleProducer<WorkerBuildingModule, WorkerBuildingModuleView> RANCHER_WORK;
    public static BuildingEntry.ModuleProducer<WorkerBuildingModule, WorkerBuildingModuleView> ATTENDANT_WORK;
    public static BuildingEntry.ModuleProducer<WorkerBuildingModule, WorkerBuildingModuleView> EV_TRAINER_WORK;
    public static BuildingEntry.ModuleProducer<WorkerBuildingModule, WorkerBuildingModuleView> HARVESTER_WORK;
    public static BuildingEntry.ModuleProducer<WorkerBuildingModule, WorkerBuildingModuleView> POKEBALLWORKSHOP_WORK;
    public static BuildingEntry.ModuleProducer<PokeballWorkshopBuilding.CraftingModule, PokeballWorkshopBuildingView.PokeballCraftingModuleView> POKEBALLWORKSHOP_RECIPES;
    public static BuildingEntry.ModuleProducer<WorkerBuildingModule, WorkerBuildingModuleView> SCIENCELAB_WORK;
    public static BuildingEntry.ModuleProducer<WorkerBuildingModule, WorkerBuildingModuleView> POKECENTER_WORK;
    public static BuildingEntry.ModuleProducer<WorkerBuildingModule, WorkerBuildingModuleView> POKEMON_GUARD_WORK;
    public static BuildingEntry.ModuleProducer<WorkerBuildingModule, WorkerBuildingModuleView> GYM_WORK;
    public static BuildingEntry.ModuleProducer<WorkerBuildingModule, WorkerBuildingModuleView> WONDER_TRADER_WORK;
    public static BuildingEntry.ModuleProducer<WorkerBuildingModule, WorkerBuildingModuleView> POKEMERCHANT_WORK;
    public static BuildingEntry.ModuleProducer<WorkerBuildingModule, WorkerBuildingModuleView> RANGER_WORK;

    //Proxy Modules
    public static BuildingEntry.ModuleProducer<PastureProxyModule, PastureProxyModuleView> PASTURE_PROXY_MODULE;
    public static BuildingEntry.ModuleProducer<DaycareProxyModule, DaycareProxyModuleView> DAYCARE_PROXY_MODULE;
    public static BuildingEntry.ModuleProducer<TrainerAcadamyProxyModule, TrainerAcadamyProxyModuleView> TRAINER_ACADAMY_PROXY_MODULE;
    public static BuildingEntry.ModuleProducer<ScienceLabProxyModule, ScienceLabProxyModuleView> SCIENCELAB_PROXY_MODULE;
    public static BuildingEntry.ModuleProducer<ScienceLabJournalProxyModule, ScienceLabJournalProxyModuleView> SCIENCELAB_JOURNAL_MODULE;
    public static BuildingEntry.ModuleProducer<WonderTradeProxyModule, WonderTradeProxyModuleView> WONDER_TRADE_PROXY_MODULE;
    public static BuildingEntry.ModuleProducer<PokemartProxyModule, PokemartProxyModuleView> POKEMART_PROXY_MODULE;

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {

        // ==========================================
        // REGISTER JOBS
        // ==========================================
        if (event.getRegistryKey().equals(JOB_KEY)) {
            ResourceLocation rancherLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "rancher");
            RANCHER_JOB_ENTRY = new JobEntry.Builder().setRegistryName(rancherLoc).setJobProducer(RancherJob::new).setJobViewProducer(() -> DefaultJobView::new).createJobEntry();
            event.register(JOB_KEY, rancherLoc, () -> RANCHER_JOB_ENTRY);

            ResourceLocation attendantLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "attendant");
            ATTENDANT_JOB_ENTRY = new JobEntry.Builder().setRegistryName(attendantLoc).setJobProducer(AttendantJob::new).setJobViewProducer(() -> DefaultJobView::new).createJobEntry();
            event.register(JOB_KEY, attendantLoc, () -> ATTENDANT_JOB_ENTRY);

            ResourceLocation evTrainerLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "ev_trainer");
            EV_TRAINER_JOB_ENTRY = new JobEntry.Builder().setRegistryName(evTrainerLoc).setJobProducer(EVTrainerJob::new).setJobViewProducer(() -> DefaultJobView::new).createJobEntry();
            event.register(JOB_KEY, evTrainerLoc, () -> EV_TRAINER_JOB_ENTRY);

            ResourceLocation harvLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "harvester");
            HARVESTER_JOB_ENTRY = new JobEntry.Builder().setRegistryName(harvLoc).setJobProducer(HarvesterJob::new).setJobViewProducer(() -> DefaultJobView::new).createJobEntry();
            event.register(JOB_KEY, harvLoc, () -> HARVESTER_JOB_ENTRY);

            ResourceLocation pokeLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "pokeball_workshop");
            POKEBALLWORKSHOP_JOB_ENTRY = new JobEntry.Builder().setRegistryName(pokeLoc).setJobProducer(PokeballWorkshopJob::new).setJobViewProducer(() -> CrafterJobView::new).createJobEntry();
            event.register(JOB_KEY, pokeLoc, () -> POKEBALLWORKSHOP_JOB_ENTRY);

            ResourceLocation nurseLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "nurse");
            NURSE_JOB_ENTRY = new JobEntry.Builder().setRegistryName(nurseLoc).setJobProducer(NurseJob::new).setJobViewProducer(() -> DefaultJobView::new).createJobEntry();
            event.register(JOB_KEY, nurseLoc, () -> NURSE_JOB_ENTRY);

            ResourceLocation pguardLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "pokemon_guard");
            POKEMON_GUARD_JOB_ENTRY = new JobEntry.Builder().setRegistryName(pguardLoc).setJobProducer(PokemonGuardJob::new).setJobViewProducer(() -> DefaultJobView::new).createJobEntry();
            event.register(JOB_KEY, pguardLoc, () -> POKEMON_GUARD_JOB_ENTRY);

            ResourceLocation sciLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "science_lab");
            SCIENCELAB_JOB_ENTRY = new JobEntry.Builder().setRegistryName(sciLoc).setJobProducer(ScientistJob::new).setJobViewProducer(() -> DefaultJobView::new).createJobEntry();
            event.register(JOB_KEY, sciLoc, () -> SCIENCELAB_JOB_ENTRY);

            ResourceLocation gymJobLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "gym");
            GYM_JOB_ENTRY = new JobEntry.Builder().setRegistryName(gymJobLoc).setJobProducer(GymJob::new).setJobViewProducer(() -> DefaultJobView::new).createJobEntry();
            event.register(JOB_KEY, gymJobLoc, () -> GYM_JOB_ENTRY);

            ResourceLocation wonderTraderLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "wonder_trader");
            WONDER_TRADER_JOB_ENTRY = new JobEntry.Builder().setRegistryName(wonderTraderLoc).setJobProducer(WonderTraderJob::new).setJobViewProducer(() -> DefaultJobView::new).createJobEntry();
            event.register(JOB_KEY, wonderTraderLoc, () -> WONDER_TRADER_JOB_ENTRY);

            ResourceLocation pokeMerchantLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "pokemerchant");
            POKEMERCHANT_JOB_ENTRY = new JobEntry.Builder().setRegistryName(pokeMerchantLoc).setJobProducer(PokeMerchantJob::new).setJobViewProducer(() -> DefaultJobView::new).createJobEntry();
            event.register(JOB_KEY, pokeMerchantLoc, () -> POKEMERCHANT_JOB_ENTRY);

            ResourceLocation rangerLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "ranger");
            RANGER_JOB_ENTRY = new JobEntry.Builder().setRegistryName(rangerLoc).setJobProducer(RangerJob::new).setJobViewProducer(() -> DefaultJobView::new).createJobEntry();
            event.register(JOB_KEY, rangerLoc, () -> RANGER_JOB_ENTRY);
        }

        // ==========================================
        // INITIALIZE MODULES & BUILDINGS
        // ==========================================
        if (event.getRegistryKey().equals(BLD_KEY)) {
            RANCHER_WORK = new BuildingEntry.ModuleProducer<>("rancher_work", () -> new WorkerBuildingModule(RANCHER_JOB_ENTRY, Skill.Stamina, Skill.Agility, false, b -> 1), () -> WorkerBuildingModuleView::new);
            ATTENDANT_WORK = new BuildingEntry.ModuleProducer<>("attendant_work", () -> new WorkerBuildingModule(ATTENDANT_JOB_ENTRY, Skill.Mana, Skill.Knowledge, false, b -> 1), () -> WorkerBuildingModuleView::new);
            EV_TRAINER_WORK = new BuildingEntry.ModuleProducer<>("ev_trainer_work", () -> new WorkerBuildingModule(EV_TRAINER_JOB_ENTRY, Skill.Athletics, Skill.Strength, false, b -> 1), () -> WorkerBuildingModuleView::new);
            PASTURE_PROXY_MODULE = new BuildingEntry.ModuleProducer<>("pasture_proxy", PastureProxyModule::new, () -> PastureProxyModuleView::new);
            DAYCARE_PROXY_MODULE = new BuildingEntry.ModuleProducer<>("daycare_proxy", DaycareProxyModule::new, () -> DaycareProxyModuleView::new);
            TRAINER_ACADAMY_PROXY_MODULE = new BuildingEntry.ModuleProducer<>("acadamy_proxy", TrainerAcadamyProxyModule::new, () -> TrainerAcadamyProxyModuleView::new);
            SCIENCELAB_PROXY_MODULE = new BuildingEntry.ModuleProducer<>("sciencelab_proxy", ScienceLabProxyModule::new, () -> ScienceLabProxyModuleView::new);
            WONDER_TRADE_PROXY_MODULE = new BuildingEntry.ModuleProducer<>("wonder_trade_proxy", WonderTradeProxyModule::new, () -> WonderTradeProxyModuleView::new);
            POKEMART_PROXY_MODULE = new BuildingEntry.ModuleProducer<>("pokemart_proxy", PokemartProxyModule::new, () -> PokemartProxyModuleView::new);
            SCIENCELAB_JOURNAL_MODULE = new BuildingEntry.ModuleProducer<>("sciencelab_journal", ScienceLabJournalProxyModule::new, () -> ScienceLabJournalProxyModuleView::new);
            HARVESTER_WORK = new BuildingEntry.ModuleProducer<>("harvester_work", () -> new WorkerBuildingModule(HARVESTER_JOB_ENTRY, Skill.Agility, Skill.Stamina, false, b -> 1), () -> WorkerBuildingModuleView::new);
            POKEBALLWORKSHOP_WORK = new BuildingEntry.ModuleProducer<>("pokeballworkshop_work", () -> new com.minecolonies.core.colony.buildings.modules.CraftingWorkerBuildingModule(POKEBALLWORKSHOP_JOB_ENTRY, Skill.Dexterity, Skill.Agility, false, b -> 1), () -> WorkerBuildingModuleView::new);
            POKEBALLWORKSHOP_RECIPES = new BuildingEntry.ModuleProducer<>("pokeballworkshop_recipes", () -> new PokeballWorkshopBuilding.CraftingModule(POKEBALLWORKSHOP_JOB_ENTRY), () -> PokeballWorkshopBuildingView.PokeballCraftingModuleView::new);
            SCIENCELAB_WORK = new BuildingEntry.ModuleProducer<>("sciencelab_work", () -> new WorkerBuildingModule(SCIENCELAB_JOB_ENTRY, Skill.Intelligence, Skill.Knowledge, false, b -> 1), () -> WorkerBuildingModuleView::new);
            POKECENTER_WORK = new BuildingEntry.ModuleProducer<>("pokecenter_work", () -> new WorkerBuildingModule(NURSE_JOB_ENTRY, Skill.Intelligence, Skill.Adaptability, false, b -> 1), () -> WorkerBuildingModuleView::new);
            POKEMON_GUARD_WORK = new BuildingEntry.ModuleProducer<>("pokemon_guard_work", () -> new WorkerBuildingModule(POKEMON_GUARD_JOB_ENTRY, Skill.Athletics, Skill.Strength, false, b -> 5), () -> WorkerBuildingModuleView::new);
            GYM_WORK = new BuildingEntry.ModuleProducer<>("gym_work", () -> new WorkerBuildingModule(GYM_JOB_ENTRY, Skill.Knowledge, Skill.Mana, false, b -> 1), () -> WorkerBuildingModuleView::new);
            WONDER_TRADER_WORK = new BuildingEntry.ModuleProducer<>("wonder_trader_work", () -> new WorkerBuildingModule(WONDER_TRADER_JOB_ENTRY, Skill.Mana, Skill.Knowledge, false, b -> 1), () -> WorkerBuildingModuleView::new);
            POKEMERCHANT_WORK = new BuildingEntry.ModuleProducer<>("pokemerchant_work", () -> new WorkerBuildingModule(POKEMERCHANT_JOB_ENTRY, Skill.Adaptability, Skill.Intelligence, false, b -> 1), () -> WorkerBuildingModuleView::new);
            RANGER_WORK = new BuildingEntry.ModuleProducer<>("ranger_work", () -> new WorkerBuildingModule(RANGER_JOB_ENTRY, Skill.Agility, Skill.Athletics, false, b -> 1), () -> WorkerBuildingModuleView::new);

            // REGISTER BUILDINGS
            ResourceLocation pastLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "colony_pasture");
            PASTURE_BUILDING_ENTRY = new BuildingEntry.Builder()
                    .setRegistryName(pastLoc)
                    .setBuildingProducer(PastureBuilding::new)
                    .setBuildingViewProducer(() -> PastureBuildingView::new)
                    .setBuildingBlock(CmoncolReg.PASTURE_BLOCK.get())
                    .addBuildingModuleProducer(RANCHER_WORK)
                    .addBuildingModuleProducer(BuildingModules.MIN_STOCK)
                    .addBuildingModuleProducer(PASTURE_PROXY_MODULE)
                    .createBuildingEntry();
            event.register(BLD_KEY, pastLoc, () -> PASTURE_BUILDING_ENTRY);

            ResourceLocation dayLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "colony_daycare");
            DAYCARE_BUILDING_ENTRY = new BuildingEntry.Builder()
                    .setRegistryName(dayLoc)
                    .setBuildingProducer(DaycareBuilding::new)
                    .setBuildingViewProducer(() -> DaycareBuildingView::new)
                    .setBuildingBlock(CmoncolReg.DAYCARE_BLOCK.get())
                    .addBuildingModuleProducer(ATTENDANT_WORK)
                    .addBuildingModuleProducer(BuildingModules.MIN_STOCK)
                    .addBuildingModuleProducer(DAYCARE_PROXY_MODULE)
                    .createBuildingEntry();
            event.register(BLD_KEY, dayLoc, () -> DAYCARE_BUILDING_ENTRY);

            ResourceLocation academyLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "colony_traineracademy");
            TRAINER_ACADAMY_BUILDING_ENTRY = new BuildingEntry.Builder()
                    .setRegistryName(academyLoc)
                    .setBuildingProducer(TrainerAcadamyBuilding::new)
                    .setBuildingViewProducer(() -> TrainerAcadamyBuildingView::new)
                    .setBuildingBlock(CmoncolReg.TRAINER_ACADAMY_BLOCK.get())
                    .addBuildingModuleProducer(EV_TRAINER_WORK)
                    .addBuildingModuleProducer(BuildingModules.MIN_STOCK)
                    .addBuildingModuleProducer(TRAINER_ACADAMY_PROXY_MODULE)
                    .createBuildingEntry();
            event.register(BLD_KEY, academyLoc, () -> TRAINER_ACADAMY_BUILDING_ENTRY);

            ResourceLocation harvLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "colony_harvester");
            HARVESTER_BUILDING_ENTRY = new BuildingEntry.Builder()
                    .setRegistryName(harvLoc)
                    .setBuildingProducer(HarvesterBuilding::new)
                    .setBuildingViewProducer(() -> HarvesterBuildingView::new)
                    .setBuildingBlock(CmoncolReg.HARVESTER_BLOCK.get())
                    .addBuildingModuleProducer(HARVESTER_WORK)
                    .addBuildingModuleProducer(BuildingModules.MIN_STOCK)
                    .createBuildingEntry();
            event.register(BLD_KEY, harvLoc, () -> HARVESTER_BUILDING_ENTRY);

            ResourceLocation pbBldLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "colony_pokeballworkshop");
            POKEBALLWORKSHOP_BUILDING_ENTRY = new BuildingEntry.Builder()
                    .setRegistryName(pbBldLoc)
                    .setBuildingProducer(PokeballWorkshopBuilding::new)
                    .setBuildingViewProducer(() -> PokeballWorkshopBuildingView::new)
                    .setBuildingBlock(CmoncolReg.POKEBALLWORKSHOP_BLOCK.get())
                    .addBuildingModuleProducer(POKEBALLWORKSHOP_WORK)
                    .addBuildingModuleProducer(BuildingModules.MIN_STOCK)
                    .addBuildingModuleProducer(POKEBALLWORKSHOP_RECIPES)
                    .addBuildingModuleProducer(BuildingModules.STATS_MODULE)
                    .createBuildingEntry();
            event.register(BLD_KEY, pbBldLoc, () -> POKEBALLWORKSHOP_BUILDING_ENTRY);

            ResourceLocation centerLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "colony_pokecenter");
            POKECENTER_BUILDING_ENTRY = new BuildingEntry.Builder()
                    .setRegistryName(centerLoc)
                    .setBuildingProducer(PokeCenterBuilding::new)
                    .setBuildingViewProducer(() -> PokeCenterBuildingView::new)
                    .setBuildingBlock(CmoncolReg.POKECENTER_BLOCK.get())
                    .addBuildingModuleProducer(POKECENTER_WORK)
                    .addBuildingModuleProducer(BuildingModules.MIN_STOCK)
                    .createBuildingEntry();
            event.register(BLD_KEY, centerLoc, () -> POKECENTER_BUILDING_ENTRY);

            ResourceLocation sciLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "colony_science_lab");
            SCIENCELAB_BUILDING_ENTRY = new BuildingEntry.Builder()
                    .setRegistryName(sciLoc)
                    .setBuildingProducer(ScienceLabBuilding::new)
                    .setBuildingViewProducer(() -> ScienceLabBuildingView::new)
                    .setBuildingBlock(CmoncolReg.SCIENCELAB_BLOCK.get())
                    .addBuildingModuleProducer(SCIENCELAB_WORK)
                    .addBuildingModuleProducer(BuildingModules.MIN_STOCK)
                    .addBuildingModuleProducer(SCIENCELAB_PROXY_MODULE)
                    .addBuildingModuleProducer(SCIENCELAB_JOURNAL_MODULE)
                    .createBuildingEntry();
            event.register(BLD_KEY, sciLoc, () -> SCIENCELAB_BUILDING_ENTRY);

            ResourceLocation pguardBldLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "colony_pokemon_guard");
            POKEMON_GUARD_BUILDING_ENTRY = new BuildingEntry.Builder()
                    .setRegistryName(pguardBldLoc)
                    .setBuildingProducer(PokemonGuardBuilding::new)
                    .setBuildingViewProducer(() -> PokemonGuardBuilding.View::new)
                    .setBuildingBlock(CmoncolReg.POKEMON_GUARD_BLOCK.get())
                    .addBuildingModuleProducer(POKEMON_GUARD_WORK)
                    .addBuildingModuleProducer(BuildingModules.MIN_STOCK)
                    .addBuildingModuleProducer(BuildingModules.GUARD_SETTINGS)
                    .addBuildingModuleProducer(BuildingModules.GUARD_ENTITY_LIST)
                    .addBuildingModuleProducer(BuildingModules.STATS_MODULE)
                    .createBuildingEntry();
            event.register(BLD_KEY, pguardBldLoc, () -> POKEMON_GUARD_BUILDING_ENTRY);

            ResourceLocation gymBldLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "colony_gym");
            GYM_BUILDING_ENTRY = new BuildingEntry.Builder()
                    .setRegistryName(gymBldLoc)
                    .setBuildingProducer(GymBuilding::new)
                    .setBuildingViewProducer(() -> GymBuildingView::new)
                    .setBuildingBlock(CmoncolReg.GYM_BLOCK.get())
                    .addBuildingModuleProducer(GYM_WORK)
                    .addBuildingModuleProducer(BuildingModules.MIN_STOCK)
                    .createBuildingEntry();
            event.register(BLD_KEY, gymBldLoc, () -> GYM_BUILDING_ENTRY);

            ResourceLocation wonderTradeBldLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "colony_wonder_trade");
            WONDER_TRADE_CENTRE_BUILDING_ENTRY = new BuildingEntry.Builder()
                    .setRegistryName(wonderTradeBldLoc)
                    .setBuildingProducer(WonderTradeCentreBuilding::new)
                    .setBuildingViewProducer(() -> WonderTradeCentreBuildingView::new)
                    .setBuildingBlock(CmoncolReg.WONDER_TRADE_CENTRE_BLOCK.get())
                    .addBuildingModuleProducer(WONDER_TRADER_WORK)
                    .addBuildingModuleProducer(BuildingModules.MIN_STOCK)
                    .addBuildingModuleProducer(WONDER_TRADE_PROXY_MODULE)
                    .createBuildingEntry();
            event.register(BLD_KEY, wonderTradeBldLoc, () -> WONDER_TRADE_CENTRE_BUILDING_ENTRY);

            ResourceLocation pokemartBldLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "colony_pokemart");
            POKEMART_BUILDING_ENTRY = new BuildingEntry.Builder()
                    .setRegistryName(pokemartBldLoc)
                    .setBuildingProducer(PokemartBuilding::new)
                    .setBuildingViewProducer(() -> PokemartBuildingView::new)
                    .setBuildingBlock(CmoncolReg.POKEMART_BLOCK.get())
                    .addBuildingModuleProducer(POKEMERCHANT_WORK)
                    .addBuildingModuleProducer(BuildingModules.MIN_STOCK)
                    .addBuildingModuleProducer(POKEMART_PROXY_MODULE)
                    .createBuildingEntry();
            event.register(BLD_KEY, pokemartBldLoc, () -> POKEMART_BUILDING_ENTRY);

            ResourceLocation rangerBldLoc = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "colony_ranger");
            RANGER_BUILDING_ENTRY = new BuildingEntry.Builder()
                    .setRegistryName(rangerBldLoc)
                    .setBuildingProducer(WatchTowerBuilding::new)
                    .setBuildingViewProducer(() -> WatchTowerBuildingView::new)
                    .setBuildingBlock(CmoncolReg.RANGER_BLOCK.get())
                    .addBuildingModuleProducer(RANGER_WORK)
                    .addBuildingModuleProducer(BuildingModules.MIN_STOCK)
                    .createBuildingEntry();
            event.register(BLD_KEY, rangerBldLoc, () -> RANGER_BUILDING_ENTRY);
        }
    }
}