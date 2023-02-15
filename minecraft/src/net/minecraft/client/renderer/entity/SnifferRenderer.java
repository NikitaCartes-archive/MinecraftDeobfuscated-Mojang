package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SnifferModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.sniffer.Sniffer;

@Environment(EnvType.CLIENT)
public class SnifferRenderer extends MobRenderer<Sniffer, SnifferModel<Sniffer>> {
	private static final ResourceLocation SNIFFER_LOCATION = new ResourceLocation("textures/entity/sniffer/sniffer.png");

	public SnifferRenderer(EntityRendererProvider.Context context) {
		super(context, new SnifferModel<>(context.bakeLayer(ModelLayers.SNIFFER)), 1.1F);
	}

	public ResourceLocation getTextureLocation(Sniffer sniffer) {
		return SNIFFER_LOCATION;
	}
}
