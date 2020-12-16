package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableMap;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class VillagerHostilesSensor extends HostilesSensor {
	private static final ImmutableMap<EntityType<?>, Float> ACCEPTABLE_DISTANCE_FROM_HOSTILES = ImmutableMap.<EntityType<?>, Float>builder()
		.put(EntityType.DROWNED, 8.0F)
		.put(EntityType.EVOKER, 12.0F)
		.put(EntityType.HUSK, 8.0F)
		.put(EntityType.ILLUSIONER, 12.0F)
		.put(EntityType.PILLAGER, 15.0F)
		.put(EntityType.RAVAGER, 12.0F)
		.put(EntityType.VEX, 8.0F)
		.put(EntityType.VINDICATOR, 10.0F)
		.put(EntityType.ZOGLIN, 10.0F)
		.put(EntityType.ZOMBIE, 8.0F)
		.put(EntityType.ZOMBIE_VILLAGER, 8.0F)
		.build();

	@Override
	protected Optional<LivingEntity> getNearestHostile(LivingEntity livingEntity) {
		return this.getVisibleEntities(livingEntity)
			.flatMap(
				list -> list.stream()
						.filter(this::isHostile)
						.filter(livingEntity2 -> this.isClose(livingEntity, livingEntity2))
						.min(Comparator.comparingDouble(livingEntity::distanceToSqr))
			);
	}

	@Override
	protected boolean isClose(LivingEntity livingEntity, LivingEntity livingEntity2) {
		float f = ACCEPTABLE_DISTANCE_FROM_HOSTILES.get(livingEntity2.getType());
		return livingEntity2.distanceToSqr(livingEntity) <= (double)(f * f);
	}

	private boolean isHostile(LivingEntity livingEntity) {
		return ACCEPTABLE_DISTANCE_FROM_HOSTILES.containsKey(livingEntity.getType());
	}
}
