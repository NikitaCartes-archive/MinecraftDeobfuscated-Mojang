package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StayCloseToTarget<E extends LivingEntity> extends Behavior<E> {
	private final Function<LivingEntity, Optional<PositionTracker>> targetPositionGetter;
	private final int closeEnough;
	private final int tooFar;
	private final float speedModifier;

	public StayCloseToTarget(Function<LivingEntity, Optional<PositionTracker>> function, int i, int j, float f) {
		super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
		this.targetPositionGetter = function;
		this.closeEnough = i;
		this.tooFar = j;
		this.speedModifier = f;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E livingEntity) {
		Optional<PositionTracker> optional = (Optional<PositionTracker>)this.targetPositionGetter.apply(livingEntity);
		if (optional.isEmpty()) {
			return false;
		} else {
			PositionTracker positionTracker = (PositionTracker)optional.get();
			return positionTracker.isVisibleBy(livingEntity) && !livingEntity.position().closerThan(positionTracker.currentPosition(), (double)this.tooFar);
		}
	}

	@Override
	protected void start(ServerLevel serverLevel, E livingEntity, long l) {
		BehaviorUtils.setWalkAndLookTargetMemories(
			livingEntity, (PositionTracker)((Optional)this.targetPositionGetter.apply(livingEntity)).get(), this.speedModifier, this.closeEnough
		);
	}
}
