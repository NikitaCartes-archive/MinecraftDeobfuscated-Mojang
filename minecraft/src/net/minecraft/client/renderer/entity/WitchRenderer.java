package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Witch;

@Environment(EnvType.CLIENT)
public class WitchRenderer extends MobRenderer<Witch, WitchModel<Witch>> {
	private static final ResourceLocation WITCH_LOCATION = new ResourceLocation("textures/entity/witch.png");

	public WitchRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new WitchModel<>(0.0F), 0.5F);
		this.addLayer(new WitchItemLayer<>(this));
	}

	public void render(Witch witch, double d, double e, double f, float g, float h) {
		this.model.setHoldingItem(!witch.getMainHandItem().isEmpty());
		super.render(witch, d, e, f, g, h);
	}

	protected ResourceLocation getTextureLocation(Witch witch) {
		return WITCH_LOCATION;
	}

	protected void scale(Witch witch, float f) {
		float g = 0.9375F;
		RenderSystem.scalef(0.9375F, 0.9375F, 0.9375F);
	}
}
