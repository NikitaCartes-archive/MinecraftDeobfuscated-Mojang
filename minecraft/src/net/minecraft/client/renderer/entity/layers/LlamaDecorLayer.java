package net.minecraft.client.renderer.entity.layers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.item.DyeColor;

@Environment(EnvType.CLIENT)
public class LlamaDecorLayer extends RenderLayer<Llama, LlamaModel<Llama>> {
	private static final ResourceLocation[] TEXTURE_LOCATION = new ResourceLocation[]{
		new ResourceLocation("textures/entity/llama/decor/white.png"),
		new ResourceLocation("textures/entity/llama/decor/orange.png"),
		new ResourceLocation("textures/entity/llama/decor/magenta.png"),
		new ResourceLocation("textures/entity/llama/decor/light_blue.png"),
		new ResourceLocation("textures/entity/llama/decor/yellow.png"),
		new ResourceLocation("textures/entity/llama/decor/lime.png"),
		new ResourceLocation("textures/entity/llama/decor/pink.png"),
		new ResourceLocation("textures/entity/llama/decor/gray.png"),
		new ResourceLocation("textures/entity/llama/decor/light_gray.png"),
		new ResourceLocation("textures/entity/llama/decor/cyan.png"),
		new ResourceLocation("textures/entity/llama/decor/purple.png"),
		new ResourceLocation("textures/entity/llama/decor/blue.png"),
		new ResourceLocation("textures/entity/llama/decor/brown.png"),
		new ResourceLocation("textures/entity/llama/decor/green.png"),
		new ResourceLocation("textures/entity/llama/decor/red.png"),
		new ResourceLocation("textures/entity/llama/decor/black.png")
	};
	private static final ResourceLocation TRADER_LLAMA = new ResourceLocation("textures/entity/llama/decor/trader_llama.png");
	private final LlamaModel<Llama> model = new LlamaModel<>(0.5F);

	public LlamaDecorLayer(RenderLayerParent<Llama, LlamaModel<Llama>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(Llama llama, float f, float g, float h, float i, float j, float k, float l) {
		DyeColor dyeColor = llama.getSwag();
		if (dyeColor != null) {
			this.bindTexture(TEXTURE_LOCATION[dyeColor.getId()]);
		} else {
			if (!llama.isTraderLlama()) {
				return;
			}

			this.bindTexture(TRADER_LLAMA);
		}

		this.getParentModel().copyPropertiesTo(this.model);
		this.model.render(llama, f, g, i, j, k, l);
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
