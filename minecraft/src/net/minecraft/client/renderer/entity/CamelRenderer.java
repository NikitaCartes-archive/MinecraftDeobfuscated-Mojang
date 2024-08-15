package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CamelModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.CamelRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.camel.Camel;

@Environment(EnvType.CLIENT)
public class CamelRenderer extends AgeableMobRenderer<Camel, CamelRenderState, CamelModel> {
	private static final ResourceLocation CAMEL_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/camel/camel.png");

	public CamelRenderer(EntityRendererProvider.Context context) {
		super(context, new CamelModel(context.bakeLayer(ModelLayers.CAMEL)), new CamelModel(context.bakeLayer(ModelLayers.CAMEL_BABY)), 0.7F);
	}

	public ResourceLocation getTextureLocation(CamelRenderState camelRenderState) {
		return CAMEL_LOCATION;
	}

	public CamelRenderState createRenderState() {
		return new CamelRenderState();
	}

	public void extractRenderState(Camel camel, CamelRenderState camelRenderState, float f) {
		super.extractRenderState(camel, camelRenderState, f);
		camelRenderState.isSaddled = camel.isSaddled();
		camelRenderState.isRidden = camel.isVehicle();
		camelRenderState.jumpCooldown = Math.max((float)camel.getJumpCooldown() - f, 0.0F);
		camelRenderState.sitAnimationState.copyFrom(camel.sitAnimationState);
		camelRenderState.sitPoseAnimationState.copyFrom(camel.sitPoseAnimationState);
		camelRenderState.sitUpAnimationState.copyFrom(camel.sitUpAnimationState);
		camelRenderState.idleAnimationState.copyFrom(camel.idleAnimationState);
		camelRenderState.dashAnimationState.copyFrom(camel.dashAnimationState);
	}
}
