package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class HuskRenderer extends ZombieRenderer {
	private static final ResourceLocation HUSK_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/husk.png");

	public HuskRenderer(EntityRendererProvider.Context context) {
		super(
			context,
			ModelLayers.HUSK,
			ModelLayers.HUSK_BABY,
			ModelLayers.HUSK_INNER_ARMOR,
			ModelLayers.HUSK_OUTER_ARMOR,
			ModelLayers.HUSK_BABY_INNER_ARMOR,
			ModelLayers.HUSK_BABY_OUTER_ARMOR
		);
	}

	@Override
	public ResourceLocation getTextureLocation(ZombieRenderState zombieRenderState) {
		return HUSK_LOCATION;
	}
}
