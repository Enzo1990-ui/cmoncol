package com.ogtenzohd.cmoncol.events;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.util.RancherRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@EventBusSubscriber(modid = CobblemonColonies.MODID)
public class RancherReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();
    public RancherReloadListener() {
        super(GSON, "cmoncol_pasture"); 
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new RancherReloadListener());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        RancherRecipeManager.clearMemory();

        for (Map.Entry<ResourceLocation, JsonElement> entry : files.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                RancherRecipeManager.parseAndAddRecipes(entry.getValue().getAsJsonObject());
            }
        }
        RancherRecipeManager.loadLocalConfigs();
        
        CobblemonColonies.LOGGER.info("Successfully loaded Pasture Recipes from Datapacks & Config!");
    }
}