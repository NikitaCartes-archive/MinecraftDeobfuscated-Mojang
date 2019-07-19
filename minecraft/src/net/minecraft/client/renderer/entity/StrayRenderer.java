package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.layers.StrayClothingLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;

@Environment(EnvType.CLIENT)
public class StrayRenderer extends SkeletonRenderer {
	private static final ResourceLocation STRAY_SKELETON_LOCATION = new ResourceLocation("textures/entity/skeleton/stray.png");

	public StrayRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
		this.addLayer(new StrayClothingLayer<>(this));
	}

	@Override
	protected ResourceLocation getTextureLocation(AbstractSkeleton abstractSkeleton) {
		return STRAY_SKELETON_LOCATION;
	}
}
