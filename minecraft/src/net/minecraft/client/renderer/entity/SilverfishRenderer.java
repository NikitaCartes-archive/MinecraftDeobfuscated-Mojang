package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Silverfish;

@Environment(EnvType.CLIENT)
public class SilverfishRenderer extends MobRenderer<Silverfish, SilverfishModel<Silverfish>> {
	private static final ResourceLocation SILVERFISH_LOCATION = new ResourceLocation("textures/entity/silverfish.png");

	public SilverfishRenderer(EntityRendererProvider.Context context) {
		super(context, new SilverfishModel<>(context.getLayer(ModelLayers.SILVERFISH)), 0.3F);
	}

	protected float getFlipDegrees(Silverfish silverfish) {
		return 180.0F;
	}

	public ResourceLocation getTextureLocation(Silverfish silverfish) {
		return SILVERFISH_LOCATION;
	}
}
