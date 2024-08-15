package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.WitherSkeleton;

@Environment(EnvType.CLIENT)
public class WitherSkeletonRenderer extends AbstractSkeletonRenderer<WitherSkeleton, SkeletonRenderState> {
	private static final ResourceLocation WITHER_SKELETON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png");

	public WitherSkeletonRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.WITHER_SKELETON, ModelLayers.WITHER_SKELETON_INNER_ARMOR, ModelLayers.WITHER_SKELETON_OUTER_ARMOR);
	}

	public ResourceLocation getTextureLocation(SkeletonRenderState skeletonRenderState) {
		return WITHER_SKELETON_LOCATION;
	}

	public SkeletonRenderState createRenderState() {
		return new SkeletonRenderState();
	}
}
