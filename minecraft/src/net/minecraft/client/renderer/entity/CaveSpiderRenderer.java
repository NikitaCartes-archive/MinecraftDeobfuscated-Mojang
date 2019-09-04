package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.CaveSpider;

@Environment(EnvType.CLIENT)
public class CaveSpiderRenderer extends SpiderRenderer<CaveSpider> {
	private static final ResourceLocation CAVE_SPIDER_LOCATION = new ResourceLocation("textures/entity/spider/cave_spider.png");

	public CaveSpiderRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
		this.shadowRadius *= 0.7F;
	}

	protected void scale(CaveSpider caveSpider, float f) {
		RenderSystem.scalef(0.7F, 0.7F, 0.7F);
	}

	protected ResourceLocation getTextureLocation(CaveSpider caveSpider) {
		return CAVE_SPIDER_LOCATION;
	}
}
