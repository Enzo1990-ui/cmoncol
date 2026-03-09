package com.ogtenzohd.cmoncol.blocks.custom.pokecenter;

import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.commands.CommandSourceStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PokeCenterBlockEntity extends TileEntityColonyBuilding {
    private int spawnTimer = 0;
    private final Random rand = new Random();

    public PokeCenterBlockEntity(BlockPos pos, BlockState state) {
        super(CmoncolReg.POKECENTER_BE.get(), pos, state);
    }

	// i want this but dont want to use RCTMod so i need to figure out something else out
	
	
	
    //public void tick() {
    //    if (level == null || level.isClientSide || getBuilding() == null || !getBuilding().isBuilt()) return;
    //    if (spawnTimer-- <= 0) {
    //        spawnTimer = 6000 + rand.nextInt(6000);
    //        spawnTrainer();
    //    }
    //}

    //private void spawnTrainer() {
    //    if (getBuilding() == null || !(this.level instanceof ServerLevel serverLevel)) return;
    //    int bLevel = getBuilding().getBuildingLevel();
    //    String trainerId = getRandomTrainerId(serverLevel, bLevel);
        
    //    String command = "rctmod trainer summon " + trainerId + " " + worldPosition.getX() + " " + (worldPosition.getY() + 1) + " " + worldPosition.getZ();
        
    //    serverLevel.getServer().getCommands().performPrefixedCommand(
     //       serverLevel.getServer().createCommandSourceStack().withPermission(4).withSuppressedOutput(), 
    //        command
     //   );
   // }

    /**
     * Secretly queries the server's command auto-complete to get every single loaded trainer ID!
     */
    //private String getRandomTrainerId(ServerLevel serverLevel, int bLevel) {
     //   try {
    //        CommandDispatcher<CommandSourceStack> dispatcher = serverLevel.getServer().getCommands().getDispatcher();
    //        CommandSourceStack source = serverLevel.getServer().createCommandSourceStack();

     //       ParseResults<CommandSourceStack> parse = dispatcher.parse("rctmod trainer summon ", source);
     //       Suggestions suggestions = dispatcher.getCompletionSuggestions(parse).join(); 
            
    //        List<String> allTrainers = new ArrayList<>();
    //        for (Suggestion suggestion : suggestions.getList()) {
    //            allTrainers.add(suggestion.getText());
    //        }

     //       if (allTrainers.isEmpty()) return "youngster_joey"; // Ultimate Safety fallback

    //        List<String> filtered = new ArrayList<>();
    //        for (String id : allTrainers) {
    //            String lower = id.toLowerCase();
                
    //            if (bLevel == 1 && (lower.contains("youngster") || lower.contains("lass") || lower.contains("bug"))) {
    //                filtered.add(id);
    //            } else if (bLevel == 2 && (lower.contains("camper") || lower.contains("picnicker") || lower.contains("hiker") || lower.contains("twins"))) {
    //                filtered.add(id);
    //            } else if (bLevel == 3 && (lower.contains("ace") || lower.contains("veteran") || lower.contains("sailor") || lower.contains("blackbelt"))) {
    //                filtered.add(id);
     //           } else if (bLevel >= 4 && (lower.contains("gym") || lower.contains("boss") || lower.contains("leader") || lower.contains("elite") || lower.contains("champion"))) {
      //              filtered.add(id);
     //           }
     //       }

      //      if (filtered.isEmpty()) {
     //           filtered = allTrainers;
     //       }

      //      return filtered.get(rand.nextInt(filtered.size()));

      //  } catch (Exception e) {
       //     return "youngster_austin_0303"; 
      //  }
  //  }
}