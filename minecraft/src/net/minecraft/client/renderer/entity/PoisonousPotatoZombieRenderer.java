package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PoisonousPotatoZombieModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

@Environment(EnvType.CLIENT)
public class PoisonousPotatoZombieRenderer extends AbstractZombieRenderer<Zombie, PoisonousPotatoZombieModel<Zombie>> {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/zombie/poisonous_potato_zombie.png");

	public PoisonousPotatoZombieRenderer(EntityRendererProvider.Context context) {
		this(context, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_INNER_ARMOR, ModelLayers.ZOMBIE_OUTER_ARMOR);
	}

	@Override
	public ResourceLocation getTextureLocation(Zombie zombie) {
		return TEXTURE_LOCATION;
	}

	public PoisonousPotatoZombieRenderer(
		EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, ModelLayerLocation modelLayerLocation3
	) {
		super(
			context,
			new PoisonousPotatoZombieModel<>(context.bakeLayer(modelLayerLocation)),
			new PoisonousPotatoZombieModel<>(context.bakeLayer(modelLayerLocation2)),
			new PoisonousPotatoZombieModel<>(context.bakeLayer(modelLayerLocation3))
		);
	}
}
