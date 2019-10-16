/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.HangingEntity;

@Environment(value=EnvType.CLIENT)
public abstract class MobRenderer<T extends Mob, M extends EntityModel<T>>
extends LivingEntityRenderer<T, M> {
    public MobRenderer(EntityRenderDispatcher entityRenderDispatcher, M entityModel, float f) {
        super(entityRenderDispatcher, entityModel, f);
    }

    @Override
    protected boolean shouldShowName(T mob) {
        return super.shouldShowName(mob) && (((LivingEntity)mob).shouldShowName() || ((Entity)mob).hasCustomName() && mob == this.entityRenderDispatcher.crosshairPickEntity);
    }

    @Override
    public boolean shouldRender(T mob, Frustum frustum, double d, double e, double f) {
        if (super.shouldRender(mob, frustum, d, e, f)) {
            return true;
        }
        Entity entity = ((Mob)mob).getLeashHolder();
        if (entity != null) {
            return frustum.isVisible(entity.getBoundingBoxForCulling());
        }
        return false;
    }

    @Override
    public void render(T mob, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        super.render(mob, d, e, f, g, h, poseStack, multiBufferSource);
        Entity entity = ((Mob)mob).getLeashHolder();
        if (entity == null) {
            return;
        }
        MobRenderer.renderLeash(mob, h, poseStack, multiBufferSource, entity);
    }

    public static void renderLeash(Mob mob, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, Entity entity) {
        poseStack.pushPose();
        double d = Mth.lerp(f * 0.5f, entity.yRot, entity.yRotO) * ((float)Math.PI / 180);
        double e = Mth.lerp(f * 0.5f, entity.xRot, entity.xRotO) * ((float)Math.PI / 180);
        double g = Math.cos(d);
        double h = Math.sin(d);
        double i = Math.sin(e);
        if (entity instanceof HangingEntity) {
            g = 0.0;
            h = 0.0;
            i = -1.0;
        }
        double j = Math.cos(e);
        double k = Mth.lerp((double)f, entity.xo, entity.getX()) - g * 0.7 - h * 0.5 * j;
        double l = Mth.lerp((double)f, entity.yo + (double)entity.getEyeHeight() * 0.7, entity.getY() + (double)entity.getEyeHeight() * 0.7) - i * 0.5 - 0.25;
        double m = Mth.lerp((double)f, entity.zo, entity.getZ()) - h * 0.7 + g * 0.5 * j;
        double n = (double)(Mth.lerp(f, mob.yBodyRot, mob.yBodyRotO) * ((float)Math.PI / 180)) + 1.5707963267948966;
        g = Math.cos(n) * (double)mob.getBbWidth() * 0.4;
        h = Math.sin(n) * (double)mob.getBbWidth() * 0.4;
        double o = Mth.lerp((double)f, mob.xo, mob.getX()) + g;
        double p = Mth.lerp((double)f, mob.yo, mob.getY());
        double q = Mth.lerp((double)f, mob.zo, mob.getZ()) + h;
        poseStack.translate(g, -(1.6 - (double)mob.getBbHeight()) * 0.5, h);
        float r = (float)(k - o);
        float s = (float)(l - p);
        float t = (float)(m - q);
        float u = 0.025f;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.leash());
        Matrix4f matrix4f = poseStack.getPose();
        float v = Mth.fastInvSqrt(r * r + t * t) * 0.025f / 2.0f;
        float w = t * v;
        float x = r * v;
        int y = mob.getLightColor();
        int z = entity.getLightColor();
        MobRenderer.renderSide(vertexConsumer, matrix4f, y, z, r, s, t, 0.025f, 0.025f, w, x);
        MobRenderer.renderSide(vertexConsumer, matrix4f, y, z, r, s, t, 0.025f, 0.0f, w, x);
        poseStack.popPose();
    }

    public static void renderSide(VertexConsumer vertexConsumer, Matrix4f matrix4f, int i, int j, float f, float g, float h, float k, float l, float m, float n) {
        int o = 24;
        int p = LightTexture.block(i);
        int q = LightTexture.block(j);
        int r = LightTexture.sky(i);
        int s = LightTexture.sky(j);
        for (int t = 0; t < 24; ++t) {
            float u = (float)t / 23.0f;
            int v = (int)Mth.lerp(u, p, q);
            int w = (int)Mth.lerp(u, r, s);
            int x = LightTexture.pack(v, w);
            MobRenderer.addVertexPair(vertexConsumer, matrix4f, x, f, g, h, k, l, 24, t, false, m, n);
            MobRenderer.addVertexPair(vertexConsumer, matrix4f, x, f, g, h, k, l, 24, t + 1, true, m, n);
        }
    }

    public static void addVertexPair(VertexConsumer vertexConsumer, Matrix4f matrix4f, int i, float f, float g, float h, float j, float k, int l, int m, boolean bl, float n, float o) {
        float p = 0.5f;
        float q = 0.4f;
        float r = 0.3f;
        if (m % 2 == 0) {
            p *= 0.7f;
            q *= 0.7f;
            r *= 0.7f;
        }
        float s = (float)m / (float)l;
        float t = f * s;
        float u = g * (s * s + s) * 0.5f + ((float)l - (float)m) / ((float)l * 0.75f) + 0.125f;
        float v = h * s;
        if (!bl) {
            vertexConsumer.vertex(matrix4f, t + n, u + j - k, v - o).color(p, q, r, 1.0f).uv2(i).endVertex();
        }
        vertexConsumer.vertex(matrix4f, t - n, u + k, v + o).color(p, q, r, 1.0f).uv2(i).endVertex();
        if (bl) {
            vertexConsumer.vertex(matrix4f, t + n, u + j - k, v - o).color(p, q, r, 1.0f).uv2(i).endVertex();
        }
    }

    @Override
    protected /* synthetic */ boolean shouldShowName(LivingEntity livingEntity) {
        return this.shouldShowName((T)((Mob)livingEntity));
    }
}

