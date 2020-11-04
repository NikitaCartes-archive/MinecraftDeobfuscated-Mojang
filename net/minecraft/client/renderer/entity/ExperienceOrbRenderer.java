/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;

@Environment(value=EnvType.CLIENT)
public class ExperienceOrbRenderer
extends EntityRenderer<ExperienceOrb> {
    private static final ResourceLocation EXPERIENCE_ORB_LOCATION = new ResourceLocation("textures/entity/experience_orb.png");
    private static final RenderType RENDER_TYPE = RenderType.itemEntityTranslucentCull(EXPERIENCE_ORB_LOCATION);

    public ExperienceOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.15f;
        this.shadowStrength = 0.75f;
    }

    @Override
    protected int getBlockLightLevel(ExperienceOrb experienceOrb, BlockPos blockPos) {
        return Mth.clamp(super.getBlockLightLevel(experienceOrb, blockPos) + 7, 0, 15);
    }

    @Override
    public void render(ExperienceOrb experienceOrb, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        int j = experienceOrb.getIcon();
        float h = (float)(j % 4 * 16 + 0) / 64.0f;
        float k = (float)(j % 4 * 16 + 16) / 64.0f;
        float l = (float)(j / 4 * 16 + 0) / 64.0f;
        float m = (float)(j / 4 * 16 + 16) / 64.0f;
        float n = 1.0f;
        float o = 0.5f;
        float p = 0.25f;
        float q = 255.0f;
        float r = ((float)experienceOrb.tickCount + g) / 2.0f;
        int s = (int)((Mth.sin(r + 0.0f) + 1.0f) * 0.5f * 255.0f);
        int t = 255;
        int u = (int)((Mth.sin(r + 4.1887903f) + 1.0f) * 0.1f * 255.0f);
        poseStack.translate(0.0, 0.1f, 0.0);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
        float v = 0.3f;
        poseStack.scale(0.3f, 0.3f, 0.3f);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        ExperienceOrbRenderer.vertex(vertexConsumer, matrix4f, matrix3f, -0.5f, -0.25f, s, 255, u, h, m, i);
        ExperienceOrbRenderer.vertex(vertexConsumer, matrix4f, matrix3f, 0.5f, -0.25f, s, 255, u, k, m, i);
        ExperienceOrbRenderer.vertex(vertexConsumer, matrix4f, matrix3f, 0.5f, 0.75f, s, 255, u, k, l, i);
        ExperienceOrbRenderer.vertex(vertexConsumer, matrix4f, matrix3f, -0.5f, 0.75f, s, 255, u, h, l, i);
        poseStack.popPose();
        super.render(experienceOrb, f, g, poseStack, multiBufferSource, i);
    }

    private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, float f, float g, int i, int j, int k, float h, float l, int m) {
        vertexConsumer.vertex(matrix4f, f, g, 0.0f).color(i, j, k, 128).uv(h, l).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(m).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(ExperienceOrb experienceOrb) {
        return EXPERIENCE_ORB_LOCATION;
    }
}

