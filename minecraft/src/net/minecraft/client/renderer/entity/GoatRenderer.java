package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.GoatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.goat.Goat;

@Environment(EnvType.CLIENT)
public class GoatRenderer extends MobRenderer<Goat, GoatModel<Goat>> {
	private static final ResourceLocation GOAT_LOCATION = new ResourceLocation("textures/entity/goat/goat.png");

	public GoatRenderer(EntityRendererProvider.Context context) {
		super(context, new GoatModel<>(context.bakeLayer(ModelLayers.GOAT)), 0.7F);
		this.addLayer(new CustomHeadLayer<>(this, context.getModelSet()));
	}

	public ResourceLocation getTextureLocation(Goat goat) {
		return GOAT_LOCATION;
	}
}
