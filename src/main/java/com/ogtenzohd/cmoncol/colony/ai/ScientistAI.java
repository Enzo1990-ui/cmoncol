package com.ogtenzohd.cmoncol.colony.ai;

import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.ogtenzohd.cmoncol.colony.buildings.ScienceLabBuilding;
import com.ogtenzohd.cmoncol.colony.job.ScientistJob;
import com.ogtenzohd.cmoncol.util.ScienceLabLootTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.ogtenzohd.cmoncol.blocks.custom.sciencelab.ScienceLabBlockEntity;

public class ScientistAI extends AbstractEntityAIBasic<ScientistJob, ScienceLabBuilding> {

    private enum State { IDLE, CHECK_TOOLS, MOVE_TO_EDGE, ON_MISSION, RETURN_TO_LAB, DEPOSIT }
    private State currentState = State.IDLE;
    private BlockPos edgeTarget = null;
    
    private int totalMissionTime = 0;
    private int missionTimer = 0;
    
    private int totalRolls = 0;
    private int currentRollIndex = 0;
    private boolean isAmbushed = false;
    private final List<ItemStack> pendingLoot = new ArrayList<>();
    
    private final Random rand = new Random();
    private String selectedSite = "Prehistoric Birch Tree"; 

    public ScientistAI(ScientistJob job) { super(job); }
    @Override public Class<ScienceLabBuilding> getExpectedBuildingClass() { return ScienceLabBuilding.class; }

    @Override
    public void tick() {
        if (job.getWorkBuilding() == null || !job.getCitizen().getEntity().isPresent()) return;
        LivingEntity entity = job.getCitizen().getEntity().get();
        if (!(entity instanceof Mob mob)) return;
        
        if (job.getWorkBuilding() instanceof ScienceLabBuilding labBld) {
            selectedSite = labBld.getCurrentSite();
        }
        
        BlockPos labPos = job.getWorkBuilding().getPosition();

        if (job.getColony().getWorld().getBlockEntity(labPos) instanceof ScienceLabBlockEntity lab) {
            if (!lab.isExpeditionActive() && currentState != State.IDLE && currentState != State.DEPOSIT) {
                endMission(mob);
                currentState = State.DEPOSIT;
                return;
            }
        }

        switch (currentState) {
            case IDLE:
                if (job.getColony().getWorld().getBlockEntity(labPos) instanceof ScienceLabBlockEntity lab) {
                    if (lab.isExpeditionActive()) {
                        currentState = State.CHECK_TOOLS;
                        return;
                    }
                }
                if (mob.getNavigation().isDone() && rand.nextInt(30) == 0) {
                    BlockPos p = labPos.offset(rand.nextInt(7) - 3, 0, rand.nextInt(7) - 3);
                    mob.getNavigation().moveTo(p.getX(), p.getY(), p.getZ(), 0.6);
                }
                break;

            case CHECK_TOOLS:
                boolean hasShovel = hasTool(entity, Items.DIAMOND_SHOVEL);
                boolean hasPickaxe = hasTool(entity, Items.DIAMOND_PICKAXE);

                if (!hasShovel || !hasPickaxe) {
                    if (!hasShovel) checkIfRequestForItemExistOrCreateAsync(new ItemStack(Items.DIAMOND_SHOVEL));
                    if (!hasPickaxe) checkIfRequestForItemExistOrCreateAsync(new ItemStack(Items.DIAMOND_PICKAXE));
                    if (entity.distanceToSqr(labPos.getCenter()) > 9) {
                        mob.getNavigation().moveTo(labPos.getX(), labPos.getY(), labPos.getZ(), 1.0);
                    }
                    return; 
                }

                edgeTarget = findEdgeTarget(labPos);
                mob.getNavigation().moveTo(edgeTarget.getX(), edgeTarget.getY(), edgeTarget.getZ(), 1.0);
                currentState = State.MOVE_TO_EDGE;
                break;

            case MOVE_TO_EDGE:
                if (edgeTarget != null) {
                    if (entity.distanceToSqr(edgeTarget.getCenter()) <= 16 || mob.getNavigation().isDone()) {
                        startMission(mob);
                    } else if (entity.tickCount % 40 == 0) {
                        mob.getNavigation().moveTo(edgeTarget.getX(), edgeTarget.getY(), edgeTarget.getZ(), 1.0);
                    }
                } else {
                    startMission(mob);
                }
                break;

            case ON_MISSION:
                missionTimer--;
                if (missionTimer == (totalMissionTime * 4 / 5)) {
                    writeJournalLine(generateJourneyText());
                }
                if (missionTimer == (totalMissionTime * 3 / 5)) {
                    writeJournalLine(generateAtmosphereText());
                }
                if (!isAmbushed && currentRollIndex < totalRolls) {
                    int diggingWindowStart = totalMissionTime * 4 / 10;
                    int diggingWindowEnd = totalMissionTime / 10;
                    int timePerRoll = (diggingWindowStart - diggingWindowEnd) / Math.max(1, totalRolls);
                    
                    int triggerTimeForThisRoll = diggingWindowStart - (timePerRoll * currentRollIndex);
                    
                    if (missionTimer == triggerTimeForThisRoll) {
                        processSingleDig(mob);
                        currentRollIndex++;
                    }
                }
                if (isAmbushed && missionTimer == (totalMissionTime / 4)) {
                    mob.hurt(mob.damageSources().mobAttack(null), 6.0f); 
                    mob.sendSystemMessage(Component.literal("§cScientist was attacked at the dig site!"));
                    writeJournalLine(generateAmbushText());
                    if (!pendingLoot.isEmpty() && rand.nextBoolean()) {
                        ItemStack lost = pendingLoot.remove(rand.nextInt(pendingLoot.size()));
                        writeJournalLine("In the chaos, I dropped " + lost.getCount() + "x " + lost.getHoverName().getString() + "!");
                    }
                }
                if (missionTimer <= 0) {
                    endMission(mob); 
                    finishMissionOutcome(mob);
                    mob.getNavigation().moveTo(labPos.getX(), labPos.getY(), labPos.getZ(), 1.0);
                    currentState = State.RETURN_TO_LAB;
                }
                break;

            case RETURN_TO_LAB:
                if (entity.distanceToSqr(labPos.getCenter()) <= 16 || mob.getNavigation().isDone()) {
                    mob.getNavigation().stop();
                    currentState = State.DEPOSIT;
                } else if (entity.tickCount % 40 == 0) {
                    mob.getNavigation().moveTo(labPos.getX(), labPos.getY(), labPos.getZ(), 1.0);
                }
                break;

            case DEPOSIT:
                depositItems(entity);
                finalizeJournal(entity); 
                
                if (job.getColony().getWorld().getBlockEntity(labPos) instanceof ScienceLabBlockEntity lab) {
                    lab.setExpeditionActive(false);
                }
                currentState = State.IDLE;
                break;
        }
    }

    private BlockPos findEdgeTarget(BlockPos center) {
        int dx = (rand.nextBoolean() ? 15 : -15) + rand.nextInt(5);
        int dz = (rand.nextBoolean() ? 15 : -15) + rand.nextInt(5);
        int y = job.getColony().getWorld().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.getX() + dx, center.getZ() + dz);
        return new BlockPos(center.getX() + dx, y, center.getZ() + dz);
    }

    private int getSkillLevel(Skill skill) {
        if (job.getCitizen() == null || job.getCitizen().getCitizenSkillHandler() == null) return 1;
        return job.getCitizen().getCitizenSkillHandler().getLevel(skill);
    }

    private void startMission(Mob mob) {
        mob.setInvisible(true);
        mob.setInvulnerable(true); 
        if (!mob.getMainHandItem().isEmpty()) {
			insertItemIntoPockets(mob, mob.getMainHandItem());
			mob.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		}
		if (!mob.getOffhandItem().isEmpty()) {
			insertItemIntoPockets(mob, mob.getOffhandItem());
			mob.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
		}
        
        totalMissionTime = 250 + rand.nextInt(100); 
        missionTimer = totalMissionTime;
        
        mob.sendSystemMessage(Component.literal("§eScientist departing for: " + selectedSite + "..."));
        
        currentExpeditionStory = new StringBuilder();
        writeJournalLine(generateDepartureText()); 
        
        int skill = getSkillLevel(Skill.Intelligence); 
        int safetyDice = 6 + (skill / 7); 
        
        isAmbushed = (rand.nextInt(safetyDice) == 0);
        pendingLoot.clear();
        currentRollIndex = 0;
        
        if (isAmbushed) {
            totalRolls = 1 + rand.nextInt(2);
        } else {
            int bonusItems = skill / 20; 
            totalRolls = 2 + bonusItems + rand.nextInt(3); 
        }
        
        currentState = State.ON_MISSION;
    }

    private void endMission(Mob mob) {
        mob.setInvisible(false);
        mob.setInvulnerable(false);
        mob.swing(InteractionHand.MAIN_HAND);
    }

    private void processSingleDig(Mob mob) {
        ItemStack loot = ScienceLabLootTable.getRandomLoot(selectedSite);
        if (!loot.isEmpty()) {
            pendingLoot.add(loot);
            String itemName = loot.getHoverName().getString();
            writeJournalLine(generateActionSetupText() +" and found " + loot.getCount() + "x " + itemName + ".");
        } else {
            writeJournalLine(generateActionSetupText() + " I thought i saw something but didn't find anything useful here.");
        }
    }

    private void finishMissionOutcome(Mob mob) {
        Map<String, Integer> finalLootCounts = new HashMap<>();
        String rarestItem = "";
        int minCount = 999;
        
        for (ItemStack stack : pendingLoot) {
            insertItemIntoPockets(mob, stack);
            String name = stack.getHoverName().getString();
            finalLootCounts.put(name, finalLootCounts.getOrDefault(name, 0) + stack.getCount());
        }
        
        for (Map.Entry<String, Integer> entry : finalLootCounts.entrySet()) {
            if (entry.getValue() < minCount) {
                rarestItem = entry.getKey();
                minCount = entry.getValue();
            }
        }
        
        if (isAmbushed) {
            writeJournalLine("I am finally heading home. That was far too close. I need to rest.");
        } else if (!finalLootCounts.isEmpty()) {
            writeJournalLine("I'm packing up the equipment and heading home. Overall, it was a fantastic trip.");
            
            StringBuilder summary = new StringBuilder("In total, I recovered: ");
            for (Map.Entry<String, Integer> entry : finalLootCounts.entrySet()) {
                summary.append(entry.getValue()).append("x ").append(entry.getKey()).append(", ");
            }
            summary.setLength(summary.length() - 2); 
            writeJournalLine(summary.toString() + ".");
            writeJournalLine("The crown jewel of this trip is definitely the " + rarestItem + ".");
            
        } else {
            writeJournalLine("I'm heading home empty-handed. Not every trip can be a winner, I suppose.");
        }

        mob.sendSystemMessage(Component.literal("§aScientist returned safely!"));
        
        if (job.getCitizen().getCitizenSkillHandler() != null) {
            job.getCitizen().getCitizenSkillHandler().addXpToSkill(Skill.Intelligence, 5.0, job.getCitizen());
        }
    }

    // the simple logs are boring so lets create a story
    private StringBuilder currentExpeditionStory = new StringBuilder();

    private void writeJournalLine(String message) {
        currentExpeditionStory.append(message).append("\n\n"); 
        
        if (job.getColony().getWorld() != null && job.getWorkBuilding() != null) {
            BlockEntity be = job.getColony().getWorld().getBlockEntity(job.getWorkBuilding().getPosition());
            if (be instanceof ScienceLabBlockEntity lab) {
                lab.updateLiveStory(currentExpeditionStory.toString().trim());
            }
        }
    }

    private void finalizeJournal(LivingEntity entity) {
        if (job.getColony().getWorld() != null && job.getWorkBuilding() != null) {
            BlockEntity be = job.getColony().getWorld().getBlockEntity(job.getWorkBuilding().getPosition());
            if (be instanceof ScienceLabBlockEntity lab) {
                long day = entity.level().getDayTime() / 24000L;
                String header = "--- Expedition " + lab.getTotalExpeditions() + " [Day " + day + "] ---\n";
                lab.addJournalEntry(header + currentExpeditionStory.toString().trim());
                
                lab.clearLiveStory();
                currentExpeditionStory = new StringBuilder();
            }
        }
    }

    private String generateDepartureText() {
        String[] departures = { "I am departing for", "Setting off towards", "Packing my gear and heading to", "Beginning my trek to" };
        return departures[rand.nextInt(departures.length)] + " the " + selectedSite + ".";
    }
    
    private String generateJourneyText() {
        String[] journey = { 
            "This walk is treacherous, I have almost slipped down these mountains multiple times.", 
            "This journey is so peaceful, I keep seeing wild Pokémon and beautiful scenery.", 
            "Wait did I just see... No it can't be, I thought I saw a Legendary Pokémon!" 
        };
        return journey[rand.nextInt(journey.length)];
    }
        
    private String generateAtmosphereText() {
        String[] weather = { "The sun is beating down on", "A thick fog covers", "A gentle breeze sweeps through", "An eerie silence fills", "Rain gently washes over" };
        String[] feeling = { "I feel a sense of profound history here.", "It makes the hairs on my neck stand up.", "Perfect conditions for a breakthrough.", "I can't shake the feeling I'm being watched.", "It's peaceful, almost too peaceful." };
        return weather[rand.nextInt(weather.length)] + " the site. " + feeling[rand.nextInt(feeling.length)];
    }

    private String generateActionSetupText() {
        String[] actions = { "While brushing away some loose dirt...", "After digging a small test trench...", "I was examining a strange rock formation when...", "I set up the perimeter and got to work." };
        return actions[rand.nextInt(actions.length)];
    }

    private String generateAmbushText() {
        String[] threats = { "A wild Pokémon leaped from the shadows!", "The ground suddenly gave way!", "A swarm of angry bug-types descended!", "I was ambushed by something highly territorial!" };
        String[] escapes = { "I barely managed to sprint back to safety.", "I had to abandon my camp and run for it.", "I narrowly escaped the attack.", "I took a nasty hit and had to fall back immediately." };
        return threats[rand.nextInt(threats.length)] + " " + escapes[rand.nextInt(escapes.length)];
    }


    // Need to ad more through out the mods life

    private boolean hasTool(LivingEntity entity, net.minecraft.world.item.Item tool) {
        IItemHandler inv = entity.getCapability(Capabilities.ItemHandler.ENTITY, null);
        if (inv != null) {
            for (int i = 0; i < inv.getSlots(); i++) {
                if (inv.getStackInSlot(i).getItem() == tool) return true;
            }
        }
        return false;
    }

    private void insertItemIntoPockets(LivingEntity entity, ItemStack stack) {
        IItemHandler citizenInv = entity.getCapability(Capabilities.ItemHandler.ENTITY, null);
        if (citizenInv != null) {
            ItemStack remaining = ItemHandlerHelper.insertItemStacked(citizenInv, stack, false);
            if (!remaining.isEmpty()) net.minecraft.world.Containers.dropItemStack(entity.level(), entity.getX(), entity.getY(), entity.getZ(), remaining);
        }
    }

    private void depositItems(LivingEntity entity) {
        IItemHandler citizenInv = entity.getCapability(Capabilities.ItemHandler.ENTITY, null);
        IItemHandler buildingInv = job.getWorkBuilding().getItemHandlerCap();
        if (citizenInv != null && buildingInv != null) {
            for (int i = 0; i < citizenInv.getSlots(); i++) {
                ItemStack stack = citizenInv.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() != Items.DIAMOND_SHOVEL && stack.getItem() != Items.DIAMOND_PICKAXE) {
                    ItemStack remaining = ItemHandlerHelper.insertItemStacked(buildingInv, stack, false);
                    citizenInv.extractItem(i, stack.getCount() - remaining.getCount(), false);
                }
            }
        }
    }
}