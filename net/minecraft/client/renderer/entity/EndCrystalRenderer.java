/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

@Environment(value=EnvType.CLIENT)
public class EndCrystalRenderer
extends EntityRenderer<EndCrystal> {
    private static final ResourceLocation END_CRYSTAL_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal.png");
    public static final float SIN_45 = (float)Math.sin(0.7853981633974483);
    private final ModelPart cube;
    private final ModelPart glass;
    private final ModelPart base;

    public EndCrystalRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
        this.shadowRadius = 0.5f;
        this.glass = new ModelPart(64, 32, 0, 0);
        this.glass.addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f);
        this.cube = new ModelPart(64, 32, 32, 0);
        this.cube.addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f);
        this.base = new ModelPart(64, 32, 0, 16);
        this.base.addBox(-6.0f, 0.0f, -6.0f, 12.0f, 4.0f, 12.0f);
    }

    @Override
    public void render(EndCrystal endCrystal, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        poseStack.pushPose();
        float i = EndCrystalRenderer.getY(endCrystal, h);
        float j = 0.0625f;
        float k = ((float)endCrystal.time + h) * 3.0f;
        int l = endCrystal.getLightColor();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(endCrystal)));
        poseStack.pushPose();
        poseStack.scale(2.0f, 2.0f, 2.0f);
        poseStack.translate(0.0, -0.5, 0.0);
        int m = OverlayTexture.NO_OVERLAY;
        if (endCrystal.showsBottom()) {
            this.base.render(poseStack, vertexConsumer, 0.0625f, l, m, null);
        }
        poseStack.mulPose(Vector3f.YP.rotationDegrees(k));
        poseStack.translate(0.0, 1.5f + i / 2.0f, 0.0);
        poseStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0f, SIN_45), 60.0f, true));
        this.glass.render(poseStack, vertexConsumer, 0.0625f, l, m, null);
        float n = 0.875f;
        poseStack.scale(0.875f, 0.875f, 0.875f);
        poseStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0f, SIN_45), 60.0f, true));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(k));
        this.glass.render(poseStack, vertexConsumer, 0.0625f, l, m, null);
        poseStack.scale(0.875f, 0.875f, 0.875f);
        poseStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0f, SIN_45), 60.0f, true));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(k));
        this.cube.render(poseStack, vertexConsumer, 0.0625f, l, m, null);
        poseStack.popPose();
        poseStack.popPose();
        BlockPos blockPos = endCrystal.getBeamTarget();
        if (blockPos != null) {
            float o = (float)blockPos.getX() + 0.5f;
            float p = (float)blockPos.getY() + 0.5f;
            float q = (float)blockPos.getZ() + 0.5f;
            float r = (float)((double)o - endCrystal.getX());
            float s = (float)((double)p - endCrystal.getY());
            float t = (float)((double)q - endCrystal.getZ());
            poseStack.translate(r, s, t);
            EnderDragonRenderer.renderCrystalBeams(-r, -s + i, -t, h, endCrystal.time, poseStack, multiBufferSource, l);
        }
        super.render(endCrystal, d, e, f, g, h, poseStack, multiBufferSource);
    }

    public static float getY(EndCrystal endCrystal, float f) {
        float g = (float)endCrystal.time + f;
        float h = Mth.sin(g * 0.2f) / 2.0f + 0.5f;
        h = (h * h + h) * 0.4f;
        return h - 1.4f;
    }

    @Override
    public ResourceLocation getTextureLocation(EndCrystal endCrystal) {
        return END_CRYSTAL_LOCATION;
    }

    @Override
    public boolean shouldRender(EndCrystal endCrystal, Frustum frustum, double d, double e, double f) {
        return super.shouldRender(endCrystal, frustum, d, e, f) || endCrystal.getBeamTarget() != null;
    }
}

