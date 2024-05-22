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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.item.DyeColor;

@Environment(EnvType.CLIENT)
public class LlamaDecorLayer extends RenderLayer<Llama, LlamaModel<Llama>> {
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
	private final LlamaModel<Llama> model;

	public LlamaDecorLayer(RenderLayerParent<Llama, LlamaModel<Llama>> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.model = new LlamaModel<>(entityModelSet.bakeLayer(ModelLayers.LLAMA_DECOR));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Llama llama, float f, float g, float h, float j, float k, float l) {
		DyeColor dyeColor = llama.getSwag();
		ResourceLocation resourceLocation;
		if (dyeColor != null) {
			resourceLocation = TEXTURE_LOCATION[dyeColor.getId()];
		} else {
			if (!llama.isTraderLlama()) {
				return;
			}

			resourceLocation = TRADER_LLAMA;
		}

		this.getParentModel().copyPropertiesTo(this.model);
		this.model.setupAnim(llama, f, g, j, k, l);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(resourceLocation));
		this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
	}
}
