package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;

public class WardenEntitySensor extends Sensor<LivingEntity> {
	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_ATTACKABLE);
	}

	@Override
	protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
		livingEntity.getBrain()
			.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
			.flatMap(
				nearestVisibleLivingEntities -> nearestVisibleLivingEntities.findClosest(
						livingEntityx -> Warden.canTargetEntity(livingEntityx) && livingEntityx.getType() == EntityType.PLAYER,
						livingEntityx -> Warden.canTargetEntity(livingEntityx) && livingEntityx.getType() != EntityType.PLAYER
					)
			)
			.ifPresent(livingEntity2 -> livingEntity.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, livingEntity2));
	}
}
