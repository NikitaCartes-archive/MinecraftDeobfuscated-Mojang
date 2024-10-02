package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.LivingEntityEmissiveLayer;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.warden.Warden;

@Environment(EnvType.CLIENT)
public class WardenRenderer extends MobRenderer<Warden, WardenRenderState, WardenModel> {
	private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/warden/warden.png");
	private static final ResourceLocation BIOLUMINESCENT_LAYER_TEXTURE = ResourceLocation.withDefaultNamespace(
		"textures/entity/warden/warden_bioluminescent_layer.png"
	);
	private static final ResourceLocation HEART_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/warden/warden_heart.png");
	private static final ResourceLocation PULSATING_SPOTS_TEXTURE_1 = ResourceLocation.withDefaultNamespace("textures/entity/warden/warden_pulsating_spots_1.png");
	private static final ResourceLocation PULSATING_SPOTS_TEXTURE_2 = ResourceLocation.withDefaultNamespace("textures/entity/warden/warden_pulsating_spots_2.png");

	public WardenRenderer(EntityRendererProvider.Context context) {
		super(context, new WardenModel(context.bakeLayer(ModelLayers.WARDEN)), 0.9F);
		this.addLayer(
			new LivingEntityEmissiveLayer<>(
				this, BIOLUMINESCENT_LAYER_TEXTURE, (wardenRenderState, f) -> 1.0F, WardenModel::getBioluminescentLayerModelParts, RenderType::entityTranslucentEmissive
			)
		);
		this.addLayer(
			new LivingEntityEmissiveLayer<>(
				this,
				PULSATING_SPOTS_TEXTURE_1,
				(wardenRenderState, f) -> Math.max(0.0F, Mth.cos(f * 0.045F) * 0.25F),
				WardenModel::getPulsatingSpotsLayerModelParts,
				RenderType::entityTranslucentEmissive
			)
		);
		this.addLayer(
			new LivingEntityEmissiveLayer<>(
				this,
				PULSATING_SPOTS_TEXTURE_2,
				(wardenRenderState, f) -> Math.max(0.0F, Mth.cos(f * 0.045F + (float) Math.PI) * 0.25F),
				WardenModel::getPulsatingSpotsLayerModelParts,
				RenderType::entityTranslucentEmissive
			)
		);
		this.addLayer(
			new LivingEntityEmissiveLayer<>(
				this, TEXTURE, (wardenRenderState, f) -> wardenRenderState.tendrilAnimation, WardenModel::getTendrilsLayerModelParts, RenderType::entityTranslucentEmissive
			)
		);
		this.addLayer(
			new LivingEntityEmissiveLayer<>(
				this,
				HEART_TEXTURE,
				(wardenRenderState, f) -> wardenRenderState.heartAnimation,
				WardenModel::getHeartLayerModelParts,
				RenderType::entityTranslucentEmissive
			)
		);
	}

	public ResourceLocation getTextureLocation(WardenRenderState wardenRenderState) {
		return TEXTURE;
	}

	public WardenRenderState createRenderState() {
		return new WardenRenderState();
	}

	public void extractRenderState(Warden warden, WardenRenderState wardenRenderState, float f) {
		super.extractRenderState(warden, wardenRenderState, f);
		wardenRenderState.tendrilAnimation = warden.getTendrilAnimation(f);
		wardenRenderState.heartAnimation = warden.getHeartAnimation(f);
		wardenRenderState.roarAnimationState.copyFrom(warden.roarAnimationState);
		wardenRenderState.sniffAnimationState.copyFrom(warden.sniffAnimationState);
		wardenRenderState.emergeAnimationState.copyFrom(warden.emergeAnimationState);
		wardenRenderState.diggingAnimationState.copyFrom(warden.diggingAnimationState);
		wardenRenderState.attackAnimationState.copyFrom(warden.attackAnimationState);
		wardenRenderState.sonicBoomAnimationState.copyFrom(warden.sonicBoomAnimationState);
	}
}
