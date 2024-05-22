package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;

@Environment(EnvType.CLIENT)
public class SkeletonRenderer<T extends AbstractSkeleton> extends HumanoidMobRenderer<T, SkeletonModel<T>> {
	private static final ResourceLocation SKELETON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png");

	public SkeletonRenderer(EntityRendererProvider.Context context) {
		this(context, ModelLayers.SKELETON, ModelLayers.SKELETON_INNER_ARMOR, ModelLayers.SKELETON_OUTER_ARMOR);
	}

	public SkeletonRenderer(
		EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, ModelLayerLocation modelLayerLocation3
	) {
		this(context, modelLayerLocation2, modelLayerLocation3, new SkeletonModel<>(context.bakeLayer(modelLayerLocation)));
	}

	public SkeletonRenderer(
		EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, SkeletonModel<T> skeletonModel
	) {
		super(context, skeletonModel, 0.5F);
		this.addLayer(
			new HumanoidArmorLayer<>(
				this, new SkeletonModel(context.bakeLayer(modelLayerLocation)), new SkeletonModel(context.bakeLayer(modelLayerLocation2)), context.getModelManager()
			)
		);
	}

	public ResourceLocation getTextureLocation(T abstractSkeleton) {
		return SKELETON_LOCATION;
	}

	protected boolean isShaking(T abstractSkeleton) {
		return abstractSkeleton.isShaking();
	}
}
