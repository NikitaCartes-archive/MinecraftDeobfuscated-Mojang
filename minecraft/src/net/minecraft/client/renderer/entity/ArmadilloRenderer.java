package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmadilloModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.ArmadilloRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.armadillo.Armadillo;

@Environment(EnvType.CLIENT)
public class ArmadilloRenderer extends AgeableMobRenderer<Armadillo, ArmadilloRenderState, ArmadilloModel> {
	private static final ResourceLocation ARMADILLO_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/armadillo.png");

	public ArmadilloRenderer(EntityRendererProvider.Context context) {
		super(context, new ArmadilloModel(context.bakeLayer(ModelLayers.ARMADILLO)), new ArmadilloModel(context.bakeLayer(ModelLayers.ARMADILLO_BABY)), 0.4F);
	}

	public ResourceLocation getTextureLocation(ArmadilloRenderState armadilloRenderState) {
		return ARMADILLO_LOCATION;
	}

	public ArmadilloRenderState createRenderState() {
		return new ArmadilloRenderState();
	}

	public void extractRenderState(Armadillo armadillo, ArmadilloRenderState armadilloRenderState, float f) {
		super.extractRenderState(armadillo, armadilloRenderState, f);
		armadilloRenderState.isHidingInShell = armadillo.shouldHideInShell();
		armadilloRenderState.peekAnimationState.copyFrom(armadillo.peekAnimationState);
		armadilloRenderState.rollOutAnimationState.copyFrom(armadillo.rollOutAnimationState);
		armadilloRenderState.rollUpAnimationState.copyFrom(armadillo.rollUpAnimationState);
	}
}
