package com.ogtenzohd.cmoncol.colony.buildings;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import net.minecraft.core.BlockPos;

public class TrainerAcadamyBuilding extends AbstractBuilding {
    public TrainerAcadamyBuilding(IColony colony, BlockPos pos) { super(colony, pos); }
    @Override public String getSchematicName() { return "colony_traineracademy"; }
}