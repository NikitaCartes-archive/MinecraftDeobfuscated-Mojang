package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SnowGolemHeadLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.SnowGolem;

@Environment(EnvType.CLIENT)
public class SnowGolemRenderer extends MobRenderer<SnowGolem, SnowGolemModel<SnowGolem>> {
	private static final ResourceLocation SNOW_GOLEM_LOCATION = new ResourceLocation("textures/entity/snow_golem.png");

	public SnowGolemRenderer(EntityRendererProvider.Context context) {
		super(context, new SnowGolemModel<>(context.bakeLayer(ModelLayers.SNOW_GOLEM)), 0.5F);
		this.addLayer(new SnowGolemHeadLayer(this));
	}

	public ResourceLocation getTextureLocation(SnowGolem snowGolem) {
		return SNOW_GOLEM_LOCATION;
	}
}
