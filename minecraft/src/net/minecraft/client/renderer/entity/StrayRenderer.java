package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.StrayClothingLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;

@Environment(EnvType.CLIENT)
public class StrayRenderer extends SkeletonRenderer {
	private static final ResourceLocation STRAY_SKELETON_LOCATION = new ResourceLocation("textures/entity/skeleton/stray.png");

	public StrayRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.STRAY, ModelLayers.STRAY_INNER_ARMOR, ModelLayers.STRAY_OUTER_ARMOR);
		this.addLayer(new StrayClothingLayer<>(this, context.getModelSet()));
	}

	@Override
	public ResourceLocation getTextureLocation(AbstractSkeleton abstractSkeleton) {
		return STRAY_SKELETON_LOCATION;
	}
}
