package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SheepFurLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;

@Environment(EnvType.CLIENT)
public class SheepRenderer extends MobRenderer<Sheep, SheepModel<Sheep>> {
	private static final ResourceLocation SHEEP_LOCATION = new ResourceLocation("textures/entity/sheep/sheep.png");

	public SheepRenderer(EntityRendererProvider.Context context) {
		super(context, new SheepModel<>(context.bakeLayer(ModelLayers.SHEEP)), 0.7F);
		this.addLayer(new SheepFurLayer(this, context.getModelSet()));
	}

	public ResourceLocation getTextureLocation(Sheep sheep) {
		return SHEEP_LOCATION;
	}
}
