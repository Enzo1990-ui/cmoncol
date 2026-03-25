package com.ogtenzohd.cmoncol.blocks.custom.pokecenter;

import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public class PokeCenterBlockEntity extends TileEntityColonyBuilding {

    private int gymResetTimer = 24000;
    private int spawnTimer = 6000;

    public PokeCenterBlockEntity(BlockPos pos, BlockState state) {
        super(CmoncolReg.POKECENTER_BE.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide || getBuilding() == null || !getBuilding().isBuilt()) return;
        if (gymResetTimer > 0) {
            gymResetTimer--;
            if (gymResetTimer <= 0) {
                resetGym();
            }
        }
        if (ModList.get().isLoaded("rctmod")) {
            if (spawnTimer > 0) {
                spawnTimer--;
            } else {
                spawnTimer = 6000 + this.level.random.nextInt(6000);
                spawnTrainer();
            }
        }
    }
    private void resetGym() {
        if (this.level == null || this.level.isClientSide || this.getBuilding() == null) return;

        com.minecolonies.api.colony.IColony colony = com.minecolonies.api.IMinecoloniesAPI.getInstance()
                .getColonyManager().getIColony(this.level, this.getBlockPos());

        if (colony == null) return;

        for (com.minecolonies.api.colony.buildings.IBuilding building : colony.getServerBuildingManager().getBuildings().values()) {
            net.minecraft.core.BlockPos buildPos = building.getPosition();

            if (level.getBlockEntity(buildPos) instanceof com.ogtenzohd.cmoncol.blocks.custom.gym.GymBlockEntity gym) {

                boolean wasSuccessful = gym.safeReset();

                if (wasSuccessful) {
                    int bLevel = Math.max(1, this.getBuilding().getBuildingLevel());
                    this.gymResetTimer = 24000 / bLevel;
                } else {
                    this.gymResetTimer = 1200;
                }

                this.setChanged();
                break;
            }
        }
    }


    private void spawnTrainer() {
        if (!(this.level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

        int bLevel = this.getBuilding().getBuildingLevel();
        String trainerId = getRandomTrainerId(serverLevel, bLevel);
        String command = String.format(java.util.Locale.US, "rctmod trainer summon %s %d %d %d",
                trainerId, worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ());

        serverLevel.getServer().getCommands().performPrefixedCommand(
                serverLevel.getServer().createCommandSourceStack().withPermission(4).withSuppressedOutput(),
                command
        );
    }

    private String getRandomTrainerId(net.minecraft.server.level.ServerLevel serverLevel, int bLevel) {
        try {
            CommandDispatcher<CommandSourceStack> dispatcher = serverLevel.getServer().getCommands().getDispatcher();
            CommandSourceStack source = serverLevel.getServer().createCommandSourceStack();

            ParseResults<CommandSourceStack> parse = dispatcher.parse("rctmod trainer summon ", source);
            Suggestions suggestions = dispatcher.getCompletionSuggestions(parse).join();

            List<String> allTrainers = new ArrayList<>();
            for (Suggestion suggestion : suggestions.getList()) {
                allTrainers.add(suggestion.getText());
            }

            if (allTrainers.isEmpty()) return "youngster_joey";

            List<String> filtered = new ArrayList<>();
            for (String id : allTrainers) {
                String lower = id.toLowerCase();

                if (bLevel == 1 && (lower.contains("youngster") || lower.contains("lass") || lower.contains("bug"))) {
                    filtered.add(id);
                } else if (bLevel == 2 && (lower.contains("camper") || lower.contains("picnicker") || lower.contains("hiker") || lower.contains("twins"))) {
                    filtered.add(id);
                } else if (bLevel == 3 && (lower.contains("ace") || lower.contains("veteran") || lower.contains("sailor") || lower.contains("blackbelt"))) {
                    filtered.add(id);
                } else if (bLevel >= 4 && (lower.contains("gym") || lower.contains("boss") || lower.contains("leader") || lower.contains("elite") || lower.contains("champion"))) {
                    filtered.add(id);
                }
            }

            if (filtered.isEmpty()) {
                filtered = allTrainers;
            }

            return filtered.get(serverLevel.random.nextInt(filtered.size()));

        } catch (Exception e) {
            return "youngster_austin_0303";
        }
    }
}