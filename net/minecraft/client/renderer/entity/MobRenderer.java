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
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public abstract class MobRenderer<T extends Mob, M extends EntityModel<T>>
extends LivingEntityRenderer<T, M> {
    public MobRenderer(EntityRendererProvider.Context context, M entityModel, float f) {
        super(context, entityModel, f);
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
        int v;
        poseStack.pushPose();
        Vec3 vec3 = entity.getRopeHoldPosition(f);
        double d = (double)(Mth.lerp(f, ((Mob)mob).yBodyRot, ((Mob)mob).yBodyRotO) * ((float)Math.PI / 180)) + 1.5707963267948966;
        Vec3 vec32 = ((Entity)mob).getLeashOffset();
        double e = Math.cos(d) * vec32.z + Math.sin(d) * vec32.x;
        double g = Math.sin(d) * vec32.z - Math.cos(d) * vec32.x;
        double h = Mth.lerp((double)f, ((Mob)mob).xo, ((Entity)mob).getX()) + e;
        double i = Mth.lerp((double)f, ((Mob)mob).yo, ((Entity)mob).getY()) + vec32.y;
        double j = Mth.lerp((double)f, ((Mob)mob).zo, ((Entity)mob).getZ()) + g;
        poseStack.translate(e, vec32.y, g);
        float k = (float)(vec3.x - h);
        float l = (float)(vec3.y - i);
        float m = (float)(vec3.z - j);
        float n = 0.025f;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.leash());
        Matrix4f matrix4f = poseStack.last().pose();
        float o = Mth.fastInvSqrt(k * k + m * m) * 0.025f / 2.0f;
        float p = m * o;
        float q = k * o;
        BlockPos blockPos = new BlockPos(((Entity)mob).getEyePosition(f));
        BlockPos blockPos2 = new BlockPos(entity.getEyePosition(f));
        int r = this.getBlockLightLevel(mob, blockPos);
        int s = this.entityRenderDispatcher.getRenderer(entity).getBlockLightLevel(entity, blockPos2);
        int t = ((Mob)mob).level.getBrightness(LightLayer.SKY, blockPos);
        int u = ((Mob)mob).level.getBrightness(LightLayer.SKY, blockPos2);
        for (v = 0; v <= 24; ++v) {
            MobRenderer.addVertexPair(vertexConsumer, matrix4f, k, l, m, r, s, t, u, 0.025f, 0.025f, p, q, v, false);
        }
        for (v = 24; v >= 0; --v) {
            MobRenderer.addVertexPair(vertexConsumer, matrix4f, k, l, m, r, s, t, u, 0.025f, 0.0f, p, q, v, true);
        }
        poseStack.popPose();
    }

    private static void addVertexPair(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g, float h, int i, int j, int k, int l, float m, float n, float o, float p, int q, boolean bl) {
        float r = (float)q / 24.0f;
        int s = (int)Mth.lerp(r, i, j);
        int t = (int)Mth.lerp(r, k, l);
        int u = LightTexture.pack(s, t);
        float v = q % 2 == (bl ? 1 : 0) ? 0.7f : 1.0f;
        float w = 0.5f * v;
        float x = 0.4f * v;
        float y = 0.3f * v;
        float z = f * r;
        float aa = g > 0.0f ? g * r * r : g - g * (1.0f - r) * (1.0f - r);
        float ab = h * r;
        vertexConsumer.vertex(matrix4f, z - o, aa + n, ab + p).color(w, x, y, 1.0f).uv2(u).endVertex();
        vertexConsumer.vertex(matrix4f, z + o, aa + m - n, ab - p).color(w, x, y, 1.0f).uv2(u).endVertex();
    }
}

