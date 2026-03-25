package com.ogtenzohd.cmoncol.colony.buildings;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.core.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import net.minecraft.core.BlockPos;

public class PokeballWorkshopBuildingView extends AbstractBuildingView {

    public PokeballWorkshopBuildingView(IColonyView colony, BlockPos pos) {
        super(colony, pos);
    }

    public static class PokeballCraftingModuleView extends CraftingModuleView {
        public PokeballCraftingModuleView() {
            super();
        }
    }
}