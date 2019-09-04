/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.culling.Culler;
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
    public boolean shouldRender(T mob, Culler culler, double d, double e, double f) {
        if (super.shouldRender(mob, culler, d, e, f)) {
            return true;
        }
        Entity entity = ((Mob)mob).getLeashHolder();
        if (entity != null) {
            return culler.isVisible(entity.getBoundingBoxForCulling());
        }
        return false;
    }

    @Override
    public void render(T mob, double d, double e, double f, float g, float h) {
        super.render(mob, d, e, f, g, h);
        if (!this.solidRender) {
            this.renderLeash(mob, d, e, f, g, h);
        }
    }

    protected void renderLeash(T mob, double d, double e, double f, float g, float h) {
        float ae;
        float ad;
        float ac;
        float ab;
        int aa;
        Entity entity = ((Mob)mob).getLeashHolder();
        if (entity == null) {
            return;
        }
        e -= (1.6 - (double)((Entity)mob).getBbHeight()) * 0.5;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        double i = Mth.lerp(h * 0.5f, entity.yRot, entity.yRotO) * ((float)Math.PI / 180);
        double j = Mth.lerp(h * 0.5f, entity.xRot, entity.xRotO) * ((float)Math.PI / 180);
        double k = Math.cos(i);
        double l = Math.sin(i);
        double m = Math.sin(j);
        if (entity instanceof HangingEntity) {
            k = 0.0;
            l = 0.0;
            m = -1.0;
        }
        double n = Math.cos(j);
        double o = Mth.lerp((double)h, entity.xo, entity.x) - k * 0.7 - l * 0.5 * n;
        double p = Mth.lerp((double)h, entity.yo + (double)entity.getEyeHeight() * 0.7, entity.y + (double)entity.getEyeHeight() * 0.7) - m * 0.5 - 0.25;
        double q = Mth.lerp((double)h, entity.zo, entity.z) - l * 0.7 + k * 0.5 * n;
        double r = (double)(Mth.lerp(h, ((Mob)mob).yBodyRot, ((Mob)mob).yBodyRotO) * ((float)Math.PI / 180)) + 1.5707963267948966;
        k = Math.cos(r) * (double)((Entity)mob).getBbWidth() * 0.4;
        l = Math.sin(r) * (double)((Entity)mob).getBbWidth() * 0.4;
        double s = Mth.lerp((double)h, ((Mob)mob).xo, ((Mob)mob).x) + k;
        double t = Mth.lerp((double)h, ((Mob)mob).yo, ((Mob)mob).y);
        double u = Mth.lerp((double)h, ((Mob)mob).zo, ((Mob)mob).z) + l;
        d += k;
        f += l;
        double v = (float)(o - s);
        double w = (float)(p - t);
        double x = (float)(q - u);
        RenderSystem.disableTexture();
        RenderSystem.disableLighting();
        RenderSystem.disableCull();
        int y = 24;
        double z = 0.025;
        bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);
        for (aa = 0; aa <= 24; ++aa) {
            ab = 0.5f;
            ac = 0.4f;
            ad = 0.3f;
            if (aa % 2 == 0) {
                ab *= 0.7f;
                ac *= 0.7f;
                ad *= 0.7f;
            }
            ae = (float)aa / 24.0f;
            bufferBuilder.vertex(d + v * (double)ae + 0.0, e + w * (double)(ae * ae + ae) * 0.5 + (double)((24.0f - (float)aa) / 18.0f + 0.125f), f + x * (double)ae).color(ab, ac, ad, 1.0f).endVertex();
            bufferBuilder.vertex(d + v * (double)ae + 0.025, e + w * (double)(ae * ae + ae) * 0.5 + (double)((24.0f - (float)aa) / 18.0f + 0.125f) + 0.025, f + x * (double)ae).color(ab, ac, ad, 1.0f).endVertex();
        }
        tesselator.end();
        bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);
        for (aa = 0; aa <= 24; ++aa) {
            ab = 0.5f;
            ac = 0.4f;
            ad = 0.3f;
            if (aa % 2 == 0) {
                ab *= 0.7f;
                ac *= 0.7f;
                ad *= 0.7f;
            }
            ae = (float)aa / 24.0f;
            bufferBuilder.vertex(d + v * (double)ae + 0.0, e + w * (double)(ae * ae + ae) * 0.5 + (double)((24.0f - (float)aa) / 18.0f + 0.125f) + 0.025, f + x * (double)ae).color(ab, ac, ad, 1.0f).endVertex();
            bufferBuilder.vertex(d + v * (double)ae + 0.025, e + w * (double)(ae * ae + ae) * 0.5 + (double)((24.0f - (float)aa) / 18.0f + 0.125f), f + x * (double)ae + 0.025).color(ab, ac, ad, 1.0f).endVertex();
        }
        tesselator.end();
        RenderSystem.enableLighting();
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
    }

    @Override
    protected /* synthetic */ boolean shouldShowName(LivingEntity livingEntity) {
        return this.shouldShowName((T)((Mob)livingEntity));
    }
}

