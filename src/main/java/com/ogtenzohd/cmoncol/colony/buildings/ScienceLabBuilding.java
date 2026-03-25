package com.ogtenzohd.cmoncol.colony.buildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.ogtenzohd.cmoncol.blocks.custom.sciencelab.ScienceLabBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ScienceLabBuilding extends AbstractBuilding {
    
    public ScienceLabBuilding(IColony colony, BlockPos pos) {
        super(colony, pos);
    }
    
    @Override
    public String getSchematicName() { return "colony_science_lab"; }

    public String getCurrentSite() {
        if (getColony() == null || getColony().getWorld() == null) return "Loading...";
        
        BlockEntity be = getColony().getWorld().getBlockEntity(getPosition());
        if (be instanceof ScienceLabBlockEntity lab) {
            return lab.getDigSiteName();
        }
        return "Prehistoric Birch Tree";
    }

}