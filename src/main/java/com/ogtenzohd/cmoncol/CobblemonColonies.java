package com.ogtenzohd.cmoncol;

import com.minecolonies.api.sounds.EventType;
import com.minecolonies.api.sounds.ModSoundEvents;
import com.minecolonies.api.util.Tuple;
import com.mojang.logging.LogUtils;
import com.ogtenzohd.cmoncol.config.CCConfig;
import com.ogtenzohd.cmoncol.network.CmoncolPackets;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

@Mod(CobblemonColonies.MODID)
public class CobblemonColonies {
    public static final String MODID = "cmoncol";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CobblemonColonies(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("[Cobblemon Colonies] Mod Construction Starting...");
        com.ogtenzohd.cmoncol.util.CmoncolPerks.fetchVIPLists();

        modContainer.registerConfig(ModConfig.Type.COMMON, CCConfig.SPEC, "cmoncol-common.toml");
        modEventBus.addListener(CmoncolPackets::register);
        CmoncolReg.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerEntityAttributes);
        modEventBus.addListener(this::registerEntityRenderers);
        modEventBus.addListener(this::onAddLayers);

        LOGGER.info("[Cobblemon Colonies] Mod Construction Complete.");
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(CmoncolReg.GHOST_RECEPTIONIST.get(), com.ogtenzohd.cmoncol.entity.GhostReceptionistEntity.createAttributes().build());
        event.put(CmoncolReg.RANGER_ENTITY.get(), com.ogtenzohd.cmoncol.entity.RangerEntity.createAttributes().build());
    }

    private void registerEntityRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                com.ogtenzohd.cmoncol.registration.CmoncolReg.GHOST_RECEPTIONIST.get(),
                com.ogtenzohd.cmoncol.client.render.InvisibleEntityRenderer::new
        );
        event.registerEntityRenderer(
                com.ogtenzohd.cmoncol.registration.CmoncolReg.RANGER_ENTITY.get(),
                com.ogtenzohd.cmoncol.client.render.InvisibleEntityRenderer::new
        );
    }

    private void onAddLayers(net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers event) {

        var defaultRenderer = event.getSkin(net.minecraft.client.resources.PlayerSkin.Model.WIDE);
        if (defaultRenderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer playerRenderer) {
            playerRenderer.addLayer(new com.ogtenzohd.cmoncol.client.render.VipBadgeLayer(playerRenderer));
            playerRenderer.addLayer(new com.ogtenzohd.cmoncol.client.render.VipCapeLayer(playerRenderer)); // <--- Added Cape!
        }

        var slimRenderer = event.getSkin(net.minecraft.client.resources.PlayerSkin.Model.SLIM);
        if (slimRenderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer playerRenderer) {
            playerRenderer.addLayer(new com.ogtenzohd.cmoncol.client.render.VipBadgeLayer(playerRenderer));
            playerRenderer.addLayer(new com.ogtenzohd.cmoncol.client.render.VipCapeLayer(playerRenderer)); // <--- Added Cape!
        }

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[Cobblemon Colonies] Common Setup Starting...");
        event.enqueueWork(() -> {
            com.ogtenzohd.cmoncol.util.RancherRecipeManager.loadLocalConfigs();

            Map<String, Map<EventType, List<Tuple<SoundEvent, SoundEvent>>>> sounds = ModSoundEvents.CITIZEN_SOUND_EVENTS;
            String[] myJobs = {"rancher", "attendant", "ev_trainer","harvester", "pokeball_workshop", "nurse", "science_lab", "pokemon_guard", "gym", "wonder_trader", "pokemerchant"};
            var baseSoundMap = sounds.get("deliveryman");

            if (baseSoundMap != null) {
                LOGGER.info("[Cobblemon Colonies] Registering Citizen Sounds...");
                for (String jobName : myJobs) {
                    if (!sounds.containsKey(jobName)) {
                        sounds.put(jobName, baseSoundMap);
                    }
                }
            } else {
                LOGGER.warn("[Cobblemon Colonies] Could not find base 'deliveryman' sounds!");
            }
        });
        LOGGER.info("[Cobblemon Colonies] Common Setup Complete.");
    }
}