package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class Mount<E extends LivingEntity> extends Behavior<E> {
	public Mount() {
		super(
			ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.RIDE_TARGET,
				MemoryStatus.VALUE_PRESENT
			)
		);
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E livingEntity) {
		return !livingEntity.isPassenger();
	}

	@Override
	protected void start(ServerLevel serverLevel, E livingEntity, long l) {
		if (this.isCloseEnoughToStartRiding(livingEntity)) {
			livingEntity.startRiding(this.getRidableEntity(livingEntity));
		} else {
			BehaviorUtils.setWalkAndLookTargetMemories(livingEntity, this.getRidableEntity(livingEntity), 1);
		}
	}

	private boolean isCloseEnoughToStartRiding(E livingEntity) {
		return this.getRidableEntity(livingEntity).closerThan(livingEntity, 1.0);
	}

	private Entity getRidableEntity(E livingEntity) {
		return (Entity)livingEntity.getBrain().getMemory(MemoryModuleType.RIDE_TARGET).get();
	}
}
