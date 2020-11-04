package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EndermiteModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Endermite;

@Environment(EnvType.CLIENT)
public class EndermiteRenderer extends MobRenderer<Endermite, EndermiteModel<Endermite>> {
	private static final ResourceLocation ENDERMITE_LOCATION = new ResourceLocation("textures/entity/endermite.png");

	public EndermiteRenderer(EntityRendererProvider.Context context) {
		super(context, new EndermiteModel<>(context.getLayer(ModelLayers.ENDERMITE)), 0.3F);
	}

	protected float getFlipDegrees(Endermite endermite) {
		return 180.0F;
	}

	public ResourceLocation getTextureLocation(Endermite endermite) {
		return ENDERMITE_LOCATION;
	}
}
