package com.ogtenzohd.cmoncol.client.render;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class InvisibleEntityRenderer<T extends Entity> extends EntityRenderer<T> {

    public InvisibleEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
    }

    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTicks, com.mojang.blaze3d.vertex.@NotNull PoseStack poseStack, net.minecraft.client.renderer.@NotNull MultiBufferSource buffer, int packedLight) {
    }
}