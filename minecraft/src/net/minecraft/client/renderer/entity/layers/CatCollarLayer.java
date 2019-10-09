package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cat;

@Environment(EnvType.CLIENT)
public class CatCollarLayer extends RenderLayer<Cat, CatModel<Cat>> {
	private static final ResourceLocation CAT_COLLAR_LOCATION = new ResourceLocation("textures/entity/cat/cat_collar.png");
	private final CatModel<Cat> catModel = new CatModel<>(0.01F);

	public CatCollarLayer(RenderLayerParent<Cat, CatModel<Cat>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Cat cat, float f, float g, float h, float j, float k, float l, float m) {
		if (cat.isTame()) {
			float[] fs = cat.getCollarColor().getTextureDiffuseColors();
			coloredCutoutModelCopyLayerRender(
				this.getParentModel(), this.catModel, CAT_COLLAR_LOCATION, poseStack, multiBufferSource, i, cat, f, g, j, k, l, m, h, fs[0], fs[1], fs[2]
			);
		}
	}
}
