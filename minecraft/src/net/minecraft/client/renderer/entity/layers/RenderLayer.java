package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public abstract class RenderLayer<S extends EntityRenderState, M extends EntityModel<? super S>> {
	private final RenderLayerParent<S, M> renderer;

	public RenderLayer(RenderLayerParent<S, M> renderLayerParent) {
		this.renderer = renderLayerParent;
	}

	protected static <S extends LivingEntityRenderState> void coloredCutoutModelCopyLayerRender(
		EntityModel<S> entityModel,
		ResourceLocation resourceLocation,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		S livingEntityRenderState,
		int j
	) {
		if (!livingEntityRenderState.isInvisible) {
			entityModel.setupAnim(livingEntityRenderState);
			renderColoredCutoutModel(entityModel, resourceLocation, poseStack, multiBufferSource, i, livingEntityRenderState, j);
		}
	}

	protected static void renderColoredCutoutModel(
		EntityModel<?> entityModel,
		ResourceLocation resourceLocation,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		LivingEntityRenderState livingEntityRenderState,
		int j
	) {
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(resourceLocation));
		entityModel.renderToBuffer(poseStack, vertexConsumer, i, LivingEntityRenderer.getOverlayCoords(livingEntityRenderState, 0.0F), j);
	}

	public M getParentModel() {
		return this.renderer.getModel();
	}

	public abstract void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S entityRenderState, float f, float g);
}
