package net.minecraft.client.renderer.entity.layers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BeeStingerModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class BeeStingerLayer<M extends PlayerModel> extends StuckInBodyLayer<M> {
	private static final ResourceLocation BEE_STINGER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee_stinger.png");

	public BeeStingerLayer(LivingEntityRenderer<?, PlayerRenderState, M> livingEntityRenderer, EntityRendererProvider.Context context) {
		super(livingEntityRenderer, new BeeStingerModel(context.bakeLayer(ModelLayers.BEE_STINGER)), BEE_STINGER_LOCATION, StuckInBodyLayer.PlacementStyle.ON_SURFACE);
	}

	@Override
	protected int numStuck(PlayerRenderState playerRenderState) {
		return playerRenderState.stingerCount;
	}
}
