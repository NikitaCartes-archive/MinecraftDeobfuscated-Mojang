package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.layers.LlamaDecorLayer;
import net.minecraft.client.renderer.entity.state.LlamaRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Llama;

@Environment(EnvType.CLIENT)
public class LlamaRenderer extends AgeableMobRenderer<Llama, LlamaRenderState, LlamaModel> {
	private static final ResourceLocation CREAMY = ResourceLocation.withDefaultNamespace("textures/entity/llama/creamy.png");
	private static final ResourceLocation WHITE = ResourceLocation.withDefaultNamespace("textures/entity/llama/white.png");
	private static final ResourceLocation BROWN = ResourceLocation.withDefaultNamespace("textures/entity/llama/brown.png");
	private static final ResourceLocation GRAY = ResourceLocation.withDefaultNamespace("textures/entity/llama/gray.png");

	public LlamaRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2) {
		super(context, new LlamaModel(context.bakeLayer(modelLayerLocation)), new LlamaModel(context.bakeLayer(modelLayerLocation2)), 0.7F);
		this.addLayer(new LlamaDecorLayer(this, context.getModelSet(), context.getEquipmentRenderer()));
	}

	public ResourceLocation getTextureLocation(LlamaRenderState llamaRenderState) {
		return switch (llamaRenderState.variant) {
			case CREAMY -> CREAMY;
			case WHITE -> WHITE;
			case BROWN -> BROWN;
			case GRAY -> GRAY;
		};
	}

	public LlamaRenderState createRenderState() {
		return new LlamaRenderState();
	}

	public void extractRenderState(Llama llama, LlamaRenderState llamaRenderState, float f) {
		super.extractRenderState(llama, llamaRenderState, f);
		llamaRenderState.variant = llama.getVariant();
		llamaRenderState.hasChest = !llama.isBaby() && llama.hasChest();
		llamaRenderState.bodyItem = llama.getBodyArmorItem();
		llamaRenderState.isTraderLlama = llama.isTraderLlama();
	}
}
