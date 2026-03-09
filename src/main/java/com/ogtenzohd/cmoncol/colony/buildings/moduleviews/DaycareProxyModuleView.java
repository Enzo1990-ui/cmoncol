package com.ogtenzohd.cmoncol.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.ogtenzohd.cmoncol.colony.buildings.gui.DaycareProxyWindow;
import com.ogtenzohd.cmoncol.blocks.custom.daycare.DaycareBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.List;
import java.util.stream.Collectors;

public class DaycareProxyModuleView extends AbstractBuildingModuleView {
    public String slot1Text = "Slot 1: Empty";
    public String slot2Text = "Slot 2: Empty";

    public DaycareProxyModuleView() { super(); }
    
    @Override 
    public BOWindow getWindow() { return new DaycareProxyWindow(this); }
    
    @Override 
    public Component getDesc() { return Component.literal("Daycare Settings"); }

    public BlockPos getTargetPos() {
        if (Minecraft.getInstance().hitResult instanceof BlockHitResult hit) return hit.getBlockPos();
        return null;
    }

    public void syncFromBlockEntity() {
        BlockPos pos = getTargetPos();
        if (pos != null) {
            Level level = Minecraft.getInstance().level;
            
            this.slot1Text = "Slot 1: Empty";
            this.slot2Text = "Slot 2: Empty";
            
            if (level != null && Minecraft.getInstance().player != null) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof DaycareBlockEntity daycare) {
                    
                    List<DaycareBlockEntity.DaycareSlot> mySlots = daycare.getStoredPokemon().stream()
                        .filter(s -> s.ownerUUID.equals(Minecraft.getInstance().player.getUUID()))
                        .collect(Collectors.toList());
                        
                    RegistryAccess regAccess = level.registryAccess();
                    
                    if (mySlots.size() > 0) {
                        Pokemon p1 = new Pokemon();
                        p1.loadFromNBT(regAccess, mySlots.get(0).pokemonNBT);
                        String name = p1.getSpecies().getName();
                        this.slot1Text = "Slot 1: " + name.substring(0, 1).toUpperCase() + name.substring(1);
                    }
                    if (mySlots.size() > 1) {
                        Pokemon p2 = new Pokemon();
                        p2.loadFromNBT(regAccess, mySlots.get(1).pokemonNBT);
                        String name = p2.getSpecies().getName();
                        this.slot2Text = "Slot 2: " + name.substring(0, 1).toUpperCase() + name.substring(1);
                    }
                }
            }
        }
    }
    
    @Override 
    public void deserialize(RegistryFriendlyByteBuf buf) {}
}