package com.ogtenzohd.cmoncol.colony.buildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import net.minecraft.core.BlockPos;

public class HarvesterBuilding extends AbstractBuilding {
    public HarvesterBuilding(IColony colony, BlockPos pos) {
        super(colony, pos);
    }
    
    @Override
    public String getSchematicName() {
        return "colony_harvester";
    }
}