package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StopBeingAngryIfTargetDead<E extends Mob> extends Behavior<E> {
	public StopBeingAngryIfTargetDead() {
		super(ImmutableMap.of(MemoryModuleType.ANGRY_AT, MemoryStatus.VALUE_PRESENT));
	}

	protected void start(ServerLevel serverLevel, E mob, long l) {
		if (this.isCurrentTargetDeadOrRemoved(mob)) {
			mob.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
		}
	}

	private boolean isCurrentTargetDeadOrRemoved(E mob) {
		Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(mob, MemoryModuleType.ANGRY_AT);
		return !optional.isPresent() || !((LivingEntity)optional.get()).isAlive();
	}
}
