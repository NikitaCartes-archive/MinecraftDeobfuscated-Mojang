package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.world.entity.monster.AbstractSkeleton;

@Environment(EnvType.CLIENT)
public abstract class AbstractSkeletonRenderer<T extends AbstractSkeleton, S extends SkeletonRenderState> extends HumanoidMobRenderer<T, S, SkeletonModel<S>> {
	public AbstractSkeletonRenderer(
		EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, ModelLayerLocation modelLayerLocation3
	) {
		this(context, modelLayerLocation2, modelLayerLocation3, new SkeletonModel<>(context.bakeLayer(modelLayerLocation)));
	}

	public AbstractSkeletonRenderer(
		EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, SkeletonModel<S> skeletonModel
	) {
		super(context, skeletonModel, 0.5F);
		this.addLayer(
			new HumanoidArmorLayer<>(
				this, new SkeletonModel(context.bakeLayer(modelLayerLocation)), new SkeletonModel(context.bakeLayer(modelLayerLocation2)), context.getModelManager()
			)
		);
	}

	public void extractRenderState(T abstractSkeleton, S skeletonRenderState, float f) {
		super.extractRenderState(abstractSkeleton, skeletonRenderState, f);
		skeletonRenderState.isAggressive = abstractSkeleton.isAggressive();
		skeletonRenderState.isShaking = abstractSkeleton.isShaking();
	}

	protected boolean isShaking(S skeletonRenderState) {
		return skeletonRenderState.isShaking;
	}
}
