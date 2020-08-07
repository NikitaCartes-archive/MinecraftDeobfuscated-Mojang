package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class LookAtTargetSink extends Behavior<Mob> {
	public LookAtTargetSink(int i, int j) {
		super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT), i, j);
	}

	protected boolean canStillUse(ServerLevel serverLevel, Mob mob, long l) {
		return mob.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).filter(positionTracker -> positionTracker.isVisibleBy(mob)).isPresent();
	}

	protected void stop(ServerLevel serverLevel, Mob mob, long l) {
		mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
	}

	protected void tick(ServerLevel serverLevel, Mob mob, long l) {
		mob.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).ifPresent(positionTracker -> mob.getLookControl().setLookAt(positionTracker.currentPosition()));
	}
}
