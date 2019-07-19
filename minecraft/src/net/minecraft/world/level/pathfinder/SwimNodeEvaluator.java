package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class SwimNodeEvaluator extends NodeEvaluator {
	private final boolean allowBreaching;

	public SwimNodeEvaluator(boolean bl) {
		this.allowBreaching = bl;
	}

	@Override
	public Node getStart() {
		return super.getNode(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5), Mth.floor(this.mob.getBoundingBox().minZ));
	}

	@Override
	public Target getGoal(double d, double e, double f) {
		return new Target(
			super.getNode(Mth.floor(d - (double)(this.mob.getBbWidth() / 2.0F)), Mth.floor(e + 0.5), Mth.floor(f - (double)(this.mob.getBbWidth() / 2.0F)))
		);
	}

	@Override
	public int getNeighbors(Node[] nodes, Node node) {
		int i = 0;

		for (Direction direction : Direction.values()) {
			Node node2 = this.getWaterNode(node.x + direction.getStepX(), node.y + direction.getStepY(), node.z + direction.getStepZ());
			if (node2 != null && !node2.closed) {
				nodes[i++] = node2;
			}
		}

		return i;
	}

	@Override
	public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int j, int k, Mob mob, int l, int m, int n, boolean bl, boolean bl2) {
		return this.getBlockPathType(blockGetter, i, j, k);
	}

	@Override
	public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int j, int k) {
		BlockPos blockPos = new BlockPos(i, j, k);
		FluidState fluidState = blockGetter.getFluidState(blockPos);
		BlockState blockState = blockGetter.getBlockState(blockPos);
		if (fluidState.isEmpty() && blockState.isPathfindable(blockGetter, blockPos.below(), PathComputationType.WATER) && blockState.isAir()) {
			return BlockPathTypes.BREACH;
		} else {
			return fluidState.is(FluidTags.WATER) && blockState.isPathfindable(blockGetter, blockPos, PathComputationType.WATER)
				? BlockPathTypes.WATER
				: BlockPathTypes.BLOCKED;
		}
	}

	@Nullable
	private Node getWaterNode(int i, int j, int k) {
		BlockPathTypes blockPathTypes = this.isFree(i, j, k);
		return (!this.allowBreaching || blockPathTypes != BlockPathTypes.BREACH) && blockPathTypes != BlockPathTypes.WATER ? null : this.getNode(i, j, k);
	}

	@Nullable
	@Override
	protected Node getNode(int i, int j, int k) {
		Node node = null;
		BlockPathTypes blockPathTypes = this.getBlockPathType(this.mob.level, i, j, k);
		float f = this.mob.getPathfindingMalus(blockPathTypes);
		if (f >= 0.0F) {
			node = super.getNode(i, j, k);
			node.type = blockPathTypes;
			node.costMalus = Math.max(node.costMalus, f);
			if (this.level.getFluidState(new BlockPos(i, j, k)).isEmpty()) {
				node.costMalus += 8.0F;
			}
		}

		return blockPathTypes == BlockPathTypes.OPEN ? node : node;
	}

	private BlockPathTypes isFree(int i, int j, int k) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int l = i; l < i + this.entityWidth; l++) {
			for (int m = j; m < j + this.entityHeight; m++) {
				for (int n = k; n < k + this.entityDepth; n++) {
					FluidState fluidState = this.level.getFluidState(mutableBlockPos.set(l, m, n));
					BlockState blockState = this.level.getBlockState(mutableBlockPos.set(l, m, n));
					if (fluidState.isEmpty() && blockState.isPathfindable(this.level, mutableBlockPos.below(), PathComputationType.WATER) && blockState.isAir()) {
						return BlockPathTypes.BREACH;
					}

					if (!fluidState.is(FluidTags.WATER)) {
						return BlockPathTypes.BLOCKED;
					}
				}
			}
		}

		BlockState blockState2 = this.level.getBlockState(mutableBlockPos);
		return blockState2.isPathfindable(this.level, mutableBlockPos, PathComputationType.WATER) ? BlockPathTypes.WATER : BlockPathTypes.BLOCKED;
	}
}
