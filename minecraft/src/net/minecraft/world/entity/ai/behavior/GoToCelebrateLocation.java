package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class GoToCelebrateLocation<E extends Mob> extends Behavior<E> {
	private final int closeEnoughDist;

	public GoToCelebrateLocation(int i) {
		super(
			ImmutableMap.of(
				MemoryModuleType.CELEBRATE_LOCATION,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED
			)
		);
		this.closeEnoughDist = i;
	}

	protected void start(ServerLevel serverLevel, Mob mob, long l) {
		BlockPos blockPos = getCelebrateLocation(mob);
		boolean bl = blockPos.closerThan(mob.getBlockPos(), (double)this.closeEnoughDist);
		if (!bl) {
			BehaviorUtils.setWalkAndLookTargetMemories(mob, getNearbyPos(mob, blockPos), this.closeEnoughDist);
		}
	}

	private static BlockPos getNearbyPos(Mob mob, BlockPos blockPos) {
		Random random = mob.level.random;
		return blockPos.offset(getRandomOffset(random), 0, getRandomOffset(random));
	}

	private static int getRandomOffset(Random random) {
		return random.nextInt(3) - 1;
	}

	private static BlockPos getCelebrateLocation(Mob mob) {
		return (BlockPos)mob.getBrain().getMemory(MemoryModuleType.CELEBRATE_LOCATION).get();
	}
}
