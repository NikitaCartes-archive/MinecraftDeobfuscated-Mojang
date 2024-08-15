package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class SkeletonClothingLayer<S extends SkeletonRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
	private final SkeletonModel<S> layerModel;
	private final ResourceLocation clothesLocation;

	public SkeletonClothingLayer(
		RenderLayerParent<S, M> renderLayerParent, EntityModelSet entityModelSet, ModelLayerLocation modelLayerLocation, ResourceLocation resourceLocation
	) {
		super(renderLayerParent);
		this.clothesLocation = resourceLocation;
		this.layerModel = new SkeletonModel<>(entityModelSet.bakeLayer(modelLayerLocation));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S skeletonRenderState, float f, float g) {
		coloredCutoutModelCopyLayerRender(this.layerModel, this.clothesLocation, poseStack, multiBufferSource, i, skeletonRenderState, -1);
	}
}
