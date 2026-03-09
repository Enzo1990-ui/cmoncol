package com.ogtenzohd.cmoncol.colony.ai;

import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.ogtenzohd.cmoncol.blocks.custom.daycare.DaycareBlockEntity;
import com.ogtenzohd.cmoncol.colony.buildings.DaycareBuilding;
import com.ogtenzohd.cmoncol.colony.job.AttendantJob;
import com.ogtenzohd.cmoncol.config.CCConfig;
import com.ogtenzohd.cmoncol.items.ItemPokemonEgg; 
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.experience.SidemodExperienceSource;

import java.util.*;


public class AttendantAI extends AbstractEntityAIBasic<AttendantJob, DaycareBuilding> {
    
    private enum State { MOVE_TO_DAYCARE, CHECK_SLOTS, TRAIN_POKEMON }
    private State currentState = State.MOVE_TO_DAYCARE;
    private int workTimer = 0;

    public AttendantAI(AttendantJob job) { super(job); }
    @Override public Class<DaycareBuilding> getExpectedBuildingClass() { return DaycareBuilding.class; }

    public void tick() {
        if (job.getWorkBuilding() == null || job.getColony().getWorld() == null) return;
        
        job.getCitizen().getEntity().ifPresent(entity -> {
            if (!entity.level().isDay() || entity.level().isRaining()) return;
            BlockPos buildingPos = job.getWorkBuilding().getPosition();

            switch (currentState) {
                case MOVE_TO_DAYCARE:
                    if (entity.distanceToSqr(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ()) > 9) {
                        entity.getNavigation().moveTo(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ(), 1.0);
                    } else { 
                        entity.getNavigation().stop();
                        currentState = State.CHECK_SLOTS; 
                    }
                    break;

                case CHECK_SLOTS:
                    BlockEntity be = entity.level().getBlockEntity(buildingPos);
                    if (be instanceof DaycareBlockEntity daycare && !daycare.getStoredPokemon().isEmpty()) {
                        workTimer = 100; 
                        currentState = State.TRAIN_POKEMON;
                    }
                    break;

                case TRAIN_POKEMON:
                    workTimer--;
                    if (entity.getRandom().nextFloat() < 0.05f) {
                         entity.getLookControl().setLookAt(buildingPos.getX(), buildingPos.getY(), buildingPos.getZ());
                    }
                    
                    if (workTimer <= 0) {
                        trainAllPokemon(entity, buildingPos);
                        currentState = State.MOVE_TO_DAYCARE;
                    }
                    break;
            }
        });
    }

    private void trainAllPokemon(LivingEntity entity, BlockPos buildingPos) {
        BlockEntity be = entity.level().getBlockEntity(buildingPos);
        if (be instanceof DaycareBlockEntity daycare) {
            
            net.minecraft.core.RegistryAccess registryAccess = entity.level().registryAccess();

            for (DaycareBlockEntity.DaycareSlot slot : daycare.getStoredPokemon()) {
                Pokemon storedMon = new Pokemon();
                storedMon.loadFromNBT(registryAccess, slot.pokemonNBT);
                storedMon.addExperience(new SidemodExperienceSource("cmoncol"), CCConfig.INSTANCE.xpPerTick.get());
                slot.timeInDaycare++;
                slot.pokemonNBT = storedMon.saveToNBT(registryAccess, new CompoundTag());
            }
            
            attemptBreeding(daycare, registryAccess, entity);
            daycare.setChanged();
        }
    }

    private void attemptBreeding(DaycareBlockEntity daycare, RegistryAccess registryAccess, LivingEntity entity) {
        List<DaycareBlockEntity.DaycareSlot> slots = daycare.getStoredPokemon();
        if (slots.size() < 2) return;

        for (int i = 0; i < slots.size(); i++) {
            for (int j = i + 1; j < slots.size(); j++) {
                DaycareBlockEntity.DaycareSlot slotA = slots.get(i);
                DaycareBlockEntity.DaycareSlot slotB = slots.get(j);
                
                if (slotA.timeInDaycare < CCConfig.INSTANCE.eggCycleThreshold.get() || slotB.timeInDaycare < CCConfig.INSTANCE.eggCycleThreshold.get()) continue;
                
                Pokemon monA = new Pokemon(); monA.loadFromNBT(registryAccess, slotA.pokemonNBT);
                Pokemon monB = new Pokemon(); monB.loadFromNBT(registryAccess, slotB.pokemonNBT);
                
                if (areCompatible(monA, monB)) {
                    if (entity.getRandom().nextDouble() <= CCConfig.INSTANCE.eggGenerationChance.get()) {
                        
                        Pokemon child = createOffspring(monA, monB);
                        if (child != null) {
                            ItemStack eggStack = ItemPokemonEgg.createEgg(child);
                            CompoundTag childNBT = child.saveToNBT(registryAccess, new CompoundTag());
                            ItemPokemonEgg.setPokemonData(eggStack, childNBT);
                            job.getColony().getWorld().getServer().getPlayerList().getPlayers().forEach(player -> {
                                if (player.getUUID().equals(slotA.ownerUUID)) {
                                    player.sendSystemMessage(Component.literal("§a[Daycare] §fYour Pokemon have produced an egg!"));
                                }
                            });
                            if (insertItemIntoBuilding(eggStack, entity)) {
                                slotA.timeInDaycare = 0; 
                                slotB.timeInDaycare = 0;
                            }
                        }
                    }
                    return;
                }
            }
        }
    }

    private Pokemon createOffspring(Pokemon parentA, Pokemon parentB) {
        Pokemon nonDitto = parentA.getSpecies().getName().equalsIgnoreCase("ditto") ? parentB : parentA;
        Pokemon mother = (parentA.getGender() == Gender.FEMALE || parentB.getSpecies().getName().equalsIgnoreCase("ditto")) ? parentA : parentB;
        
        com.cobblemon.mod.common.pokemon.Species baseSpecies = nonDitto.getSpecies();
        while (baseSpecies.getPreEvolution() != null) {
            baseSpecies = baseSpecies.getPreEvolution().getSpecies();
        }
        String speciesId = baseSpecies.getResourceIdentifier().getPath();

        String itemA = getHeldItemId(parentA);
        String itemB = getHeldItemId(parentB);
        
        CCConfig.BreedingMode mode = CCConfig.INSTANCE.breedingMode.get();
        boolean isMasuda = !Objects.equals(parentA.getOriginalTrainer(), parentB.getOriginalTrainer());
        
        double baseShinyRate = 1.0 / 4096.0;
        int shinyRolls = isMasuda ? 6 : 1;
        
        if (mode == CCConfig.BreedingMode.CLASSIC) {
            baseShinyRate = 1.0 / 8192.0;
            shinyRolls = isMasuda ? 5 : 1;
        } else if (mode == CCConfig.BreedingMode.EASY) {
            baseShinyRate = 1.0 / 2048.0;
        }

        shinyRolls += getShinyItemBoost(itemA);
        shinyRolls += getShinyItemBoost(itemB);

        boolean forceShiny = false;
        
        if (mode == CCConfig.BreedingMode.EASY && (itemA.equals("minecraft:nether_star") || itemB.equals("minecraft:nether_star"))) {
            forceShiny = true;
        } else {
            for (int i = 0; i < shinyRolls; i++) {
                if (Math.random() < baseShinyRate) {
                    forceShiny = true;
                    break;
                }
            }
        }

        String propsString = "species=" + speciesId + " level=1";
        if (forceShiny) propsString += " shiny=yes";
        Pokemon child = PokemonProperties.Companion.parse(propsString).create();

        boolean aHasEverstone = itemA.equals("minecraft:everstone");
        boolean bHasEverstone = itemB.equals("minecraft:everstone");
        
        if (mode == CCConfig.BreedingMode.EASY) {
            child.setNature((Math.random() < 0.5) ? parentA.getNature() : parentB.getNature());
        } else if (mode == CCConfig.BreedingMode.CLASSIC) {
            if (aHasEverstone && Math.random() < 0.5) child.setNature(parentA.getNature());
            else if (bHasEverstone && Math.random() < 0.5) child.setNature(parentB.getNature());
        } else {
            if (aHasEverstone && bHasEverstone) child.setNature((Math.random() < 0.5) ? parentA.getNature() : parentB.getNature());
            else if (aHasEverstone) child.setNature(parentA.getNature());
            else if (bHasEverstone) child.setNature(parentB.getNature());
        }

        int inheritedStatsCount = 3;
        
        if (mode == CCConfig.BreedingMode.EASY) {
            inheritedStatsCount = 5;
        } else if (mode == CCConfig.BreedingMode.MODERN) {
            if (itemA.equals("cobblemon:destiny_knot") || itemB.equals("cobblemon:destiny_knot")) {
                inheritedStatsCount = 5;
            }
        }

        List<Stat> availableStats = new ArrayList<>(Arrays.asList(Stats.HP, Stats.ATTACK, Stats.DEFENCE, Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED));
        
        handlePowerItem(itemA, parentA, child, availableStats);
        handlePowerItem(itemB, parentB, child, availableStats);
        
        Collections.shuffle(availableStats);
        for (int i = 0; i < Math.min(inheritedStatsCount, availableStats.size()); i++) {
            Stat stat = availableStats.get(i);
            Pokemon ivParent = (Math.random() < 0.5) ? parentA : parentB;
            child.getIvs().set(stat, ivParent.getIvs().getOrDefault(stat));
        }

        if (mode != CCConfig.BreedingMode.CLASSIC) {
            if (mother.getCaughtBall() != null) {
                String ballName = mother.getCaughtBall().toString();
                if (!ballName.contains("master_ball") && !ballName.contains("cherish_ball")) {
                    child.setCaughtBall(mother.getCaughtBall());
                }
            }
        }

        return child;
    }

    private String getHeldItemId(Pokemon pokemon) {
        ItemStack item = pokemon.heldItem();
        if (item == null || item.isEmpty()) return "";
        return BuiltInRegistries.ITEM.getKey(item.getItem()).toString();
    }

    private int getShinyItemBoost(String itemId) {
        switch (itemId) {
            case "minecraft:amethyst_shard": return 2;
            case "minecraft:emerald": return 4;
            case "minecraft:diamond": return 8;
            case "minecraft:nether_star": return 15;
            default: return 0;
        }
    }

    private void handlePowerItem(String itemId, Pokemon parent, Pokemon child, List<Stat> availableStats) {
        Stat targetStat = null;
        switch (itemId) {
            case "cobblemon:power_weight": targetStat = Stats.HP; break;
            case "cobblemon:power_bracer": targetStat = Stats.ATTACK; break;
            case "cobblemon:power_belt": targetStat = Stats.DEFENCE; break;
            case "cobblemon:power_lens": targetStat = Stats.SPECIAL_ATTACK; break;
            case "cobblemon:power_band": targetStat = Stats.SPECIAL_DEFENCE; break;
            case "cobblemon:power_anklet": targetStat = Stats.SPEED; break;
        }
        
        if (targetStat != null && availableStats.contains(targetStat)) {
            child.getIvs().set(targetStat, parent.getIvs().getOrDefault(targetStat));
            availableStats.remove(targetStat);
        }
    }

    private boolean areCompatible(Pokemon a, Pokemon b) {
        if (a.getForm().getEggGroups().contains(EggGroup.UNDISCOVERED) || b.getForm().getEggGroups().contains(EggGroup.UNDISCOVERED)) return false;
        
        boolean aIsDitto = a.getSpecies().getName().equalsIgnoreCase("ditto");
        boolean bIsDitto = b.getSpecies().getName().equalsIgnoreCase("ditto");
        if (aIsDitto || bIsDitto) {
            return !a.getSpecies().getName().equalsIgnoreCase(b.getSpecies().getName());
        }

        if (a.getGender() != Gender.GENDERLESS && b.getGender() != Gender.GENDERLESS && a.getGender() == b.getGender()) return false;
        
        for (EggGroup group : a.getForm().getEggGroups()) { 
            if (b.getForm().getEggGroups().contains(group)) return true; 
        }
        return false;
    }

    private boolean insertItemIntoBuilding(ItemStack stack, LivingEntity entity) {
        if (stack.isEmpty()) return false;
        
        IItemHandler buildingInv = job.getWorkBuilding().getItemHandlerCap();
        if (buildingInv != null) {
            ItemStack remaining = ItemHandlerHelper.insertItemStacked(buildingInv, stack, false);
            if (remaining.isEmpty()) return true;
            stack = remaining; 
        }
        
        IItemHandler citizenInv = entity.getCapability(Capabilities.ItemHandler.ENTITY, null);
        if (citizenInv != null) {
            ItemStack remaining = ItemHandlerHelper.insertItemStacked(citizenInv, stack, false);
            if (remaining.isEmpty()) return true;
            stack = remaining;
        }
        
        Containers.dropItemStack(job.getColony().getWorld(), entity.getX(), entity.getY(), entity.getZ(), stack);
        return true;
    }
}