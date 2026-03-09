package com.ogtenzohd.cmoncol.colony.buildings;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import net.minecraft.core.BlockPos;

public class ScienceLabBuildingView extends AbstractBuildingView implements IBuildingView {
    public ScienceLabBuildingView(IColonyView colony, BlockPos pos) {
        super(colony, pos);
    }
}