package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SheepWoolLayer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;

@Environment(EnvType.CLIENT)
public class SheepRenderer extends AgeableMobRenderer<Sheep, SheepRenderState, SheepModel> {
	private static final ResourceLocation SHEEP_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/sheep/sheep.png");

	public SheepRenderer(EntityRendererProvider.Context context) {
		super(context, new SheepModel(context.bakeLayer(ModelLayers.SHEEP)), new SheepModel(context.bakeLayer(ModelLayers.SHEEP_BABY)), 0.7F);
		this.addLayer(new SheepWoolLayer(this, context.getModelSet()));
	}

	public ResourceLocation getTextureLocation(SheepRenderState sheepRenderState) {
		return SHEEP_LOCATION;
	}

	public SheepRenderState createRenderState() {
		return new SheepRenderState();
	}

	public void extractRenderState(Sheep sheep, SheepRenderState sheepRenderState, float f) {
		super.extractRenderState(sheep, sheepRenderState, f);
		sheepRenderState.headEatAngleScale = sheep.getHeadEatAngleScale(f);
		sheepRenderState.headEatPositionScale = sheep.getHeadEatPositionScale(f);
		sheepRenderState.isSheared = sheep.isSheared();
		sheepRenderState.woolColor = sheep.getColor();
		sheepRenderState.id = sheep.getId();
	}
}
