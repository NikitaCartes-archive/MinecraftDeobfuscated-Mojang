package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.IntRange;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class BabyFollowAdult<E extends AgableMob> extends Behavior<E> {
	private final IntRange followRange;
	private final float speedModifier;

	public BabyFollowAdult(IntRange intRange, float f) {
		super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
		this.followRange = intRange;
		this.speedModifier = f;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E agableMob) {
		if (!agableMob.isBaby()) {
			return false;
		} else {
			AgableMob agableMob2 = this.getNearestAdult(agableMob);
			return agableMob.closerThan(agableMob2, (double)(this.followRange.getMaxInclusive() + 1))
				&& !agableMob.closerThan(agableMob2, (double)this.followRange.getMinInclusive());
		}
	}

	protected void start(ServerLevel serverLevel, E agableMob, long l) {
		BehaviorUtils.setWalkAndLookTargetMemories(agableMob, this.getNearestAdult(agableMob), this.speedModifier, this.followRange.getMinInclusive() - 1);
	}

	private AgableMob getNearestAdult(E agableMob) {
		return (AgableMob)agableMob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT).get();
	}
}
