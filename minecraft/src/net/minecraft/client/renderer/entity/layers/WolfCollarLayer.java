package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

@Environment(EnvType.CLIENT)
public class WolfCollarLayer extends RenderLayer<Wolf, WolfModel<Wolf>> {
	private static final ResourceLocation WOLF_COLLAR_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_collar.png");

	public WolfCollarLayer(RenderLayerParent<Wolf, WolfModel<Wolf>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(Wolf wolf, float f, float g, float h, float i, float j, float k, float l) {
		if (wolf.isTame() && !wolf.isInvisible()) {
			this.bindTexture(WOLF_COLLAR_LOCATION);
			float[] fs = wolf.getCollarColor().getTextureDiffuseColors();
			GlStateManager.color3f(fs[0], fs[1], fs[2]);
			this.getParentModel().render(wolf, f, g, i, j, k, l);
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return true;
	}
}
