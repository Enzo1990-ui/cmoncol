package com.ogtenzohd.cmoncol.entity;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.ogtenzohd.cmoncol.util.RangerRewardGenerator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RangerEntity extends PathfinderMob {

    private int buildingLevel = 1;
    // Map to keep track of when players last interacted (Cooldown system)
    private final Map<UUID, Long> playerCooldowns = new HashMap<>();

    public RangerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public void setBuildingLevel(int level) {
        this.buildingLevel = level;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayerSqr) {
        return true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(@NotNull Entity entityIn) {
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {

            long currentTime = this.level().getGameTime();
            if (playerCooldowns.containsKey(player.getUUID())) {
                long lastInteract = playerCooldowns.get(player.getUUID());
                if (currentTime - lastInteract < 24000) {
                    player.sendSystemMessage(Component.literal("§cThe Ranger is busy observing Pokemon right now. Come back tomorrow!"));
                    return InteractionResult.SUCCESS;
                }
            }

            if (player instanceof ServerPlayer serverPlayer) {
                PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(serverPlayer);
                Pokemon leadMon = party.get(0);

                if (leadMon != null) {
                    playerCooldowns.put(player.getUUID(), currentTime);

                    int friendship = leadMon.getFriendship();

                    if (friendship >= 255) {
                        if (player.level().random.nextInt(100) < 25) {
                            ItemStack superEgg = RangerRewardGenerator.generateRangerEgg((ServerLevel) player.level(), this.buildingLevel);

                            player.getInventory().placeItemBackInInventory(superEgg);
                            player.sendSystemMessage(Component.literal("§aThe Ranger was amazed by your bond and gave you a mysterious Egg!"));
                        } else {
                            Item rareCandy = BuiltInRegistries.ITEM.get(ResourceLocation.parse("cobblemon:rare_candy"));
                            player.getInventory().placeItemBackInInventory(new ItemStack(rareCandy, 1));
                            player.sendSystemMessage(Component.literal("§bThe Ranger gave you a Rare Candy for taking such great care of your partner!"));
                        }
                    } else if (friendship > 150) {
                        Item sitrus = BuiltInRegistries.ITEM.get(ResourceLocation.parse("cobblemon:sitrus_berry"));
                        player.getInventory().placeItemBackInInventory(new ItemStack(sitrus, 3));
                        player.sendSystemMessage(Component.literal("§eYour Pokemon seems happy! The Ranger gave you some berries."));
                    } else {
                        Item potion = BuiltInRegistries.ITEM.get(ResourceLocation.parse("cobblemon:potion"));
                        player.getInventory().placeItemBackInInventory(new ItemStack(potion, 1));
                        player.sendSystemMessage(Component.literal("§7The Ranger gave you a Potion. Keep spending time with your Pokemon!"));
                    }
                } else {
                    player.sendSystemMessage(Component.literal("§cYou don't have a lead Pokemon in your party!"));
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("BuildingLevel", this.buildingLevel);

        CompoundTag cooldownsTag = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : playerCooldowns.entrySet()) {
            cooldownsTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        compound.put("PlayerCooldowns", cooldownsTag);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("BuildingLevel")) {
            this.buildingLevel = compound.getInt("BuildingLevel");
        }

        if (compound.contains("PlayerCooldowns")) {
            CompoundTag cooldownsTag = compound.getCompound("PlayerCooldowns");
            for (String key : cooldownsTag.getAllKeys()) {
                playerCooldowns.put(UUID.fromString(key), cooldownsTag.getLong(key));
            }
        }
    }
}