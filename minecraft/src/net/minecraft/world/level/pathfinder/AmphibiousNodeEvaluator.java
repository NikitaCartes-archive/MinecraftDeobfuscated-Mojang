package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;

public class AmphibiousNodeEvaluator extends WalkNodeEvaluator {
	private final boolean prefersShallowSwimming;
	private float oldWalkableCost;
	private float oldWaterBorderCost;

	public AmphibiousNodeEvaluator(boolean bl) {
		this.prefersShallowSwimming = bl;
	}

	@Override
	public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
		super.prepare(pathNavigationRegion, mob);
		mob.setPathfindingMalus(PathType.WATER, 0.0F);
		this.oldWalkableCost = mob.getPathfindingMalus(PathType.WALKABLE);
		mob.setPathfindingMalus(PathType.WALKABLE, 6.0F);
		this.oldWaterBorderCost = mob.getPathfindingMalus(PathType.WATER_BORDER);
		mob.setPathfindingMalus(PathType.WATER_BORDER, 4.0F);
	}

	@Override
	public void done() {
		this.mob.setPathfindingMalus(PathType.WALKABLE, this.oldWalkableCost);
		this.mob.setPathfindingMalus(PathType.WATER_BORDER, this.oldWaterBorderCost);
		super.done();
	}

	@Override
	public Node getStart() {
		return !this.mob.isInWater()
			? super.getStart()
			: this.getStartNode(
				new BlockPos(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5), Mth.floor(this.mob.getBoundingBox().minZ))
			);
	}

	@Override
	public Target getTarget(double d, double e, double f) {
		return this.getTargetNodeAt(d, e + 0.5, f);
	}

	@Override
	public int getNeighbors(Node[] nodes, Node node) {
		int i = super.getNeighbors(nodes, node);
		PathType pathType = this.getCachedPathType(node.x, node.y + 1, node.z);
		PathType pathType2 = this.getCachedPathType(node.x, node.y, node.z);
		int j;
		if (this.mob.getPathfindingMalus(pathType) >= 0.0F && pathType2 != PathType.STICKY_HONEY) {
			j = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
		} else {
			j = 0;
		}

		double d = this.getFloorLevel(new BlockPos(node.x, node.y, node.z));
		Node node2 = this.findAcceptedNode(node.x, node.y + 1, node.z, Math.max(0, j - 1), d, Direction.UP, pathType2);
		Node node3 = this.findAcceptedNode(node.x, node.y - 1, node.z, j, d, Direction.DOWN, pathType2);
		if (this.isVerticalNeighborValid(node2, node)) {
			nodes[i++] = node2;
		}

		if (this.isVerticalNeighborValid(node3, node) && pathType2 != PathType.TRAPDOOR) {
			nodes[i++] = node3;
		}

		for (int k = 0; k < i; k++) {
			Node node4 = nodes[k];
			if (node4.type == PathType.WATER && this.prefersShallowSwimming && node4.y < this.mob.level().getSeaLevel() - 10) {
				node4.costMalus++;
			}
		}

		return i;
	}

	private boolean isVerticalNeighborValid(@Nullable Node node, Node node2) {
		return this.isNeighborValid(node, node2) && node.type == PathType.WATER;
	}

	@Override
	protected boolean isAmphibious() {
		return true;
	}

	@Override
	public PathType getPathType(BlockGetter blockGetter, int i, int j, int k) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		PathType pathType = getPathTypeFromState(blockGetter, mutableBlockPos.set(i, j, k));
		if (pathType == PathType.WATER) {
			for (Direction direction : Direction.values()) {
				PathType pathType2 = getPathTypeFromState(blockGetter, mutableBlockPos.set(i, j, k).move(direction));
				if (pathType2 == PathType.BLOCKED) {
					return PathType.WATER_BORDER;
				}
			}

			return PathType.WATER;
		} else {
			return getPathTypeStatic(blockGetter, mutableBlockPos);
		}
	}
}
