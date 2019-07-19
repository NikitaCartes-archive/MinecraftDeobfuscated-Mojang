package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.animal.TropicalFish;

@Environment(EnvType.CLIENT)
public class TropicalFishPatternLayer extends RenderLayer<TropicalFish, EntityModel<TropicalFish>> {
	private final TropicalFishModelA<TropicalFish> modelA = new TropicalFishModelA<>(0.008F);
	private final TropicalFishModelB<TropicalFish> modelB = new TropicalFishModelB<>(0.008F);

	public TropicalFishPatternLayer(RenderLayerParent<TropicalFish, EntityModel<TropicalFish>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(TropicalFish tropicalFish, float f, float g, float h, float i, float j, float k, float l) {
		if (!tropicalFish.isInvisible()) {
			EntityModel<TropicalFish> entityModel = (EntityModel<TropicalFish>)(tropicalFish.getBaseVariant() == 0 ? this.modelA : this.modelB);
			this.bindTexture(tropicalFish.getPatternTextureLocation());
			float[] fs = tropicalFish.getPatternColor();
			GlStateManager.color3f(fs[0], fs[1], fs[2]);
			this.getParentModel().copyPropertiesTo(entityModel);
			entityModel.prepareMobModel(tropicalFish, f, g, h);
			entityModel.render(tropicalFish, f, g, i, j, k, l);
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return true;
	}
}
