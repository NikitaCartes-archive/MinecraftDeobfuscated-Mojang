package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SaddleableRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class SaddleLayer<S extends LivingEntityRenderState & SaddleableRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
	private final ResourceLocation textureLocation;
	private final M adultModel;
	private final M babyModel;

	public SaddleLayer(RenderLayerParent<S, M> renderLayerParent, M entityModel, M entityModel2, ResourceLocation resourceLocation) {
		super(renderLayerParent);
		this.adultModel = entityModel;
		this.babyModel = entityModel2;
		this.textureLocation = resourceLocation;
	}

	public SaddleLayer(RenderLayerParent<S, M> renderLayerParent, M entityModel, ResourceLocation resourceLocation) {
		this(renderLayerParent, entityModel, entityModel, resourceLocation);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S livingEntityRenderState, float f, float g) {
		if (livingEntityRenderState.isSaddled()) {
			M entityModel = livingEntityRenderState.isBaby ? this.babyModel : this.adultModel;
			entityModel.setupAnim(livingEntityRenderState);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(this.textureLocation));
			entityModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
		}
	}
}
