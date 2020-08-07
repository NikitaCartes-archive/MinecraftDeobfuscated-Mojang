package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class VillagerHostilesSensor extends Sensor<LivingEntity> {
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
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(MemoryModuleType.NEAREST_HOSTILE);
	}

	@Override
	protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
		livingEntity.getBrain().setMemory(MemoryModuleType.NEAREST_HOSTILE, this.getNearestHostile(livingEntity));
	}

	private Optional<LivingEntity> getNearestHostile(LivingEntity livingEntity) {
		return this.getVisibleEntities(livingEntity)
			.flatMap(
				list -> list.stream()
						.filter(this::isHostile)
						.filter(livingEntity2 -> this.isClose(livingEntity, livingEntity2))
						.min((livingEntity2, livingEntity3) -> this.compareMobDistance(livingEntity, livingEntity2, livingEntity3))
			);
	}

	private Optional<List<LivingEntity>> getVisibleEntities(LivingEntity livingEntity) {
		return livingEntity.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES);
	}

	private int compareMobDistance(LivingEntity livingEntity, LivingEntity livingEntity2, LivingEntity livingEntity3) {
		return Mth.floor(livingEntity2.distanceToSqr(livingEntity) - livingEntity3.distanceToSqr(livingEntity));
	}

	private boolean isClose(LivingEntity livingEntity, LivingEntity livingEntity2) {
		float f = ACCEPTABLE_DISTANCE_FROM_HOSTILES.get(livingEntity2.getType());
		return livingEntity2.distanceToSqr(livingEntity) <= (double)(f * f);
	}

	private boolean isHostile(LivingEntity livingEntity) {
		return ACCEPTABLE_DISTANCE_FROM_HOSTILES.containsKey(livingEntity.getType());
	}
}
