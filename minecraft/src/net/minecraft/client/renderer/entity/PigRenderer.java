package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;

@Environment(EnvType.CLIENT)
public class PigRenderer extends MobRenderer<Pig, PigModel<Pig>> {
	private static final ResourceLocation PIG_LOCATION = new ResourceLocation("textures/entity/pig/pig.png");

	public PigRenderer(EntityRendererProvider.Context context) {
		super(context, new PigModel<>(context.bakeLayer(ModelLayers.PIG)), 0.7F);
		this.addLayer(new SaddleLayer<>(this, new PigModel<>(context.bakeLayer(ModelLayers.PIG_SADDLE)), new ResourceLocation("textures/entity/pig/pig_saddle.png")));
	}

	public ResourceLocation getTextureLocation(Pig pig) {
		return PIG_LOCATION;
	}
}
