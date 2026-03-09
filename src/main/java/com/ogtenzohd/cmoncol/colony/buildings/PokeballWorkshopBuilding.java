package com.ogtenzohd.cmoncol.colony.buildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import net.minecraft.core.BlockPos;

public class PokeballWorkshopBuilding extends AbstractBuilding {
    public PokeballWorkshopBuilding(IColony colony, BlockPos pos) {
        super(colony, pos);
    }

    @Override
    public String getSchematicName() {
        return "colony_ball_workshop";
    }
}