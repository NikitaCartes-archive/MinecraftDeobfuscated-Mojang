package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;

@Environment(EnvType.CLIENT)
public class PigRenderer extends AgeableMobRenderer<Pig, PigRenderState, PigModel> {
	private static final ResourceLocation PIG_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/pig/pig.png");

	public PigRenderer(EntityRendererProvider.Context context) {
		super(context, new PigModel(context.bakeLayer(ModelLayers.PIG)), new PigModel(context.bakeLayer(ModelLayers.PIG_BABY)), 0.7F);
		this.addLayer(
			new SaddleLayer<>(
				this,
				new PigModel(context.bakeLayer(ModelLayers.PIG_SADDLE)),
				new PigModel(context.bakeLayer(ModelLayers.PIG_BABY_SADDLE)),
				ResourceLocation.withDefaultNamespace("textures/entity/pig/pig_saddle.png")
			)
		);
	}

	public ResourceLocation getTextureLocation(PigRenderState pigRenderState) {
		return PIG_LOCATION;
	}

	public PigRenderState createRenderState() {
		return new PigRenderState();
	}

	public void extractRenderState(Pig pig, PigRenderState pigRenderState, float f) {
		super.extractRenderState(pig, pigRenderState, f);
		pigRenderState.isSaddled = pig.isSaddled();
	}
}
