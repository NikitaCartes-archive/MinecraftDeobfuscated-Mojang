package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.PiglinRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.CrossbowItem;

@Environment(EnvType.CLIENT)
public class PiglinRenderer extends HumanoidMobRenderer<AbstractPiglin, PiglinRenderState, PiglinModel> {
	private static final ResourceLocation PIGLIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/piglin/piglin.png");
	private static final ResourceLocation PIGLIN_BRUTE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/piglin/piglin_brute.png");
	public static final CustomHeadLayer.Transforms PIGLIN_CUSTOM_HEAD_TRANSFORMS = new CustomHeadLayer.Transforms(0.0F, 0.0F, 1.0019531F);

	public PiglinRenderer(
		EntityRendererProvider.Context context,
		ModelLayerLocation modelLayerLocation,
		ModelLayerLocation modelLayerLocation2,
		ModelLayerLocation modelLayerLocation3,
		ModelLayerLocation modelLayerLocation4,
		ModelLayerLocation modelLayerLocation5,
		ModelLayerLocation modelLayerLocation6
	) {
		super(
			context,
			new PiglinModel(context.bakeLayer(modelLayerLocation)),
			new PiglinModel(context.bakeLayer(modelLayerLocation2)),
			0.5F,
			PIGLIN_CUSTOM_HEAD_TRANSFORMS
		);
		this.addLayer(
			new HumanoidArmorLayer<>(
				this,
				new HumanoidArmorModel(context.bakeLayer(modelLayerLocation3)),
				new HumanoidArmorModel(context.bakeLayer(modelLayerLocation4)),
				new HumanoidArmorModel(context.bakeLayer(modelLayerLocation5)),
				new HumanoidArmorModel(context.bakeLayer(modelLayerLocation6)),
				context.getModelManager()
			)
		);
	}

	public ResourceLocation getTextureLocation(PiglinRenderState piglinRenderState) {
		return piglinRenderState.isBrute ? PIGLIN_BRUTE_LOCATION : PIGLIN_LOCATION;
	}

	public PiglinRenderState createRenderState() {
		return new PiglinRenderState();
	}

	public void extractRenderState(AbstractPiglin abstractPiglin, PiglinRenderState piglinRenderState, float f) {
		super.extractRenderState(abstractPiglin, piglinRenderState, f);
		piglinRenderState.isBrute = abstractPiglin.getType() == EntityType.PIGLIN_BRUTE;
		piglinRenderState.armPose = abstractPiglin.getArmPose();
		piglinRenderState.maxCrossbowChageDuration = (float)CrossbowItem.getChargeDuration(abstractPiglin.getUseItem(), abstractPiglin);
		piglinRenderState.isConverting = abstractPiglin.isConverting();
	}

	protected boolean isShaking(PiglinRenderState piglinRenderState) {
		return super.isShaking(piglinRenderState) || piglinRenderState.isConverting;
	}
}
