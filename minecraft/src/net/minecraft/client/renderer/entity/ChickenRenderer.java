package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Chicken;

@Environment(EnvType.CLIENT)
public class ChickenRenderer extends AgeableMobRenderer<Chicken, ChickenRenderState, ChickenModel> {
	private static final ResourceLocation CHICKEN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/chicken.png");

	public ChickenRenderer(EntityRendererProvider.Context context) {
		super(context, new ChickenModel(context.bakeLayer(ModelLayers.CHICKEN)), new ChickenModel(context.bakeLayer(ModelLayers.CHICKEN_BABY)), 0.3F);
	}

	public ResourceLocation getTextureLocation(ChickenRenderState chickenRenderState) {
		return CHICKEN_LOCATION;
	}

	public ChickenRenderState createRenderState() {
		return new ChickenRenderState();
	}

	public void extractRenderState(Chicken chicken, ChickenRenderState chickenRenderState, float f) {
		super.extractRenderState(chicken, chickenRenderState, f);
		chickenRenderState.flap = Mth.lerp(f, chicken.oFlap, chicken.flap);
		chickenRenderState.flapSpeed = Mth.lerp(f, chicken.oFlapSpeed, chicken.flapSpeed);
	}
}
