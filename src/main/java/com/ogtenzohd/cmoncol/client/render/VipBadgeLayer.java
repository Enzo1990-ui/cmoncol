package com.ogtenzohd.cmoncol.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class VipBadgeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation VIP_TEXTURE = ResourceLocation.fromNamespaceAndPath("cmoncol", "textures/misc/vip_badge.png");

    public VipBadgeLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        if (!com.ogtenzohd.cmoncol.util.CmoncolPerks.hasVIPPerks(player.getUUID())) {
            return;
        }

        if (player.isInvisible()) {
            return;
        }

        poseStack.pushPose();
        this.getParentModel().body.translateAndRotate(poseStack);
        poseStack.translate(0.14f, 0.25f, -0.15f);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(VIP_TEXTURE));
        Matrix4f matrix4f = poseStack.last().pose();
        float size = 0.05f;

        vertexConsumer.addVertex(matrix4f, -size, -size, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, 1.0F);
        vertexConsumer.addVertex(matrix4f, -size, size, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, 1.0F);
        vertexConsumer.addVertex(matrix4f, size, size, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, 1.0F);
        vertexConsumer.addVertex(matrix4f, size, -size, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, 1.0F);

        vertexConsumer.addVertex(matrix4f, size, -size, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, -1.0F);
        vertexConsumer.addVertex(matrix4f, size, size, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, -1.0F);
        vertexConsumer.addVertex(matrix4f, -size, size, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, -1.0F);
        vertexConsumer.addVertex(matrix4f, -size, -size, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, -1.0F);

        poseStack.popPose();
    }
}