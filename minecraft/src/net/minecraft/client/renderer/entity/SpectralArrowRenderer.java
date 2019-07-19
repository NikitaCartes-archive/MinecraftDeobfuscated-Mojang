package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.SpectralArrow;

@Environment(EnvType.CLIENT)
public class SpectralArrowRenderer extends ArrowRenderer<SpectralArrow> {
	public static final ResourceLocation SPECTRAL_ARROW_LOCATION = new ResourceLocation("textures/entity/projectiles/spectral_arrow.png");

	public SpectralArrowRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	protected ResourceLocation getTextureLocation(SpectralArrow spectralArrow) {
		return SPECTRAL_ARROW_LOCATION;
	}
}
