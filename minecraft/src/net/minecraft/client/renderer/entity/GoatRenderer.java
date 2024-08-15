package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.GoatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.GoatRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.goat.Goat;

@Environment(EnvType.CLIENT)
public class GoatRenderer extends AgeableMobRenderer<Goat, GoatRenderState, GoatModel> {
	private static final ResourceLocation GOAT_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/goat/goat.png");

	public GoatRenderer(EntityRendererProvider.Context context) {
		super(context, new GoatModel(context.bakeLayer(ModelLayers.GOAT)), new GoatModel(context.bakeLayer(ModelLayers.GOAT_BABY)), 0.7F);
	}

	public ResourceLocation getTextureLocation(GoatRenderState goatRenderState) {
		return GOAT_LOCATION;
	}

	public GoatRenderState createRenderState() {
		return new GoatRenderState();
	}

	public void extractRenderState(Goat goat, GoatRenderState goatRenderState, float f) {
		super.extractRenderState(goat, goatRenderState, f);
		goatRenderState.hasLeftHorn = goat.hasLeftHorn();
		goatRenderState.hasRightHorn = goat.hasRightHorn();
		goatRenderState.rammingXHeadRot = goat.getRammingXHeadRot();
	}
}
