package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

@Environment(EnvType.CLIENT)
public class MinecartRenderer extends AbstractMinecartRenderer<AbstractMinecart, MinecartRenderState> {
	public MinecartRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation) {
		super(context, modelLayerLocation);
	}

	public MinecartRenderState createRenderState() {
		return new MinecartRenderState();
	}
}
