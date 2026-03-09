package com.ogtenzohd.cmoncol.items;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.List;

public class ItemPokemonEgg extends Item {

    public ItemPokemonEgg(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack createEgg(Pokemon pokemon) {
        ItemStack stack = new ItemStack(com.ogtenzohd.cmoncol.registration.CmoncolReg.POKEMON_EGG.get());
        
        CompoundTag tag = new CompoundTag();
        com.ogtenzohd.cmoncol.config.CCConfig.BreedingMode mode = com.ogtenzohd.cmoncol.config.CCConfig.INSTANCE.breedingMode.get();
        
        int stepMultiplier = 128;
        int minSteps = 1000;
        
        if (mode == com.ogtenzohd.cmoncol.config.CCConfig.BreedingMode.CLASSIC) {
            stepMultiplier = 256;
            minSteps = 2000;
        } else if (mode == com.ogtenzohd.cmoncol.config.CCConfig.BreedingMode.EASY) {
            stepMultiplier = 64;
            minSteps = 500;
        }

        int steps = Math.max(minSteps, pokemon.getSpecies().getEggCycles() * stepMultiplier);
        
        tag.putInt("EggSteps", steps);
        tag.putString("SpeciesName", pokemon.getSpecies().getName());
        tag.putFloat("LastWalkDist", 0f); 
        
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static void setPokemonData(ItemStack stack, CompoundTag pokemonNBT) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.put("PokemonData", pokemonNBT);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("SpeciesName")) {
            tooltipComponents.add(Component.literal("§7Species: §e" + tag.getString("SpeciesName")));
            
            if (tag.contains("EggSteps")) {
                int steps = tag.getInt("EggSteps");
                tooltipComponents.add(Component.literal("§bSteps Remaining: " + steps));
                
                String message = steps < 500 ? "It's making sounds!" : "It looks mysterious.";
                tooltipComponents.add(Component.literal("§7§o" + message));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            
            float lastDist = tag.contains("LastWalkDist") ? tag.getFloat("LastWalkDist") : 0f;
            float currentDist = player.walkDist;

            if (currentDist > lastDist) {
                if (tag.contains("EggSteps")) {
                    int steps = tag.getInt("EggSteps");
                    steps--;
                    
                    if (steps <= 0) {
                        hatchEgg(stack, player, tag);
                        return;
                    } else {
                        tag.putInt("EggSteps", steps);
                    }
                }
            }
            
            tag.putFloat("LastWalkDist", currentDist);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    private void hatchEgg(ItemStack stack, ServerPlayer player, CompoundTag tag) {
        if (tag.contains("PokemonData")) {
            Pokemon pokemon = new Pokemon();
            pokemon.loadFromNBT(player.level().registryAccess(), tag.getCompound("PokemonData"));
            
            pokemon.setOriginalTrainer(player.getUUID());
            pokemon.heal();
            
            PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
            
            if (party.add(pokemon)) {
                player.sendSystemMessage(Component.literal("§aOh? Your Egg hatched into a " + pokemon.getDisplayName(true).getString() + "!"));
                player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                stack.setCount(0); 
            }
        }
    }
}