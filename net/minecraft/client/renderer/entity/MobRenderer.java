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
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.LightLayer;

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
    public void render(T mob, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        super.render(mob, f, g, poseStack, multiBufferSource, i);
        Entity entity = ((Mob)mob).getLeashHolder();
        if (entity == null) {
            return;
        }
        this.renderLeash(mob, g, poseStack, multiBufferSource, entity);
    }

    private <E extends Entity> void renderLeash(T mob, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, E entity) {
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
        double n = (double)(Mth.lerp(f, ((Mob)mob).yBodyRot, ((Mob)mob).yBodyRotO) * ((float)Math.PI / 180)) + 1.5707963267948966;
        g = Math.cos(n) * (double)((Entity)mob).getBbWidth() * 0.4;
        h = Math.sin(n) * (double)((Entity)mob).getBbWidth() * 0.4;
        double o = Mth.lerp((double)f, ((Mob)mob).xo, ((Entity)mob).getX()) + g;
        double p = Mth.lerp((double)f, ((Mob)mob).yo, ((Entity)mob).getY());
        double q = Mth.lerp((double)f, ((Mob)mob).zo, ((Entity)mob).getZ()) + h;
        poseStack.translate(g, -(1.6 - (double)((Entity)mob).getBbHeight()) * 0.5, h);
        float r = (float)(k - o);
        float s = (float)(l - p);
        float t = (float)(m - q);
        float u = 0.025f;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.leash());
        Matrix4f matrix4f = poseStack.last().pose();
        float v = Mth.fastInvSqrt(r * r + t * t) * 0.025f / 2.0f;
        float w = t * v;
        float x = r * v;
        BlockPos blockPos = new BlockPos(((Entity)mob).getEyePosition(f));
        BlockPos blockPos2 = new BlockPos(entity.getEyePosition(f));
        int y = this.getBlockLightLevel(mob, blockPos);
        int z = this.entityRenderDispatcher.getRenderer(entity).getBlockLightLevel(entity, blockPos2);
        int aa = ((Mob)mob).level.getBrightness(LightLayer.SKY, blockPos);
        int ab = ((Mob)mob).level.getBrightness(LightLayer.SKY, blockPos2);
        MobRenderer.renderSide(vertexConsumer, matrix4f, r, s, t, y, z, aa, ab, 0.025f, 0.025f, w, x);
        MobRenderer.renderSide(vertexConsumer, matrix4f, r, s, t, y, z, aa, ab, 0.025f, 0.0f, w, x);
        poseStack.popPose();
    }

    public static void renderSide(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g, float h, int i, int j, int k, int l, float m, float n, float o, float p) {
        int q = 24;
        for (int r = 0; r < 24; ++r) {
            float s = (float)r / 23.0f;
            int t = (int)Mth.lerp(s, i, j);
            int u = (int)Mth.lerp(s, k, l);
            int v = LightTexture.pack(t, u);
            MobRenderer.addVertexPair(vertexConsumer, matrix4f, v, f, g, h, m, n, 24, r, false, o, p);
            MobRenderer.addVertexPair(vertexConsumer, matrix4f, v, f, g, h, m, n, 24, r + 1, true, o, p);
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
}

