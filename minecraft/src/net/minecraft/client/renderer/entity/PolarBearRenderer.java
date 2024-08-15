package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PolarBearModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.PolarBearRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.PolarBear;

@Environment(EnvType.CLIENT)
public class PolarBearRenderer extends AgeableMobRenderer<PolarBear, PolarBearRenderState, PolarBearModel> {
	private static final ResourceLocation BEAR_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/bear/polarbear.png");

	public PolarBearRenderer(EntityRendererProvider.Context context) {
		super(context, new PolarBearModel(context.bakeLayer(ModelLayers.POLAR_BEAR)), new PolarBearModel(context.bakeLayer(ModelLayers.POLAR_BEAR_BABY)), 0.9F);
	}

	public ResourceLocation getTextureLocation(PolarBearRenderState polarBearRenderState) {
		return BEAR_LOCATION;
	}

	public PolarBearRenderState createRenderState() {
		return new PolarBearRenderState();
	}

	public void extractRenderState(PolarBear polarBear, PolarBearRenderState polarBearRenderState, float f) {
		super.extractRenderState(polarBear, polarBearRenderState, f);
		polarBearRenderState.standScale = polarBear.getStandingAnimationScale(f);
	}
}
