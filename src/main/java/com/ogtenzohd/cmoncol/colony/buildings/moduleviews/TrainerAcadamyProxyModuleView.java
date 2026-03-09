package com.ogtenzohd.cmoncol.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.ogtenzohd.cmoncol.colony.buildings.gui.TrainerAcadamyProxyWindow;
import com.ogtenzohd.cmoncol.blocks.custom.traineracadamy.TrainerAcadamyBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

public class TrainerAcadamyProxyModuleView extends AbstractBuildingModuleView {
    public String storedPokemonName = "None";
    public String currentStat = "hp";
    public boolean currentHyper = false;

    public TrainerAcadamyProxyModuleView() { super(); }
    
    @Override 
    public BOWindow getWindow() { return new TrainerAcadamyProxyWindow(this); }
    
    @Override 
    public Component getDesc() { return Component.literal("Trainer Acadamy Settings"); }

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
                if (be instanceof TrainerAcadamyBlockEntity gym) {
                    this.storedPokemonName = gym.hasPokemon() ? "Stored [Data Present]" : "None";
                    this.currentStat = gym.getTargetStat();
                    this.currentHyper = gym.isHyperTrain();
                }
            }
        }
    }
    
    @Override 
    public void deserialize(RegistryFriendlyByteBuf buf) {}
}