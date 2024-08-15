package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.ZombifiedPiglinModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.ZombifiedPiglinRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.ZombifiedPiglin;

@Environment(EnvType.CLIENT)
public class ZombifiedPiglinRenderer extends HumanoidMobRenderer<ZombifiedPiglin, ZombifiedPiglinRenderState, ZombifiedPiglinModel> {
	private static final ResourceLocation ZOMBIFIED_PIGLIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/piglin/zombified_piglin.png");

	public ZombifiedPiglinRenderer(
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
			new ZombifiedPiglinModel(context.bakeLayer(modelLayerLocation)),
			new ZombifiedPiglinModel(context.bakeLayer(modelLayerLocation2)),
			0.5F,
			PiglinRenderer.PIGLIN_CUSTOM_HEAD_TRANSFORMS
		);
		this.addLayer(
			new HumanoidArmorLayer<>(
				this,
				new HumanoidArmorModel(context.bakeLayer(modelLayerLocation3)),
				new HumanoidArmorModel(context.bakeLayer(modelLayerLocation4)),
				new HumanoidArmorModel(context.bakeLayer(modelLayerLocation5)),
				new HumanoidArmorModel(context.bakeLayer(modelLayerLocation5)),
				context.getModelManager()
			)
		);
	}

	public ResourceLocation getTextureLocation(ZombifiedPiglinRenderState zombifiedPiglinRenderState) {
		return ZOMBIFIED_PIGLIN_LOCATION;
	}

	public ZombifiedPiglinRenderState createRenderState() {
		return new ZombifiedPiglinRenderState();
	}

	public void extractRenderState(ZombifiedPiglin zombifiedPiglin, ZombifiedPiglinRenderState zombifiedPiglinRenderState, float f) {
		super.extractRenderState(zombifiedPiglin, zombifiedPiglinRenderState, f);
		zombifiedPiglinRenderState.isAggressive = zombifiedPiglin.isAggressive();
	}
}
