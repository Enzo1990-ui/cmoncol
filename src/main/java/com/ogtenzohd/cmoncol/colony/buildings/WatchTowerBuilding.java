package com.ogtenzohd.cmoncol.colony.buildings;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import net.minecraft.core.BlockPos;

public class WatchTowerBuilding extends AbstractBuilding {
    public WatchTowerBuilding(IColony colony, BlockPos pos) { super(colony, pos); }

    @Override public String getSchematicName() {
        return "colony_ranger";
    }

}