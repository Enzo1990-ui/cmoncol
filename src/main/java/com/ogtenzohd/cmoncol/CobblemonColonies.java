package com.ogtenzohd.cmoncol;

import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import com.ogtenzohd.cmoncol.config.CCConfig;
import com.ogtenzohd.cmoncol.network.CmoncolPackets;
import com.minecolonies.api.sounds.EventType;
import com.minecolonies.api.sounds.ModSoundEvents;
import com.minecolonies.api.util.Tuple;
import com.mojang.logging.LogUtils;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

import com.ogtenzohd.cmoncol.registration.CmoncolReg; 
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(CobblemonColonies.MODID)
public class CobblemonColonies {
    public static final String MODID = "cmoncol";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CobblemonColonies(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("[Cobblemon Colonies] Mod Construction Starting...");
        
        modContainer.registerConfig(ModConfig.Type.COMMON, CCConfig.SPEC, "cmoncol-common.toml");
        modEventBus.addListener(CmoncolPackets::register);
        CmoncolReg.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        
        modEventBus.addListener(this::registerEntityAttributes);
		modEventBus.addListener(this::registerEntityRenderers);
             
        LOGGER.info("[Cobblemon Colonies] Mod Construction Complete.");
    }
    
    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(CmoncolReg.GHOST_RECEPTIONIST.get(), com.ogtenzohd.cmoncol.entity.GhostReceptionistEntity.createAttributes().build());
    }
	
	private void registerEntityRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
            com.ogtenzohd.cmoncol.registration.CmoncolReg.GHOST_RECEPTIONIST.get(), 
            com.ogtenzohd.cmoncol.client.render.InvisibleEntityRenderer::new
        );
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[Cobblemon Colonies] Common Setup Starting...");
        event.enqueueWork(() -> {
			com.ogtenzohd.cmoncol.util.RancherRecipeManager.load();
			//this is how other mods register the sounds
            Map<String, Map<EventType, List<Tuple<SoundEvent, SoundEvent>>>> sounds = ModSoundEvents.CITIZEN_SOUND_EVENTS;
            String[] myJobs = {"rancher", "attendant", "ev_trainer","harvester", "pokeball_workshop", "nurse", "science_lab", "pokemon_guard", "gym"};
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