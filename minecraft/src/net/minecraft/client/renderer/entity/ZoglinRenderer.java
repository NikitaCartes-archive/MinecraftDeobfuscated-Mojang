package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zoglin;

@Environment(EnvType.CLIENT)
public class ZoglinRenderer extends AbstractHoglinRenderer<Zoglin> {
	private static final ResourceLocation ZOGLIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/hoglin/zoglin.png");

	public ZoglinRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.ZOGLIN, ModelLayers.ZOGLIN_BABY, 0.7F);
	}

	public ResourceLocation getTextureLocation(HoglinRenderState hoglinRenderState) {
		return ZOGLIN_LOCATION;
	}
}
