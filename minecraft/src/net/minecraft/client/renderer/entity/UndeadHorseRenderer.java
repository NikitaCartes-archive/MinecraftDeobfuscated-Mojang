package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

@Environment(EnvType.CLIENT)
public class UndeadHorseRenderer extends AbstractHorseRenderer<AbstractHorse, HorseModel<AbstractHorse>> {
	private static final Map<EntityType<?>, ResourceLocation> MAP = Maps.<EntityType<?>, ResourceLocation>newHashMap(
		ImmutableMap.of(
			EntityType.ZOMBIE_HORSE,
			new ResourceLocation("textures/entity/horse/horse_zombie.png"),
			EntityType.SKELETON_HORSE,
			new ResourceLocation("textures/entity/horse/horse_skeleton.png")
		)
	);

	public UndeadHorseRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation) {
		super(context, new HorseModel<>(context.bakeLayer(modelLayerLocation)), 1.0F);
	}

	public ResourceLocation getTextureLocation(AbstractHorse abstractHorse) {
		return (ResourceLocation)MAP.get(abstractHorse.getType());
	}
}
