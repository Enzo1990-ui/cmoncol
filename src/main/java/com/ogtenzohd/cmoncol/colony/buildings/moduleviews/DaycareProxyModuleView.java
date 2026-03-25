package com.ogtenzohd.cmoncol.colony.buildings.moduleviews;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.ogtenzohd.cmoncol.blocks.custom.daycare.DaycareBlockEntity;
import com.ogtenzohd.cmoncol.colony.buildings.gui.DaycareProxyWindow;
import com.ogtenzohd.cmoncol.compat.CmoncolEconomyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DaycareProxyModuleView extends AbstractBuildingModuleView {
    public String slot1Text = "Slot 1: Empty";
    public String slot2Text = "Slot 2: Empty";

    public int slot1Cost = 0;
    public int slot2Cost = 0;

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
            this.slot1Cost = 0;
            this.slot2Cost = 0;

            if (level != null && Minecraft.getInstance().player != null) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof DaycareBlockEntity daycare) {

                    List<DaycareBlockEntity.DaycareSlot> mySlots = daycare.getStoredPokemon().stream()
                            .filter(s -> s.ownerUUID.equals(Minecraft.getInstance().player.getUUID()))
                            .toList();

                    RegistryAccess regAccess = level.registryAccess();

                    if (!mySlots.isEmpty()) {
                        Pokemon currentMon = new Pokemon();
                        currentMon.loadFromNBT(regAccess, mySlots.getFirst().pokemonNBT);

                        Pokemon originalMon = new Pokemon();
                        originalMon.loadFromNBT(regAccess, mySlots.getFirst().snapshotNBT);

                        String name = currentMon.getSpecies().getName();
                        this.slot1Text = "Slot 1: " + name.substring(0, 1).toUpperCase() + name.substring(1);

                        if (com.ogtenzohd.cmoncol.config.CCConfig.INSTANCE.enableDaycareCost.get()) {
                            int levelsGained = Math.max(0, currentMon.getLevel() - originalMon.getLevel());
                            this.slot1Cost = CmoncolEconomyManager.get() instanceof com.ogtenzohd.cmoncol.economy.providers.CobbleDollarsProvider ?
                                    100 + (levelsGained * 100) : levelsGained;
                        }
                    }
                    if (mySlots.size() > 1) {
                        Pokemon currentMon = new Pokemon();
                        currentMon.loadFromNBT(regAccess, mySlots.get(1).pokemonNBT);

                        Pokemon originalMon = new Pokemon();
                        originalMon.loadFromNBT(regAccess, mySlots.get(1).snapshotNBT);

                        String name = currentMon.getSpecies().getName();
                        this.slot2Text = "Slot 2: " + name.substring(0, 1).toUpperCase() + name.substring(1);

                        if (com.ogtenzohd.cmoncol.config.CCConfig.INSTANCE.enableDaycareCost.get()) {
                            int levelsGained = Math.max(0, currentMon.getLevel() - originalMon.getLevel());
                            this.slot2Cost = CmoncolEconomyManager.get() instanceof com.ogtenzohd.cmoncol.economy.providers.CobbleDollarsProvider ?
                                    100 + (levelsGained * 100) : levelsGained;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void deserialize(@NotNull RegistryFriendlyByteBuf buf) {}
}