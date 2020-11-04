package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.IntRange;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class BabyFollowAdult<E extends AgeableMob> extends Behavior<E> {
	private final IntRange followRange;
	private final float speedModifier;

	public BabyFollowAdult(IntRange intRange, float f) {
		super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
		this.followRange = intRange;
		this.speedModifier = f;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E ageableMob) {
		if (!ageableMob.isBaby()) {
			return false;
		} else {
			AgeableMob ageableMob2 = this.getNearestAdult(ageableMob);
			return ageableMob.closerThan(ageableMob2, (double)(this.followRange.getMaxInclusive() + 1))
				&& !ageableMob.closerThan(ageableMob2, (double)this.followRange.getMinInclusive());
		}
	}

	protected void start(ServerLevel serverLevel, E ageableMob, long l) {
		BehaviorUtils.setWalkAndLookTargetMemories(ageableMob, this.getNearestAdult(ageableMob), this.speedModifier, this.followRange.getMinInclusive() - 1);
	}

	private AgeableMob getNearestAdult(E ageableMob) {
		return (AgeableMob)ageableMob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT).get();
	}
}
