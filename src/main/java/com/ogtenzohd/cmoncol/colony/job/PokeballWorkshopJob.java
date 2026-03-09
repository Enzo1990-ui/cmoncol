package com.ogtenzohd.cmoncol.colony.job;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.ogtenzohd.cmoncol.colony.buildings.PokeballWorkshopBuilding;
import com.ogtenzohd.cmoncol.colony.CmoncolRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

// this may or may not work, i need to change it to a crafterAI, but need more researc, as i havnt made a crafterAI yet!

public class PokeballWorkshopJob extends AbstractJob<PokeballWorkshopJob.PokeballWorkshopAI, PokeballWorkshopJob> {

    public PokeballWorkshopJob(ICitizenData citizenData) {
        super(citizenData);
        this.setRegistryEntry(CmoncolRegistries.POKEBALLWORKSHOP_JOB_ENTRY);
    }

    @Override
    @NotNull
    public PokeballWorkshopAI generateAI() {
        return new PokeballWorkshopAI(this);
    }

    @Override 
    public String getNameTagDescription() { return "Ball Smith"; }

    public static class PokeballWorkshopAI extends AbstractEntityAIBasic<PokeballWorkshopJob, PokeballWorkshopBuilding> {
        
        private int craftTimer = 0;
        private ItemStack pendingResult = ItemStack.EMPTY;

        public PokeballWorkshopAI(PokeballWorkshopJob job) { super(job); }
        @Override public Class<PokeballWorkshopBuilding> getExpectedBuildingClass() { return PokeballWorkshopBuilding.class; }

        @Override
        public void tick() {
            if (job.getWorkBuilding() == null || !job.getCitizen().getEntity().isPresent()) return;
            var entity = job.getCitizen().getEntity().get();

            if (craftTimer > 0) {
                craftTimer--;
                if (craftTimer % 20 == 0) entity.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                if (craftTimer == 0) completeCrafting();
                return;
            }

            checkForRecipe();
        }

        private void checkForRecipe() {
            int level = job.getWorkBuilding().getBuildingLevel();
            
            // Level 5: Master Ball
            if (level >= 5 && tryCraft("cobblemon:master_ball", Items.DIAMOND)) return; 

            // Level 4: Special Balls (All colors)
            if (level >= 4) {
                // Red: Level, Repeat
                if (tryCraft("cobblemon:level_ball", "cobblemon:red_apricorn")) return;
                if (tryCraft("cobblemon:repeat_ball", "cobblemon:red_apricorn")) return;
                
                // Yellow: Moon, Fast
                if (tryCraft("cobblemon:moon_ball", "cobblemon:yellow_apricorn")) return;
                if (tryCraft("cobblemon:fast_ball", "cobblemon:yellow_apricorn")) return;
                
                // Blue: Dive, Net, Lure
                if (tryCraft("cobblemon:dive_ball", "cobblemon:blue_apricorn")) return;
                if (tryCraft("cobblemon:net_ball", "cobblemon:blue_apricorn")) return;
                if (tryCraft("cobblemon:lure_ball", "cobblemon:blue_apricorn")) return;
                
                // Green: Friend, Nest, Safari
                if (tryCraft("cobblemon:friend_ball", "cobblemon:green_apricorn")) return;
                if (tryCraft("cobblemon:nest_ball", "cobblemon:green_apricorn")) return;
                if (tryCraft("cobblemon:safari_ball", "cobblemon:green_apricorn")) return;
                
                // Black: Heavy, Luxury, Dusk
                if (tryCraft("cobblemon:heavy_ball", "cobblemon:black_apricorn")) return;
                if (tryCraft("cobblemon:luxury_ball", "cobblemon:black_apricorn")) return;
                if (tryCraft("cobblemon:dusk_ball", "cobblemon:black_apricorn")) return;
                
                // Pink: Love, Heal, Dream
                if (tryCraft("cobblemon:love_ball", "cobblemon:pink_apricorn")) return;
                if (tryCraft("cobblemon:heal_ball", "cobblemon:pink_apricorn")) return;
                if (tryCraft("cobblemon:dream_ball", "cobblemon:pink_apricorn")) return;
                
                // White: Timer, Premier
                if (tryCraft("cobblemon:timer_ball", "cobblemon:white_apricorn")) return;
                if (tryCraft("cobblemon:premier_ball", "cobblemon:white_apricorn")) return;
            }

            // Level 3: Ultra Ball
            if (level >= 3 && tryCraft("cobblemon:ultra_ball", "cobblemon:black_apricorn")) return;
            
            // Level 2: Great Ball
            if (level >= 2 && tryCraft("cobblemon:great_ball", "cobblemon:blue_apricorn")) return;
            
            // Level 1: Poke Ball
            if (level >= 1 && tryCraft("cobblemon:poke_ball", "cobblemon:red_apricorn")) return;
        }
		

        private boolean tryCraft(String ballId, Object ingredient) {
    Item ballItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(ballId));
    Item ingItem = (ingredient instanceof Item) ? (Item) ingredient : BuiltInRegistries.ITEM.get(ResourceLocation.parse((String) ingredient));
    
    if (ballItem == Items.AIR || ingItem == null) return false;

    boolean hasIng = hasItem(ingItem);
    boolean hasIron = hasItem(Items.IRON_INGOT);

    if (hasIng && hasIron) {
        consumeItem(ingItem);
        consumeItem(Items.IRON_INGOT);
        
        int dexterity = job.getCitizen().getCitizenSkillHandler().getLevel(Skill.Dexterity);
        craftTimer = Math.max(20, 100 - (dexterity * 2));
        
        this.pendingResult = new ItemStack(ballItem);
        return true; 
    } else {
        if (!hasIng) checkIfRequestForItemExistOrCreateAsync(new ItemStack(ingItem, 16));
        if (!hasIron) checkIfRequestForItemExistOrCreateAsync(new ItemStack(Items.IRON_INGOT, 16));
        return false;
    }
}

        private void completeCrafting() {
            if (!pendingResult.isEmpty()) {
                insertItemIntoBuilding(pendingResult);
                pendingResult = ItemStack.EMPTY;
            }
        }

        private boolean hasItem(Item item) {
            IItemHandler inv = job.getWorkBuilding().getItemHandlerCap();
            if (inv == null) return false;
            for(int i=0; i<inv.getSlots(); i++) if(inv.getStackInSlot(i).getItem() == item) return true;
            return false;
        }
        
        private void consumeItem(Item item) {
            IItemHandler inv = job.getWorkBuilding().getItemHandlerCap();
            if (inv == null) return;
            for(int i=0; i<inv.getSlots(); i++) {
                if(inv.getStackInSlot(i).getItem() == item) {
                    inv.extractItem(i, 1, false);
                    return;
                }
            }
        }

        private void insertItemIntoBuilding(ItemStack stack) {
            IItemHandler inv = job.getWorkBuilding().getItemHandlerCap();
            if (inv != null) ItemHandlerHelper.insertItemStacked(inv, stack, false);
        }
    }
}