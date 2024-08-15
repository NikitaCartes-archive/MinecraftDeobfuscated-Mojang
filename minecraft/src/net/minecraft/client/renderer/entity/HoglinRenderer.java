package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.hoglin.Hoglin;

@Environment(EnvType.CLIENT)
public class HoglinRenderer extends AbstractHoglinRenderer<Hoglin> {
	private static final ResourceLocation HOGLIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/hoglin/hoglin.png");

	public HoglinRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.HOGLIN, ModelLayers.HOGLIN_BABY, 0.7F);
	}

	public ResourceLocation getTextureLocation(HoglinRenderState hoglinRenderState) {
		return HOGLIN_LOCATION;
	}

	public void extractRenderState(Hoglin hoglin, HoglinRenderState hoglinRenderState, float f) {
		super.extractRenderState(hoglin, hoglinRenderState, f);
		hoglinRenderState.isConverting = hoglin.isConverting();
	}

	protected boolean isShaking(HoglinRenderState hoglinRenderState) {
		return super.isShaking(hoglinRenderState) || hoglinRenderState.isConverting;
	}
}
