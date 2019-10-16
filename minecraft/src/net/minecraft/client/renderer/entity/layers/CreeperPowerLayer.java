package net.minecraft.client.renderer.entity.layers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Creeper;

@Environment(EnvType.CLIENT)
public class CreeperPowerLayer extends EnergySwirlLayer<Creeper, CreeperModel<Creeper>> {
	private static final ResourceLocation POWER_LOCATION = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
	private final CreeperModel<Creeper> model = new CreeperModel<>(2.0F);

	public CreeperPowerLayer(RenderLayerParent<Creeper, CreeperModel<Creeper>> renderLayerParent) {
		super(renderLayerParent);
	}

	@Override
	protected float xOffset(float f) {
		return f * 0.01F;
	}

	@Override
	protected ResourceLocation getTextureLocation() {
		return POWER_LOCATION;
	}

	@Override
	protected EntityModel<Creeper> model() {
		return this.model;
	}
}
