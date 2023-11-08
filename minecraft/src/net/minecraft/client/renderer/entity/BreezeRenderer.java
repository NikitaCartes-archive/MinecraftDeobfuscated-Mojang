package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BreezeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.BreezeEyesLayer;
import net.minecraft.client.renderer.entity.layers.BreezeWindLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.breeze.Breeze;

@Environment(EnvType.CLIENT)
public class BreezeRenderer extends MobRenderer<Breeze, BreezeModel<Breeze>> {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/breeze/breeze.png");
	private static final ResourceLocation WIND_TEXTURE_LOCATION = new ResourceLocation("textures/entity/breeze/breeze_wind.png");

	public BreezeRenderer(EntityRendererProvider.Context context) {
		super(context, new BreezeModel<>(context.bakeLayer(ModelLayers.BREEZE)), 0.8F);
		this.addLayer(new BreezeWindLayer(this, context.getModelSet(), WIND_TEXTURE_LOCATION));
		this.addLayer(new BreezeEyesLayer(this, context.getModelSet(), TEXTURE_LOCATION));
	}

	public ResourceLocation getTextureLocation(Breeze breeze) {
		return TEXTURE_LOCATION;
	}
}
