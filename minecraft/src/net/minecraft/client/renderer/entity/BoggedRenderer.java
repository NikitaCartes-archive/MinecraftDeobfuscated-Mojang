package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BoggedModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Bogged;

@Environment(EnvType.CLIENT)
public class BoggedRenderer extends SkeletonRenderer<Bogged> {
	private static final ResourceLocation BOGGED_SKELETON_LOCATION = new ResourceLocation("textures/entity/skeleton/bogged.png");
	private static final ResourceLocation BOGGED_OUTER_LAYER_LOCATION = new ResourceLocation("textures/entity/skeleton/bogged_overlay.png");

	public BoggedRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.BOGGED_INNER_ARMOR, ModelLayers.BOGGED_OUTER_ARMOR, new BoggedModel(context.bakeLayer(ModelLayers.BOGGED)));
		this.addLayer(new SkeletonClothingLayer<>(this, context.getModelSet(), ModelLayers.BOGGED_OUTER_LAYER, BOGGED_OUTER_LAYER_LOCATION));
	}

	public ResourceLocation getTextureLocation(Bogged bogged) {
		return BOGGED_SKELETON_LOCATION;
	}
}
