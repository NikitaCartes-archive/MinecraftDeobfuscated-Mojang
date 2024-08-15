package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.CaveSpider;

@Environment(EnvType.CLIENT)
public class CaveSpiderRenderer extends SpiderRenderer<CaveSpider> {
	private static final ResourceLocation CAVE_SPIDER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/spider/cave_spider.png");

	public CaveSpiderRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.CAVE_SPIDER);
		this.shadowRadius = 0.56F;
	}

	@Override
	public ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
		return CAVE_SPIDER_LOCATION;
	}
}
