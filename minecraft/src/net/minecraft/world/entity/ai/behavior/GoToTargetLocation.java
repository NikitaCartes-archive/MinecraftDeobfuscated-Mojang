package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class GoToTargetLocation<E extends Mob> extends Behavior<E> {
	private final MemoryModuleType<BlockPos> locationMemory;
	private final int closeEnoughDist;
	private final float speedModifier;

	public GoToTargetLocation(MemoryModuleType<BlockPos> memoryModuleType, int i, float f) {
		super(
			ImmutableMap.of(
				memoryModuleType,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED
			)
		);
		this.locationMemory = memoryModuleType;
		this.closeEnoughDist = i;
		this.speedModifier = f;
	}

	protected void start(ServerLevel serverLevel, Mob mob, long l) {
		BlockPos blockPos = this.getTargetLocation(mob);
		boolean bl = blockPos.closerThan(mob.blockPosition(), (double)this.closeEnoughDist);
		if (!bl) {
			BehaviorUtils.setWalkAndLookTargetMemories(mob, getNearbyPos(mob, blockPos), this.speedModifier, this.closeEnoughDist);
		}
	}

	private static BlockPos getNearbyPos(Mob mob, BlockPos blockPos) {
		RandomSource randomSource = mob.level.random;
		return blockPos.offset(getRandomOffset(randomSource), 0, getRandomOffset(randomSource));
	}

	private static int getRandomOffset(RandomSource randomSource) {
		return randomSource.nextInt(3) - 1;
	}

	private BlockPos getTargetLocation(Mob mob) {
		return (BlockPos)mob.getBrain().getMemory(this.locationMemory).get();
	}
}
