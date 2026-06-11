package com.ogtenzohd.cmoncol.util;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class DummyEntityHelper {
    public static final String DUMMY_TAG = "cmoncol_dummy";

    public static String getBuildingTag(BlockPos pos) {
        return "cmoncol_origin_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
    }

    /**
     * Scans a radius discards ANY PokemonEntity
     * that has the dummy tag AND matches this building's specific origin tag.
     * * @param level        The server level
     * @param searchCenter The center of the search (e.g., garden center or building pos)
     * @param buildingPos  The exact block pos of the BlockEntity (used for the tag)
     * @param searchRadius How far out to sweep (e.g., 48 blocks)
     */

    public static void clearGhostsForBuilding(ServerLevel level, BlockPos searchCenter, BlockPos buildingPos, int searchRadius) {
        AABB scanArea = new AABB(searchCenter).inflate(searchRadius);
        String myBuildingTag = getBuildingTag(buildingPos);

        List<PokemonEntity> ghosts = level.getEntitiesOfClass(PokemonEntity.class, scanArea, entity -> {
            return entity.getTags().contains(DUMMY_TAG) && entity.getTags().contains(myBuildingTag);
        });

        for (PokemonEntity ghost : ghosts) {
            ghost.discard();
        }
    }

    public static void applyDummyTags(Entity entity, BlockPos buildingPos) {
        entity.getTags().add(DUMMY_TAG);
        entity.getTags().add(getBuildingTag(buildingPos));
    }
}