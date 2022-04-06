package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.phys.AABB;

public class NearestLivingEntitySensor<T extends LivingEntity> extends Sensor<T> {
	@Override
	protected void doTick(ServerLevel serverLevel, T livingEntity) {
		AABB aABB = livingEntity.getBoundingBox().inflate((double)this.radiusXZ(), (double)this.radiusY(), (double)this.radiusXZ());
		List<LivingEntity> list = serverLevel.getEntitiesOfClass(LivingEntity.class, aABB, livingEntity2 -> livingEntity2 != livingEntity && livingEntity2.isAlive());
		list.sort(Comparator.comparingDouble(livingEntity::distanceToSqr));
		Brain<?> brain = livingEntity.getBrain();
		brain.setMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES, list);
		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, new NearestVisibleLivingEntities(livingEntity, list));
	}

	protected int radiusXZ() {
		return 16;
	}

	protected int radiusY() {
		return 16;
	}

	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
	}
}
