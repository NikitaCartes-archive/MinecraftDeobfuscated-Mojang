package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.BeretLayer;
import net.minecraft.client.renderer.entity.layers.MustacheLayer;
import net.minecraft.client.renderer.entity.layers.TailLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.RayTracing;

@Environment(EnvType.CLIENT)
public class RayTracingRenderer extends LivingEntityRenderer<RayTracing, PlayerModel<RayTracing>> {
	private static final ResourceLocation RAY_LOCATION = new ResourceLocation("textures/entity/player/wide/ray.png");

	public RayTracingRenderer(EntityRendererProvider.Context context) {
		super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
		this.addLayer(new BeretLayer<>(this, context.getModelSet()));
		this.addLayer(new MustacheLayer<>(this, context.getModelSet()));
		this.addLayer(new TailLayer<>(this, context.getModelSet()));
	}

	public ResourceLocation getTextureLocation(RayTracing rayTracing) {
		return RAY_LOCATION;
	}
}
