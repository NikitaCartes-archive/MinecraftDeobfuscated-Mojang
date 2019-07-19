/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Spider;

@Environment(value=EnvType.CLIENT)
public class SpiderRenderer<T extends Spider>
extends MobRenderer<T, SpiderModel<T>> {
    private static final ResourceLocation SPIDER_LOCATION = new ResourceLocation("textures/entity/spider/spider.png");

    public SpiderRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new SpiderModel(), 0.8f);
        this.addLayer(new SpiderEyesLayer(this));
    }

    @Override
    protected float getFlipDegrees(T spider) {
        return 180.0f;
    }

    @Override
    protected ResourceLocation getTextureLocation(T spider) {
        return SPIDER_LOCATION;
    }
}

