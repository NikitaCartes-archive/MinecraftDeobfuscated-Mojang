package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.FrogModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.FrogRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.frog.Frog;

@Environment(EnvType.CLIENT)
public class FrogRenderer extends MobRenderer<Frog, FrogRenderState, FrogModel> {
	public FrogRenderer(EntityRendererProvider.Context context) {
		super(context, new FrogModel(context.bakeLayer(ModelLayers.FROG)), 0.3F);
	}

	public ResourceLocation getTextureLocation(FrogRenderState frogRenderState) {
		return frogRenderState.texture;
	}

	public FrogRenderState createRenderState() {
		return new FrogRenderState();
	}

	public void extractRenderState(Frog frog, FrogRenderState frogRenderState, float f) {
		super.extractRenderState(frog, frogRenderState, f);
		frogRenderState.isSwimming = frog.isInWaterOrBubble();
		frogRenderState.jumpAnimationState.copyFrom(frog.jumpAnimationState);
		frogRenderState.croakAnimationState.copyFrom(frog.croakAnimationState);
		frogRenderState.tongueAnimationState.copyFrom(frog.tongueAnimationState);
		frogRenderState.swimIdleAnimationState.copyFrom(frog.swimIdleAnimationState);
		frogRenderState.texture = frog.getVariant().value().texture();
	}
}
