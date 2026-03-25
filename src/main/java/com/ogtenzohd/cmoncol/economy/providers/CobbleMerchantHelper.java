package com.ogtenzohd.cmoncol.economy.providers;

import fr.harmex.cobbledollars.common.world.entity.CobbleMerchant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class CobbleMerchantHelper {

    public static Entity spawnInvisibleMerchant(ServerLevel level, BlockPos spawnPos) {
        ResourceLocation entityId = ResourceLocation.fromNamespaceAndPath("cobbledollars", "cobble_merchant");
        EntityType<?> merchantType = BuiltInRegistries.ENTITY_TYPE.get(entityId);

        Entity entity = merchantType.create(level);

        if (entity instanceof CobbleMerchant merchant) {
            merchant.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

            merchant.setInvisible(true);
            merchant.setInvulnerable(true);
            merchant.setNoGravity(true);
            merchant.setNoAi(true);
            merchant.setSilent(true);
            merchant.setCustomNameVisible(false);

            merchant.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, -1, 0, false, false));

            level.addFreshEntity(merchant);

            return entity;
        }
        return null;
    }

    public static void forwardInteraction(net.minecraft.world.entity.player.Player player, BlockPos buildingPos, ServerLevel level) {
        net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(buildingPos).inflate(5);

        java.util.List<Entity> merchants = level.getEntities(null, searchBox).stream()
                .filter(e -> e instanceof fr.harmex.cobbledollars.common.world.entity.CobbleMerchant)
                .toList();

        Entity merchant;

        if (!merchants.isEmpty()) {
            merchant = merchants.getFirst();
        } else {
            merchant = spawnInvisibleMerchant(level, buildingPos);
        }

        if (merchant != null) {
            merchant.interact(player, net.minecraft.world.InteractionHand.MAIN_HAND);
        } else {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cThe Pokemart is currently closed! (Storefront entity failed to spawn)"));
        }
    }
}