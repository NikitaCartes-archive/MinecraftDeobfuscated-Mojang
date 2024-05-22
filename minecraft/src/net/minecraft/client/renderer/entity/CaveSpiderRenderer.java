package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.CaveSpider;

@Environment(EnvType.CLIENT)
public class CaveSpiderRenderer extends SpiderRenderer<CaveSpider> {
	private static final ResourceLocation CAVE_SPIDER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/spider/cave_spider.png");
	private static final float SCALE = 0.7F;

	public CaveSpiderRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.CAVE_SPIDER);
		this.shadowRadius *= 0.7F;
	}

	protected void scale(CaveSpider caveSpider, PoseStack poseStack, float f) {
		poseStack.scale(0.7F, 0.7F, 0.7F);
	}

	public ResourceLocation getTextureLocation(CaveSpider caveSpider) {
		return CAVE_SPIDER_LOCATION;
	}
}
