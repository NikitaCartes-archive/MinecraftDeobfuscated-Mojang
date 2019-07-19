package net.minecraft.client.renderer.entity.layers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;

@Environment(EnvType.CLIENT)
public class PigSaddleLayer extends RenderLayer<Pig, PigModel<Pig>> {
	private static final ResourceLocation SADDLE_LOCATION = new ResourceLocation("textures/entity/pig/pig_saddle.png");
	private final PigModel<Pig> model = new PigModel<>(0.5F);

	public PigSaddleLayer(RenderLayerParent<Pig, PigModel<Pig>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(Pig pig, float f, float g, float h, float i, float j, float k, float l) {
		if (pig.hasSaddle()) {
			this.bindTexture(SADDLE_LOCATION);
			this.getParentModel().copyPropertiesTo(this.model);
			this.model.render(pig, f, g, i, j, k, l);
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
