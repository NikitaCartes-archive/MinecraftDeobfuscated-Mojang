package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class NearestLivingEntitySensor extends Sensor<LivingEntity> {
	private static final TargetingConditions TARGETING = new TargetingConditions().range(16.0).allowSameTeam().allowNonAttackable().allowUnseeable();

	@Override
	protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
		List<LivingEntity> list = serverLevel.getEntitiesOfClass(
			LivingEntity.class, livingEntity.getBoundingBox().inflate(16.0, 16.0, 16.0), livingEntity2 -> livingEntity2 != livingEntity && livingEntity2.isAlive()
		);
		list.sort(Comparator.comparingDouble(livingEntity::distanceToSqr));
		Brain<?> brain = livingEntity.getBrain();
		brain.setMemory(MemoryModuleType.LIVING_ENTITIES, list);
		brain.setMemory(
			MemoryModuleType.VISIBLE_LIVING_ENTITIES,
			(List<LivingEntity>)list.stream()
				.filter(livingEntity2 -> TARGETING.test(livingEntity, livingEntity2))
				.filter(livingEntity::canSee)
				.collect(Collectors.toList())
		);
	}

	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(MemoryModuleType.LIVING_ENTITIES, MemoryModuleType.VISIBLE_LIVING_ENTITIES);
	}
}
