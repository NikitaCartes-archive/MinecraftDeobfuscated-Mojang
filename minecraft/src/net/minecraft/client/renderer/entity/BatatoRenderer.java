package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BatatoModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ambient.Batato;

@Environment(EnvType.CLIENT)
public class BatatoRenderer extends MobRenderer<Batato, BatatoModel> {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/batato.png");

	public BatatoRenderer(EntityRendererProvider.Context context) {
		super(context, new BatatoModel(context.bakeLayer(ModelLayers.BATATO)), 0.25F);
	}

	public ResourceLocation getTextureLocation(Batato batato) {
		return TEXTURE_LOCATION;
	}
}
