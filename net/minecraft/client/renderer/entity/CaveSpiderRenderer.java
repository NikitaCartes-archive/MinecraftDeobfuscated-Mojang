/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.CaveSpider;

@Environment(value=EnvType.CLIENT)
public class CaveSpiderRenderer
extends SpiderRenderer<CaveSpider> {
    private static final ResourceLocation CAVE_SPIDER_LOCATION = new ResourceLocation("textures/entity/spider/cave_spider.png");

    public CaveSpiderRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
        this.shadowRadius *= 0.7f;
    }

    @Override
    protected void scale(CaveSpider caveSpider, float f) {
        GlStateManager.scalef(0.7f, 0.7f, 0.7f);
    }

    @Override
    protected ResourceLocation getTextureLocation(CaveSpider caveSpider) {
        return CAVE_SPIDER_LOCATION;
    }
}

