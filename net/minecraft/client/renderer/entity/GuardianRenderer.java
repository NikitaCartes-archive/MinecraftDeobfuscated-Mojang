/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class GuardianRenderer
extends MobRenderer<Guardian, GuardianModel> {
    private static final ResourceLocation GUARDIAN_LOCATION = new ResourceLocation("textures/entity/guardian.png");
    private static final ResourceLocation GUARDIAN_BEAM_LOCATION = new ResourceLocation("textures/entity/guardian_beam.png");

    public GuardianRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        this(entityRenderDispatcher, 0.5f);
    }

    protected GuardianRenderer(EntityRenderDispatcher entityRenderDispatcher, float f) {
        super(entityRenderDispatcher, new GuardianModel(), f);
    }

    @Override
    public boolean shouldRender(Guardian guardian, Culler culler, double d, double e, double f) {
        LivingEntity livingEntity;
        if (super.shouldRender(guardian, culler, d, e, f)) {
            return true;
        }
        if (guardian.hasActiveAttackTarget() && (livingEntity = guardian.getActiveAttackTarget()) != null) {
            Vec3 vec3 = this.getPosition(livingEntity, (double)livingEntity.getBbHeight() * 0.5, 1.0f);
            Vec3 vec32 = this.getPosition(guardian, guardian.getEyeHeight(), 1.0f);
            if (culler.isVisible(new AABB(vec32.x, vec32.y, vec32.z, vec3.x, vec3.y, vec3.z))) {
                return true;
            }
        }
        return false;
    }

    private Vec3 getPosition(LivingEntity livingEntity, double d, float f) {
        double e = Mth.lerp((double)f, livingEntity.xOld, livingEntity.x);
        double g = Mth.lerp((double)f, livingEntity.yOld, livingEntity.y) + d;
        double h = Mth.lerp((double)f, livingEntity.zOld, livingEntity.z);
        return new Vec3(e, g, h);
    }

    @Override
    public void render(Guardian guardian, double d, double e, double f, float g, float h) {
        super.render(guardian, d, e, f, g, h);
        LivingEntity livingEntity = guardian.getActiveAttackTarget();
        if (livingEntity != null) {
            float i = guardian.getAttackAnimationScale(h);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tesselator.getBuilder();
            this.bindTexture(GUARDIAN_BEAM_LOCATION);
            GlStateManager.texParameter(3553, 10242, 10497);
            GlStateManager.texParameter(3553, 10243, 10497);
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            float j = 240.0f;
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 240.0f, 240.0f);
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            float k = (float)guardian.level.getGameTime() + h;
            float l = k * 0.5f % 1.0f;
            float m = guardian.getEyeHeight();
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float)d, (float)e + m, (float)f);
            Vec3 vec3 = this.getPosition(livingEntity, (double)livingEntity.getBbHeight() * 0.5, h);
            Vec3 vec32 = this.getPosition(guardian, m, h);
            Vec3 vec33 = vec3.subtract(vec32);
            double n = vec33.length() + 1.0;
            vec33 = vec33.normalize();
            float o = (float)Math.acos(vec33.y);
            float p = (float)Math.atan2(vec33.z, vec33.x);
            GlStateManager.rotatef((1.5707964f - p) * 57.295776f, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotatef(o * 57.295776f, 1.0f, 0.0f, 0.0f);
            boolean q = true;
            double r = (double)k * 0.05 * -1.5;
            bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            float s = i * i;
            int t = 64 + (int)(s * 191.0f);
            int u = 32 + (int)(s * 191.0f);
            int v = 128 - (int)(s * 64.0f);
            double w = 0.2;
            double x = 0.282;
            double y = 0.0 + Math.cos(r + 2.356194490192345) * 0.282;
            double z = 0.0 + Math.sin(r + 2.356194490192345) * 0.282;
            double aa = 0.0 + Math.cos(r + 0.7853981633974483) * 0.282;
            double ab = 0.0 + Math.sin(r + 0.7853981633974483) * 0.282;
            double ac = 0.0 + Math.cos(r + 3.9269908169872414) * 0.282;
            double ad = 0.0 + Math.sin(r + 3.9269908169872414) * 0.282;
            double ae = 0.0 + Math.cos(r + 5.497787143782138) * 0.282;
            double af = 0.0 + Math.sin(r + 5.497787143782138) * 0.282;
            double ag = 0.0 + Math.cos(r + Math.PI) * 0.2;
            double ah = 0.0 + Math.sin(r + Math.PI) * 0.2;
            double ai = 0.0 + Math.cos(r + 0.0) * 0.2;
            double aj = 0.0 + Math.sin(r + 0.0) * 0.2;
            double ak = 0.0 + Math.cos(r + 1.5707963267948966) * 0.2;
            double al = 0.0 + Math.sin(r + 1.5707963267948966) * 0.2;
            double am = 0.0 + Math.cos(r + 4.71238898038469) * 0.2;
            double an = 0.0 + Math.sin(r + 4.71238898038469) * 0.2;
            double ao = n;
            double ap = 0.0;
            double aq = 0.4999;
            double ar = -1.0f + l;
            double as = n * 2.5 + ar;
            bufferBuilder.vertex(ag, ao, ah).uv(0.4999, as).color(t, u, v, 255).endVertex();
            bufferBuilder.vertex(ag, 0.0, ah).uv(0.4999, ar).color(t, u, v, 255).endVertex();
            bufferBuilder.vertex(ai, 0.0, aj).uv(0.0, ar).color(t, u, v, 255).endVertex();
            bufferBuilder.vertex(ai, ao, aj).uv(0.0, as).color(t, u, v, 255).endVertex();
            bufferBuilder.vertex(ak, ao, al).uv(0.4999, as).color(t, u, v, 255).endVertex();
            bufferBuilder.vertex(ak, 0.0, al).uv(0.4999, ar).color(t, u, v, 255).endVertex();
            bufferBuilder.vertex(am, 0.0, an).uv(0.0, ar).color(t, u, v, 255).endVertex();
            bufferBuilder.vertex(am, ao, an).uv(0.0, as).color(t, u, v, 255).endVertex();
            double at = 0.0;
            if (guardian.tickCount % 2 == 0) {
                at = 0.5;
            }
            bufferBuilder.vertex(y, ao, z).uv(0.5, at + 0.5).color(t, u, v, 255).endVertex();
            bufferBuilder.vertex(aa, ao, ab).uv(1.0, at + 0.5).color(t, u, v, 255).endVertex();
            bufferBuilder.vertex(ae, ao, af).uv(1.0, at).color(t, u, v, 255).endVertex();
            bufferBuilder.vertex(ac, ao, ad).uv(0.5, at).color(t, u, v, 255).endVertex();
            tesselator.end();
            GlStateManager.popMatrix();
        }
    }

    @Override
    protected ResourceLocation getTextureLocation(Guardian guardian) {
        return GUARDIAN_LOCATION;
    }
}

