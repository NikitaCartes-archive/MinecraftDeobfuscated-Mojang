/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerBulletModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ShulkerBullet;

@Environment(value=EnvType.CLIENT)
public class ShulkerBulletRenderer
extends EntityRenderer<ShulkerBullet> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/shulker/spark.png");
    private final ShulkerBulletModel<ShulkerBullet> model = new ShulkerBulletModel();

    public ShulkerBulletRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    private float rotlerp(float f, float g, float h) {
        float i;
        for (i = g - f; i < -180.0f; i += 360.0f) {
        }
        while (i >= 180.0f) {
            i -= 360.0f;
        }
        return f + h * i;
    }

    @Override
    public void render(ShulkerBullet shulkerBullet, double d, double e, double f, float g, float h) {
        RenderSystem.pushMatrix();
        float i = this.rotlerp(shulkerBullet.yRotO, shulkerBullet.yRot, h);
        float j = Mth.lerp(h, shulkerBullet.xRotO, shulkerBullet.xRot);
        float k = (float)shulkerBullet.tickCount + h;
        RenderSystem.translatef((float)d, (float)e + 0.15f, (float)f);
        RenderSystem.rotatef(Mth.sin(k * 0.1f) * 180.0f, 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef(Mth.cos(k * 0.1f) * 180.0f, 1.0f, 0.0f, 0.0f);
        RenderSystem.rotatef(Mth.sin(k * 0.15f) * 360.0f, 0.0f, 0.0f, 1.0f);
        float l = 0.03125f;
        RenderSystem.enableRescaleNormal();
        RenderSystem.scalef(-1.0f, -1.0f, 1.0f);
        this.bindTexture(shulkerBullet);
        this.model.render(shulkerBullet, 0.0f, 0.0f, 0.0f, i, j, 0.03125f);
        RenderSystem.enableBlend();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 0.5f);
        RenderSystem.scalef(1.5f, 1.5f, 1.5f);
        this.model.render(shulkerBullet, 0.0f, 0.0f, 0.0f, i, j, 0.03125f);
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
        super.render(shulkerBullet, d, e, f, g, h);
    }

    @Override
    protected ResourceLocation getTextureLocation(ShulkerBullet shulkerBullet) {
        return TEXTURE_LOCATION;
    }
}

