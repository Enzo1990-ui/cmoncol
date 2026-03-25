package com.ogtenzohd.cmoncol.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.ogtenzohd.cmoncol.colony.buildings.gui.PokemartProxyWindow;
import com.ogtenzohd.cmoncol.blocks.custom.pokemart.PokemartBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

public class PokemartProxyModuleView extends AbstractBuildingModuleView {
    
    private int buildingLevel = 1;

    public PokemartProxyModuleView() { super(); }
    
    @Override 
    public BOWindow getWindow() { return new PokemartProxyWindow(this); }
    
    @Override 
    public Component getDesc() { return Component.literal("Pokemart Info"); }

    public BlockPos getTargetPos() {
        if (Minecraft.getInstance().hitResult instanceof BlockHitResult hit) return hit.getBlockPos();
        return null;
    }

    public void syncFromBlockEntity() {
        BlockPos pos = getTargetPos();
        if (pos != null) {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof PokemartBlockEntity mart && mart.getBuilding() != null) {
                    this.buildingLevel = mart.getBuilding().getBuildingLevel();
                }
            }
        }
    }

    public int getBuildingLevel() {
        return this.buildingLevel;
    }

    @Override 
    public void deserialize(RegistryFriendlyByteBuf buf) {}
}