package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Skeleton;

@Environment(EnvType.CLIENT)
public class SkeletonRenderer extends AbstractSkeletonRenderer<Skeleton, SkeletonRenderState> {
	private static final ResourceLocation SKELETON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png");

	public SkeletonRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.SKELETON, ModelLayers.SKELETON_INNER_ARMOR, ModelLayers.SKELETON_OUTER_ARMOR);
	}

	public ResourceLocation getTextureLocation(SkeletonRenderState skeletonRenderState) {
		return SKELETON_LOCATION;
	}

	public SkeletonRenderState createRenderState() {
		return new SkeletonRenderState();
	}
}
