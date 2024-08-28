package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.SpectralArrow;

@Environment(EnvType.CLIENT)
public class SpectralArrowRenderer extends ArrowRenderer<SpectralArrow, ArrowRenderState> {
	public static final ResourceLocation SPECTRAL_ARROW_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/spectral_arrow.png");

	public SpectralArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected ResourceLocation getTextureLocation(ArrowRenderState arrowRenderState) {
		return SPECTRAL_ARROW_LOCATION;
	}

	public ArrowRenderState createRenderState() {
		return new ArrowRenderState();
	}
}
