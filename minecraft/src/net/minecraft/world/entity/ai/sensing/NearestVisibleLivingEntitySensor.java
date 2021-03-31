package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public abstract class NearestVisibleLivingEntitySensor extends Sensor<LivingEntity> {
	protected abstract boolean isMatchingEntity(LivingEntity livingEntity, LivingEntity livingEntity2);

	protected abstract MemoryModuleType<LivingEntity> getMemory();

	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(this.getMemory());
	}

	@Override
	protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
		livingEntity.getBrain().setMemory(this.getMemory(), this.getNearestEntity(livingEntity));
	}

	private Optional<LivingEntity> getNearestEntity(LivingEntity livingEntity) {
		return this.getVisibleEntities(livingEntity)
			.flatMap(
				list -> list.stream()
						.filter(livingEntity2 -> this.isMatchingEntity(livingEntity, livingEntity2))
						.min(Comparator.comparingDouble(livingEntity::distanceToSqr))
			);
	}

	protected Optional<List<LivingEntity>> getVisibleEntities(LivingEntity livingEntity) {
		return livingEntity.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES);
	}
}
