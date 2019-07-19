package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

@Environment(EnvType.CLIENT)
public class DrownedOuterLayer<T extends Zombie> extends RenderLayer<T, DrownedModel<T>> {
	private static final ResourceLocation DROWNED_OUTER_LAYER_LOCATION = new ResourceLocation("textures/entity/zombie/drowned_outer_layer.png");
	private final DrownedModel<T> model = new DrownedModel<>(0.25F, 0.0F, 64, 64);

	public DrownedOuterLayer(RenderLayerParent<T, DrownedModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(T zombie, float f, float g, float h, float i, float j, float k, float l) {
		if (!zombie.isInvisible()) {
			this.getParentModel().copyPropertiesTo(this.model);
			this.model.prepareMobModel(zombie, f, g, h);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.bindTexture(DROWNED_OUTER_LAYER_LOCATION);
			this.model.render(zombie, f, g, i, j, k, l);
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return true;
	}
}
