package com.ogtenzohd.cmoncol.entity;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.ogtenzohd.cmoncol.util.RangerRewardGenerator;
import net.minecraft.core.registries.BuiltInRegistries;
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

public class RangerEntity extends PathfinderMob {

    private int buildingLevel = 1;

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

            if (player instanceof ServerPlayer serverPlayer) {
                PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(serverPlayer);
                Pokemon leadMon = party.get(0);

                if (leadMon != null) {
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
}