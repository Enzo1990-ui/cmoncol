package com.ogtenzohd.cmoncol.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.ogtenzohd.cmoncol.blocks.custom.pokemonguard.PokemonGuardBuildingBlockEntity;
import com.ogtenzohd.cmoncol.colony.buildings.gui.PokemonGuardProxyWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PokemonGuardProxyModuleView extends AbstractBuildingModuleView {
    public int buildingLevel = 1;
    public final List<Integer> guardIds = new ArrayList<>();
    public final Map<Integer, String> guardNames = new HashMap<>();
    public final Map<Integer, String> assignments = new HashMap<>();

    public PokemonGuardProxyModuleView() { super(); }

    @Override
    public BOWindow getWindow() { return new PokemonGuardProxyWindow(this); }

    @Override
    public Component getDesc() { return Component.literal("Guard Settings"); }

    public BlockPos getTargetPos() {
        if (getBuildingView() != null) return getBuildingView().getPosition();
        if (Minecraft.getInstance().hitResult instanceof BlockHitResult hit) return hit.getBlockPos();
        return null;
    }

    public void syncFromBlockEntity() {
        if (this.getBuildingView() != null) {
            this.buildingLevel = this.getBuildingView().getBuildingLevel();
        }

        BlockPos pos = getTargetPos();
        if (pos != null && Minecraft.getInstance().level != null) {
            Level level = Minecraft.getInstance().level;
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PokemonGuardBuildingBlockEntity guardBE) {
                this.guardIds.clear();
                this.guardIds.addAll(guardBE.syncedGuardIds);

                this.guardNames.clear();
                this.guardNames.putAll(guardBE.syncedGuardNames);

                this.assignments.clear();
                for (int id : this.guardIds) {
                    this.assignments.put(id, guardBE.getAssignedPartner(id));
                }
            }
        }
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {}
}