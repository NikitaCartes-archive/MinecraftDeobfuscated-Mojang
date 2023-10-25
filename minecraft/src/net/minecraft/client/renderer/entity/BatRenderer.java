package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ambient.Bat;

@Environment(EnvType.CLIENT)
public class BatRenderer extends MobRenderer<Bat, BatModel> {
	private static final ResourceLocation BAT_LOCATION = new ResourceLocation("textures/entity/bat.png");

	public BatRenderer(EntityRendererProvider.Context context) {
		super(context, new BatModel(context.bakeLayer(ModelLayers.BAT)), 0.25F);
	}

	public ResourceLocation getTextureLocation(Bat bat) {
		return BAT_LOCATION;
	}
}
