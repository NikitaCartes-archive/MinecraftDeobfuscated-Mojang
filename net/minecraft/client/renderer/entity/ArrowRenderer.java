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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;

@Environment(value=EnvType.CLIENT)
public abstract class ArrowRenderer<T extends AbstractArrow>
extends EntityRenderer<T> {
    public ArrowRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    public void render(T abstractArrow, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotation(Mth.lerp(h, ((AbstractArrow)abstractArrow).yRotO, ((AbstractArrow)abstractArrow).yRot) - 90.0f, true));
        poseStack.mulPose(Vector3f.ZP.rotation(Mth.lerp(h, ((AbstractArrow)abstractArrow).xRotO, ((AbstractArrow)abstractArrow).xRot), true));
        boolean i = false;
        float j = 0.0f;
        float k = 0.5f;
        float l = 0.0f;
        float m = 0.15625f;
        float n = 0.0f;
        float o = 0.15625f;
        float p = 0.15625f;
        float q = 0.3125f;
        float r = 0.05625f;
        float s = (float)((AbstractArrow)abstractArrow).shakeTime - h;
        if (s > 0.0f) {
            float t = -Mth.sin(s * 3.0f) * s;
            poseStack.mulPose(Vector3f.ZP.rotation(t, true));
        }
        poseStack.mulPose(Vector3f.XP.rotation(45.0f, true));
        poseStack.scale(0.05625f, 0.05625f, 0.05625f);
        poseStack.translate(-4.0, 0.0, 0.0);
        int u = ((Entity)abstractArrow).getLightColor();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(this.getTextureLocation(abstractArrow)));
        OverlayTexture.setDefault(vertexConsumer);
        Matrix4f matrix4f = poseStack.getPose();
        this.vertex(matrix4f, vertexConsumer, -7, -2, -2, 0.0f, 0.15625f, 1, 0, 0, u);
        this.vertex(matrix4f, vertexConsumer, -7, -2, 2, 0.15625f, 0.15625f, 1, 0, 0, u);
        this.vertex(matrix4f, vertexConsumer, -7, 2, 2, 0.15625f, 0.3125f, 1, 0, 0, u);
        this.vertex(matrix4f, vertexConsumer, -7, 2, -2, 0.0f, 0.3125f, 1, 0, 0, u);
        this.vertex(matrix4f, vertexConsumer, -7, 2, -2, 0.0f, 0.15625f, -1, 0, 0, u);
        this.vertex(matrix4f, vertexConsumer, -7, 2, 2, 0.15625f, 0.15625f, -1, 0, 0, u);
        this.vertex(matrix4f, vertexConsumer, -7, -2, 2, 0.15625f, 0.3125f, -1, 0, 0, u);
        this.vertex(matrix4f, vertexConsumer, -7, -2, -2, 0.0f, 0.3125f, -1, 0, 0, u);
        for (int v = 0; v < 4; ++v) {
            poseStack.mulPose(Vector3f.XP.rotation(90.0f, true));
            this.vertex(matrix4f, vertexConsumer, -8, -2, 0, 0.0f, 0.0f, 0, 1, 0, u);
            this.vertex(matrix4f, vertexConsumer, 8, -2, 0, 0.5f, 0.0f, 0, 1, 0, u);
            this.vertex(matrix4f, vertexConsumer, 8, 2, 0, 0.5f, 0.15625f, 0, 1, 0, u);
            this.vertex(matrix4f, vertexConsumer, -8, 2, 0, 0.0f, 0.15625f, 0, 1, 0, u);
        }
        vertexConsumer.unsetDefaultOverlayCoords();
        poseStack.popPose();
        super.render(abstractArrow, d, e, f, g, h, poseStack, multiBufferSource);
    }

    public void vertex(Matrix4f matrix4f, VertexConsumer vertexConsumer, int i, int j, int k, float f, float g, int l, int m, int n, int o) {
        vertexConsumer.vertex(matrix4f, i, j, k).color(255, 255, 255, 255).uv(f, g).uv2(o).normal(l, n, m).endVertex();
    }
}

