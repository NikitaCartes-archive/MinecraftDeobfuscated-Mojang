package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CreakingModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.LivingEntityEmissiveLayer;
import net.minecraft.client.renderer.entity.state.CreakingRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.creaking.Creaking;

@Environment(EnvType.CLIENT)
public class CreakingRenderer<T extends Creaking> extends MobRenderer<T, CreakingRenderState, CreakingModel> {
	private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/creaking/creaking.png");
	private static final ResourceLocation EYES_TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/creaking/creaking_eyes.png");

	public CreakingRenderer(EntityRendererProvider.Context context) {
		super(context, new CreakingModel(context.bakeLayer(ModelLayers.CREAKING)), 0.7F);
		this.addLayer(
			new LivingEntityEmissiveLayer<>(this, EYES_TEXTURE_LOCATION, (creakingRenderState, f) -> 1.0F, CreakingModel::getHeadModelParts, RenderType::eyes)
		);
	}

	public ResourceLocation getTextureLocation(CreakingRenderState creakingRenderState) {
		return TEXTURE_LOCATION;
	}

	public CreakingRenderState createRenderState() {
		return new CreakingRenderState();
	}

	public void extractRenderState(T creaking, CreakingRenderState creakingRenderState, float f) {
		super.extractRenderState(creaking, creakingRenderState, f);
		creakingRenderState.attackAnimationState.copyFrom(creaking.attackAnimationState);
		creakingRenderState.invulnerabilityAnimationState.copyFrom(creaking.invulnerabilityAnimationState);
		creakingRenderState.isActive = creaking.isActive();
		creakingRenderState.canMove = creaking.canMove();
	}
}
