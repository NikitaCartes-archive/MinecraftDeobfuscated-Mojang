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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;

@Environment(value=EnvType.CLIENT)
public class ExperienceOrbRenderer
extends EntityRenderer<ExperienceOrb> {
    private static final ResourceLocation EXPERIENCE_ORB_LOCATION = new ResourceLocation("textures/entity/experience_orb.png");

    public ExperienceOrbRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
        this.shadowRadius = 0.15f;
        this.shadowStrength = 0.75f;
    }

    @Override
    public void render(ExperienceOrb experienceOrb, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        poseStack.pushPose();
        int i = experienceOrb.getIcon();
        float j = (float)(i % 4 * 16 + 0) / 64.0f;
        float k = (float)(i % 4 * 16 + 16) / 64.0f;
        float l = (float)(i / 4 * 16 + 0) / 64.0f;
        float m = (float)(i / 4 * 16 + 16) / 64.0f;
        float n = 1.0f;
        float o = 0.5f;
        float p = 0.25f;
        float q = 255.0f;
        float r = ((float)experienceOrb.tickCount + h) / 2.0f;
        int s = (int)((Mth.sin(r + 0.0f) + 1.0f) * 0.5f * 255.0f);
        int t = 255;
        int u = (int)((Mth.sin(r + 4.1887903f) + 1.0f) * 0.1f * 255.0f);
        poseStack.translate(0.0, 0.1f, 0.0);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f - this.entityRenderDispatcher.playerRotY));
        float v = (float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX;
        poseStack.mulPose(Vector3f.XP.rotationDegrees(v));
        float w = 0.3f;
        poseStack.scale(0.3f, 0.3f, 0.3f);
        int x = experienceOrb.getLightColor();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(EXPERIENCE_ORB_LOCATION));
        Matrix4f matrix4f = poseStack.getPose();
        ExperienceOrbRenderer.vertex(vertexConsumer, matrix4f, -0.5f, -0.25f, s, 255, u, j, m, x);
        ExperienceOrbRenderer.vertex(vertexConsumer, matrix4f, 0.5f, -0.25f, s, 255, u, k, m, x);
        ExperienceOrbRenderer.vertex(vertexConsumer, matrix4f, 0.5f, 0.75f, s, 255, u, k, l, x);
        ExperienceOrbRenderer.vertex(vertexConsumer, matrix4f, -0.5f, 0.75f, s, 255, u, j, l, x);
        poseStack.popPose();
        super.render(experienceOrb, d, e, f, g, h, poseStack, multiBufferSource);
    }

    private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g, int i, int j, int k, float h, float l, int m) {
        vertexConsumer.vertex(matrix4f, f, g, 0.0f).color(i, j, k, 128).uv(h, l).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(m).normal(0.0f, 1.0f, 0.0f).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(ExperienceOrb experienceOrb) {
        return EXPERIENCE_ORB_LOCATION;
    }
}

