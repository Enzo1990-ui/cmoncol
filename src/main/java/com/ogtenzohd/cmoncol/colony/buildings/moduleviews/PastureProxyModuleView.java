package com.ogtenzohd.cmoncol.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.ogtenzohd.cmoncol.blocks.custom.pasture.PastureBlockEntity;
import com.ogtenzohd.cmoncol.colony.buildings.gui.PastureProxyWindow;
import com.ogtenzohd.cmoncol.util.RancherRecipeManager; // Fixed Import
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.ArrayList;
import java.util.List;

public class PastureProxyModuleView extends AbstractBuildingModuleView {
    public final List<String> slotNames = new ArrayList<>();
    public final List<String> slotModes = new ArrayList<>(); 
    public final List<Boolean> slotToggleable = new ArrayList<>(); 
    public int maxSlots = 0;

    public PastureProxyModuleView() { super(); }

    @Override public BOWindow getWindow() { return new PastureProxyWindow(this); }
    @Override public Component getDesc() { return Component.literal("Pasture Storage"); }

    public void syncFromBlockEntity() {
        slotNames.clear();
        slotModes.clear();
        slotToggleable.clear();
        BlockPos pos = getTargetPos();
        if (pos != null && Minecraft.getInstance().level != null) {
            BlockEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
            if (be instanceof PastureBlockEntity pasture) {
                this.maxSlots = pasture.getMaxSlots();
                for (PastureBlockEntity.PastureSlot slot : pasture.getStoredPokemon()) {
                    Pokemon mon = new Pokemon();
                    mon.loadFromNBT(Minecraft.getInstance().level.registryAccess(), slot.pokemonNBT);
                    
                    slotNames.add(mon.getDisplayName(true).getString());
                    
                    String species = mon.getSpecies().getName().toLowerCase();
                    RancherRecipeManager.RancherRecipe r = RancherRecipeManager.getRecipe(species, slot.selectedRecipe);
                    if (r != null) {
                        slotModes.add(r.getLabel());
                        slotToggleable.add(RancherRecipeManager.getRecipesFor(species).size() > 1);
                    } else {
                        slotModes.add("None");
                        slotToggleable.add(false);
                    }
                }
            }
        }
    }
    
    public BlockPos getTargetPos() {
        if (Minecraft.getInstance().hitResult instanceof BlockHitResult hit) return hit.getBlockPos();
        return null;
    }

    @Override public void deserialize(RegistryFriendlyByteBuf buf) {}
}