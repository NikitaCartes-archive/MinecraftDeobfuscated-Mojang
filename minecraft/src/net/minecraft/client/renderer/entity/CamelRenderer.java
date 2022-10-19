package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CamelModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.camel.Camel;

@Environment(EnvType.CLIENT)
public class CamelRenderer extends MobRenderer<Camel, CamelModel<Camel>> {
	private static final ResourceLocation CAMEL_LOCATION = new ResourceLocation("textures/entity/camel/camel.png");

	public CamelRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation) {
		super(context, new CamelModel<>(context.bakeLayer(modelLayerLocation)), 0.7F);
	}

	public ResourceLocation getTextureLocation(Camel camel) {
		return CAMEL_LOCATION;
	}
}
