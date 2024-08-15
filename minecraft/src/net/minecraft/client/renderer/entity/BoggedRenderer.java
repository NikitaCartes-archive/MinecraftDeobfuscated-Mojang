package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BoggedModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import net.minecraft.client.renderer.entity.state.BoggedRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Bogged;

@Environment(EnvType.CLIENT)
public class BoggedRenderer extends AbstractSkeletonRenderer<Bogged, BoggedRenderState> {
	private static final ResourceLocation BOGGED_SKELETON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/bogged.png");
	private static final ResourceLocation BOGGED_OUTER_LAYER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/bogged_overlay.png");

	public BoggedRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.BOGGED_INNER_ARMOR, ModelLayers.BOGGED_OUTER_ARMOR, new BoggedModel(context.bakeLayer(ModelLayers.BOGGED)));
		this.addLayer(new SkeletonClothingLayer<>(this, context.getModelSet(), ModelLayers.BOGGED_OUTER_LAYER, BOGGED_OUTER_LAYER_LOCATION));
	}

	public ResourceLocation getTextureLocation(BoggedRenderState boggedRenderState) {
		return BOGGED_SKELETON_LOCATION;
	}

	public BoggedRenderState createRenderState() {
		return new BoggedRenderState();
	}

	public void extractRenderState(Bogged bogged, BoggedRenderState boggedRenderState, float f) {
		super.extractRenderState(bogged, boggedRenderState, f);
		boggedRenderState.isSheared = bogged.isSheared();
	}
}
