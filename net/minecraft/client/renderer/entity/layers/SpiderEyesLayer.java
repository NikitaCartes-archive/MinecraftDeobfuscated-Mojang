/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class SpiderEyesLayer<T extends Entity, M extends SpiderModel<T>>
extends RenderLayer<T, M> {
    private static final ResourceLocation SPIDER_EYES_LOCATION = new ResourceLocation("textures/entity/spider_eyes.png");

    public SpiderEyesLayer(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(T entity, float f, float g, float h, float i, float j, float k, float l) {
        this.bindTexture(SPIDER_EYES_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        if (((Entity)entity).isInvisible()) {
            RenderSystem.depthMask(false);
        } else {
            RenderSystem.depthMask(true);
        }
        int m = 61680;
        int n = m % 65536;
        int o = m / 65536;
        RenderSystem.glMultiTexCoord2f(33985, n, o);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        FogRenderer.resetFogColor(true);
        ((SpiderModel)this.getParentModel()).render(entity, f, g, i, j, k, l);
        FogRenderer.resetFogColor(false);
        m = ((Entity)entity).getLightColor();
        n = m % 65536;
        o = m / 65536;
        RenderSystem.glMultiTexCoord2f(33985, n, o);
        this.setLightColor(entity);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

