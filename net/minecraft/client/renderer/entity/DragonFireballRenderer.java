/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.DragonFireball;

@Environment(value=EnvType.CLIENT)
public class DragonFireballRenderer
extends EntityRenderer<DragonFireball> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_fireball.png");

    public DragonFireballRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    public void render(DragonFireball dragonFireball, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        poseStack.pushPose();
        poseStack.scale(2.0f, 2.0f, 2.0f);
        float i = 1.0f;
        float j = 0.5f;
        float k = 0.25f;
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f - this.entityRenderDispatcher.playerRotY));
        float l = (float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX;
        poseStack.mulPose(Vector3f.XP.rotationDegrees(l));
        Matrix4f matrix4f = poseStack.getPose();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE_LOCATION));
        int m = dragonFireball.getLightColor();
        vertexConsumer.vertex(matrix4f, -0.5f, -0.25f, 0.0f).color(255, 255, 255, 255).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(m).normal(0.0f, 1.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, 0.5f, -0.25f, 0.0f).color(255, 255, 255, 255).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(m).normal(0.0f, 1.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, 0.5f, 0.75f, 0.0f).color(255, 255, 255, 255).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(m).normal(0.0f, 1.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, -0.5f, 0.75f, 0.0f).color(255, 255, 255, 255).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(m).normal(0.0f, 1.0f, 0.0f).endVertex();
        poseStack.popPose();
        super.render(dragonFireball, d, e, f, g, h, poseStack, multiBufferSource);
    }

    @Override
    public ResourceLocation getTextureLocation(DragonFireball dragonFireball) {
        return TEXTURE_LOCATION;
    }
}

