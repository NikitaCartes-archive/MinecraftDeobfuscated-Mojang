package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Ocelot;

@Environment(EnvType.CLIENT)
public class OcelotRenderer extends MobRenderer<Ocelot, OcelotModel<Ocelot>> {
	private static final ResourceLocation CAT_OCELOT_LOCATION = new ResourceLocation("textures/entity/cat/ocelot.png");

	public OcelotRenderer(EntityRendererProvider.Context context) {
		super(context, new OcelotModel<>(context.bakeLayer(ModelLayers.OCELOT)), 0.4F);
	}

	public ResourceLocation getTextureLocation(Ocelot ocelot) {
		return CAT_OCELOT_LOCATION;
	}
}
