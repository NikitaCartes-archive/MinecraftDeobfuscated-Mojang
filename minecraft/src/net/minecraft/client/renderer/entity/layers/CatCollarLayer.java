package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CatModel;
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

	public void render(Cat cat, float f, float g, float h, float i, float j, float k, float l) {
		if (cat.isTame() && !cat.isInvisible()) {
			this.bindTexture(CAT_COLLAR_LOCATION);
			float[] fs = cat.getCollarColor().getTextureDiffuseColors();
			RenderSystem.color3f(fs[0], fs[1], fs[2]);
			this.getParentModel().copyPropertiesTo(this.catModel);
			this.catModel.prepareMobModel(cat, f, g, h);
			this.catModel.render(cat, f, g, i, j, k, l);
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return true;
	}
}
