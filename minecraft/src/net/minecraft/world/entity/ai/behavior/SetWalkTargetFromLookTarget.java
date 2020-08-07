package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromLookTarget extends Behavior<LivingEntity> {
	private final float speedModifier;
	private final int closeEnoughDistance;

	public SetWalkTargetFromLookTarget(float f, int i) {
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT));
		this.speedModifier = f;
		this.closeEnoughDistance = i;
	}

	@Override
	protected void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		Brain<?> brain = livingEntity.getBrain();
		PositionTracker positionTracker = (PositionTracker)brain.getMemory(MemoryModuleType.LOOK_TARGET).get();
		brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(positionTracker, this.speedModifier, this.closeEnoughDistance));
	}
}
