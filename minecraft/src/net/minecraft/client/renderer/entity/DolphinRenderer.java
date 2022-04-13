package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.DolphinCarryingItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Dolphin;

@Environment(EnvType.CLIENT)
public class DolphinRenderer extends MobRenderer<Dolphin, DolphinModel<Dolphin>> {
	private static final ResourceLocation DOLPHIN_LOCATION = new ResourceLocation("textures/entity/dolphin.png");

	public DolphinRenderer(EntityRendererProvider.Context context) {
		super(context, new DolphinModel<>(context.bakeLayer(ModelLayers.DOLPHIN)), 0.7F);
		this.addLayer(new DolphinCarryingItemLayer(this, context.getItemInHandRenderer()));
	}

	public ResourceLocation getTextureLocation(Dolphin dolphin) {
		return DOLPHIN_LOCATION;
	}
}
