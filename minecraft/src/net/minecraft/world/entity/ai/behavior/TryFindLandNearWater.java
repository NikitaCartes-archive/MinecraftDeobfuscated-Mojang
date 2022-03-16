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
import net.minecraft.world.phys.shapes.CollisionContext;

public class TryFindLandNearWater extends Behavior<PathfinderMob> {
	private final int range;
	private final float speedModifier;
	private long nextOkStartTime;

	public TryFindLandNearWater(int i, float f) {
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
		this.nextOkStartTime = l + 40L;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
		return !pathfinderMob.level.getFluidState(pathfinderMob.blockPosition()).is(FluidTags.WATER);
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		if (l >= this.nextOkStartTime) {
			CollisionContext collisionContext = CollisionContext.of(pathfinderMob);
			BlockPos blockPos = pathfinderMob.blockPosition();
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (BlockPos blockPos2 : BlockPos.withinManhattan(blockPos, this.range, this.range, this.range)) {
				if ((blockPos2.getX() != blockPos.getX() || blockPos2.getZ() != blockPos.getZ())
					&& serverLevel.getBlockState(blockPos2).getCollisionShape(serverLevel, blockPos2, collisionContext).isEmpty()
					&& !serverLevel.getBlockState(mutableBlockPos.setWithOffset(blockPos2, Direction.DOWN))
						.getCollisionShape(serverLevel, blockPos2, collisionContext)
						.isEmpty()) {
					for (Direction direction : Direction.Plane.HORIZONTAL) {
						mutableBlockPos.setWithOffset(blockPos2, direction);
						if (serverLevel.getBlockState(mutableBlockPos).isAir() && serverLevel.getBlockState(mutableBlockPos.move(Direction.DOWN)).is(Blocks.WATER)) {
							this.nextOkStartTime = l + 40L;
							BehaviorUtils.setWalkAndLookTargetMemories(pathfinderMob, blockPos2, this.speedModifier, 0);
							return;
						}
					}
				}
			}
		}
	}
}
