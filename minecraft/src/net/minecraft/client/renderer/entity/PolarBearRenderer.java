package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PolarBearModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.PolarBear;

@Environment(EnvType.CLIENT)
public class PolarBearRenderer extends MobRenderer<PolarBear, PolarBearModel<PolarBear>> {
	private static final ResourceLocation BEAR_LOCATION = new ResourceLocation("textures/entity/bear/polarbear.png");

	public PolarBearRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new PolarBearModel<>(), 0.9F);
	}

	protected ResourceLocation getTextureLocation(PolarBear polarBear) {
		return BEAR_LOCATION;
	}

	protected void scale(PolarBear polarBear, float f) {
		RenderSystem.scalef(1.2F, 1.2F, 1.2F);
		super.scale(polarBear, f);
	}
}
