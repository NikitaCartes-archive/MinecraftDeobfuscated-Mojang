package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.GameRules;

public class StopBeingAngryIfTargetDead<E extends Mob> extends Behavior<E> {
	public StopBeingAngryIfTargetDead() {
		super(ImmutableMap.of(MemoryModuleType.ANGRY_AT, MemoryStatus.VALUE_PRESENT));
	}

	protected void start(ServerLevel serverLevel, E mob, long l) {
		BehaviorUtils.getLivingEntityFromUUIDMemory(mob, MemoryModuleType.ANGRY_AT)
			.ifPresent(
				livingEntity -> {
					if (livingEntity.isDeadOrDying()
						&& (livingEntity.getType() != EntityType.PLAYER || serverLevel.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS))) {
						mob.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
					}
				}
			);
	}
}
