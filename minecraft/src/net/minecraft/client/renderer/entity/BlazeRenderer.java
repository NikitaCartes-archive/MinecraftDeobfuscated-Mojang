package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Blaze;

@Environment(EnvType.CLIENT)
public class BlazeRenderer extends MobRenderer<Blaze, BlazeModel<Blaze>> {
	private static final ResourceLocation BLAZE_LOCATION = new ResourceLocation("textures/entity/blaze.png");

	public BlazeRenderer(EntityRendererProvider.Context context) {
		super(context, new BlazeModel<>(context.getLayer(ModelLayers.BLAZE)), 0.5F);
	}

	protected int getBlockLightLevel(Blaze blaze, BlockPos blockPos) {
		return 15;
	}

	public ResourceLocation getTextureLocation(Blaze blaze) {
		return BLAZE_LOCATION;
	}
}
