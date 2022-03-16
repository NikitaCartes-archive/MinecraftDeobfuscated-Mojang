package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class TryFindLand extends Behavior<PathfinderMob> {
	private static final int COOLDOWN_TICKS = 60;
	private final int range;
	private final float speedModifier;
	private long nextOkStartTime;

	public TryFindLand(int i, float f) {
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

	protected void stop(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		this.nextOkStartTime = l + 60L;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
		return pathfinderMob.level.getFluidState(pathfinderMob.blockPosition()).is(FluidTags.WATER);
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		if (l >= this.nextOkStartTime) {
			BlockPos blockPos = pathfinderMob.blockPosition();
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			CollisionContext collisionContext = CollisionContext.of(pathfinderMob);

			for (BlockPos blockPos2 : BlockPos.withinManhattan(blockPos, this.range, this.range, this.range)) {
				if (blockPos2.getX() != blockPos.getX() || blockPos2.getZ() != blockPos.getZ()) {
					BlockState blockState = serverLevel.getBlockState(blockPos2);
					BlockState blockState2 = serverLevel.getBlockState(mutableBlockPos.setWithOffset(blockPos2, Direction.DOWN));
					if (!blockState.is(Blocks.WATER)
						&& serverLevel.getFluidState(blockPos2).isEmpty()
						&& blockState.getCollisionShape(serverLevel, blockPos2, collisionContext).isEmpty()
						&& blockState2.isFaceSturdy(serverLevel, mutableBlockPos, Direction.UP)) {
						this.nextOkStartTime = l + 60L;
						BehaviorUtils.setWalkAndLookTargetMemories(pathfinderMob, blockPos2.immutable(), this.speedModifier, 1);
						return;
					}
				}
			}
		}
	}
}
