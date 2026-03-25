package com.ogtenzohd.cmoncol.entity;

import com.ogtenzohd.cmoncol.blocks.custom.gym.GymBlock;
import com.ogtenzohd.cmoncol.client.ClientHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

public class GhostReceptionistEntity extends PathfinderMob {

    public GhostReceptionistEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
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
        if (this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            BlockPos.betweenClosedStream(
                    this.blockPosition().offset(-10, -5, -10),
                    this.blockPosition().offset(10, 5, 10))
                .filter(pos -> this.level().getBlockState(pos).getBlock() instanceof GymBlock)
                .findFirst()
                    .ifPresent(gymPos -> {
                        if (FMLEnvironment.dist.isClient()) {
                            ClientHelper.openGymScreen(gymPos);
                        }
                    });

            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }
}