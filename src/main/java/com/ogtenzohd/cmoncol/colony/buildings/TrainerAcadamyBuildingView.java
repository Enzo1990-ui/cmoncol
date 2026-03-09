package com.ogtenzohd.cmoncol.colony.buildings;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import net.minecraft.core.BlockPos;

public class TrainerAcadamyBuildingView extends AbstractBuildingView implements IBuildingView {
    public TrainerAcadamyBuildingView(IColonyView colony, BlockPos pos) {
        super(colony, pos);
    }
}