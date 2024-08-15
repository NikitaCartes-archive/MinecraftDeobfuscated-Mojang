package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.BatRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ambient.Bat;

@Environment(EnvType.CLIENT)
public class BatRenderer extends MobRenderer<Bat, BatRenderState, BatModel> {
	private static final ResourceLocation BAT_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/bat.png");

	public BatRenderer(EntityRendererProvider.Context context) {
		super(context, new BatModel(context.bakeLayer(ModelLayers.BAT)), 0.25F);
	}

	public ResourceLocation getTextureLocation(BatRenderState batRenderState) {
		return BAT_LOCATION;
	}

	public BatRenderState createRenderState() {
		return new BatRenderState();
	}

	public void extractRenderState(Bat bat, BatRenderState batRenderState, float f) {
		super.extractRenderState(bat, batRenderState, f);
		batRenderState.isResting = bat.isResting();
		batRenderState.flyAnimationState.copyFrom(bat.flyAnimationState);
		batRenderState.restAnimationState.copyFrom(bat.restAnimationState);
	}
}
