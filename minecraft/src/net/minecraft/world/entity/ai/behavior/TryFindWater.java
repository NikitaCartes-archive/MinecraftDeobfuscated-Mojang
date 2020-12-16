package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class TryFindWater extends Behavior<PathfinderMob> {
	private final int range;
	private final float speedModifier;

	public TryFindWater(int i, float f) {
		super(
			ImmutableMap.of(
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED
			)
		);
		this.range = i;
		this.speedModifier = f;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
		return !pathfinderMob.level.getFluidState(pathfinderMob.blockPosition()).is(FluidTags.WATER);
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		BlockPos blockPos = null;

		for (BlockPos blockPos2 : BlockPos.withinManhattan(pathfinderMob.blockPosition(), this.range, this.range, this.range)) {
			if (pathfinderMob.level.getFluidState(blockPos2).is(FluidTags.WATER)) {
				blockPos = blockPos2.immutable();
				break;
			}
		}

		if (blockPos != null) {
			BehaviorUtils.setWalkAndLookTargetMemories(pathfinderMob, blockPos, this.speedModifier, 0);
		}
	}
}
