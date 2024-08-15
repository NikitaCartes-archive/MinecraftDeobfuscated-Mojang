package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LlamaRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class LlamaDecorLayer extends RenderLayer<LlamaRenderState, LlamaModel> {
	private static final ResourceLocation[] TEXTURE_LOCATION = new ResourceLocation[]{
		ResourceLocation.withDefaultNamespace("textures/entity/llama/decor/white.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/llama/decor/orange.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/llama/decor/magenta.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/llama/decor/light_blue.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/llama/decor/yellow.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/llama/decor/lime.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/llama/decor/pink.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/llama/decor/gray.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/llama/decor/light_gray.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/llama/decor/cyan.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/llama/decor/purple.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/llama/decor/blue.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/llama/decor/brown.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/llama/decor/green.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/llama/decor/red.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/llama/decor/black.png")
	};
	private static final ResourceLocation TRADER_LLAMA = ResourceLocation.withDefaultNamespace("textures/entity/llama/decor/trader_llama.png");
	private final LlamaModel adultModel;
	private final LlamaModel babyModel;

	public LlamaDecorLayer(RenderLayerParent<LlamaRenderState, LlamaModel> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.adultModel = new LlamaModel(entityModelSet.bakeLayer(ModelLayers.LLAMA_DECOR));
		this.babyModel = new LlamaModel(entityModelSet.bakeLayer(ModelLayers.LLAMA_BABY_DECOR));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, LlamaRenderState llamaRenderState, float f, float g) {
		ResourceLocation resourceLocation;
		if (llamaRenderState.decorColor != null) {
			resourceLocation = TEXTURE_LOCATION[llamaRenderState.decorColor.getId()];
		} else {
			if (!llamaRenderState.isTraderLlama) {
				return;
			}

			resourceLocation = TRADER_LLAMA;
		}

		LlamaModel llamaModel = llamaRenderState.isBaby ? this.babyModel : this.adultModel;
		llamaModel.setupAnim(llamaRenderState);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(resourceLocation));
		llamaModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
	}
}
