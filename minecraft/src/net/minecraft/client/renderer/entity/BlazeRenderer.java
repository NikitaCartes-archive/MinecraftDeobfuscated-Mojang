package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Blaze;

@Environment(EnvType.CLIENT)
public class BlazeRenderer extends MobRenderer<Blaze, LivingEntityRenderState, BlazeModel> {
	private static final ResourceLocation BLAZE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/blaze.png");

	public BlazeRenderer(EntityRendererProvider.Context context) {
		super(context, new BlazeModel(context.bakeLayer(ModelLayers.BLAZE)), 0.5F);
	}

	protected int getBlockLightLevel(Blaze blaze, BlockPos blockPos) {
		return 15;
	}

	@Override
	public ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
		return BLAZE_LOCATION;
	}

	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}
}
