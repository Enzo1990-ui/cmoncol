package com.ogtenzohd.cmoncol.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.ogtenzohd.cmoncol.colony.buildings.gui.WonderTradeProxyWindow;
import com.ogtenzohd.cmoncol.blocks.custom.wondertrade.WonderTradeCentreBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

public class WonderTradeProxyModuleView extends AbstractBuildingModuleView {
    
    public boolean hasDepositedPokemon = false;
    public boolean hasReadyPokemon = false;
    public boolean isBoosted = false;
    public int tradeTimer = 0;

    public WonderTradeProxyModuleView() { super(); }
    
    @Override 
    public BOWindow getWindow() { return new WonderTradeProxyWindow(this); }
    
    @Override 
    public Component getDesc() { return Component.literal("Wonder Trade Settings"); }

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
                if (be instanceof WonderTradeCentreBlockEntity wt) {
                    this.hasReadyPokemon = wt.getReadyPokemon() != null;
                    this.tradeTimer = wt.getTradeTimer();
                    this.hasDepositedPokemon = this.tradeTimer > 0;
                    this.isBoosted = wt.isBoosted();
                }
            }
        }
    }
    
    @Override 
    public void deserialize(RegistryFriendlyByteBuf buf) {}
}