package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;

public class PathfindingContext {
	private final CollisionGetter level;
	@Nullable
	private final PathTypeCache cache;
	private final BlockPos mobPosition;
	private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

	public PathfindingContext(CollisionGetter collisionGetter, Mob mob) {
		this.level = collisionGetter;
		if (mob.level() instanceof ServerLevel serverLevel) {
			this.cache = serverLevel.getPathTypeCache();
		} else {
			this.cache = null;
		}

		this.mobPosition = mob.blockPosition();
	}

	public PathType getPathTypeFromState(int i, int j, int k) {
		BlockPos blockPos = this.mutablePos.set(i, j, k);
		return this.cache == null ? WalkNodeEvaluator.getPathTypeFromState(this.level, blockPos) : this.cache.getOrCompute(this.level, blockPos);
	}

	public BlockState getBlockState(BlockPos blockPos) {
		return this.level.getBlockState(blockPos);
	}

	public CollisionGetter level() {
		return this.level;
	}

	public BlockPos mobPosition() {
		return this.mobPosition;
	}
}
