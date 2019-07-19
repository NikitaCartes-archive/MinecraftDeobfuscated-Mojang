package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HorseModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;

@Environment(EnvType.CLIENT)
public class UndeadHorseRenderer extends AbstractHorseRenderer<AbstractHorse, HorseModel<AbstractHorse>> {
	private static final Map<Class<?>, ResourceLocation> MAP = Maps.<Class<?>, ResourceLocation>newHashMap(
		ImmutableMap.of(
			ZombieHorse.class,
			new ResourceLocation("textures/entity/horse/horse_zombie.png"),
			SkeletonHorse.class,
			new ResourceLocation("textures/entity/horse/horse_skeleton.png")
		)
	);

	public UndeadHorseRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new HorseModel<>(0.0F), 1.0F);
	}

	protected ResourceLocation getTextureLocation(AbstractHorse abstractHorse) {
		return (ResourceLocation)MAP.get(abstractHorse.getClass());
	}
}
