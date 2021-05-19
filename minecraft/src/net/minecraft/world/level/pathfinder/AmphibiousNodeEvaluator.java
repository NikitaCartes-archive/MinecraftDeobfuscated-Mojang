package net.minecraft.world.level.pathfinder;

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
		mob.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
		this.oldWalkableCost = mob.getPathfindingMalus(BlockPathTypes.WALKABLE);
		mob.setPathfindingMalus(BlockPathTypes.WALKABLE, 6.0F);
		this.oldWaterBorderCost = mob.getPathfindingMalus(BlockPathTypes.WATER_BORDER);
		mob.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 4.0F);
	}

	@Override
	public void done() {
		this.mob.setPathfindingMalus(BlockPathTypes.WALKABLE, this.oldWalkableCost);
		this.mob.setPathfindingMalus(BlockPathTypes.WATER_BORDER, this.oldWaterBorderCost);
		super.done();
	}

	@Override
	public Node getStart() {
		return this.getNode(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5), Mth.floor(this.mob.getBoundingBox().minZ));
	}

	@Override
	public Target getGoal(double d, double e, double f) {
		return new Target(this.getNode(Mth.floor(d), Mth.floor(e + 0.5), Mth.floor(f)));
	}

	@Override
	public int getNeighbors(Node[] nodes, Node node) {
		int i = super.getNeighbors(nodes, node);
		BlockPathTypes blockPathTypes = this.getCachedBlockType(this.mob, node.x, node.y + 1, node.z);
		BlockPathTypes blockPathTypes2 = this.getCachedBlockType(this.mob, node.x, node.y, node.z);
		int j;
		if (this.mob.getPathfindingMalus(blockPathTypes) >= 0.0F && blockPathTypes2 != BlockPathTypes.STICKY_HONEY) {
			j = Mth.floor(Math.max(1.0F, this.mob.maxUpStep));
		} else {
			j = 0;
		}

		double d = this.getFloorLevel(new BlockPos(node.x, node.y, node.z));
		Node node2 = this.findAcceptedNode(node.x, node.y + 1, node.z, Math.max(0, j - 1), d, Direction.UP, blockPathTypes2);
		Node node3 = this.findAcceptedNode(node.x, node.y - 1, node.z, j, d, Direction.DOWN, blockPathTypes2);
		if (this.isNeighborValid(node2, node)) {
			nodes[i++] = node2;
		}

		if (this.isNeighborValid(node3, node) && blockPathTypes2 != BlockPathTypes.TRAPDOOR) {
			nodes[i++] = node3;
		}

		for (int k = 0; k < i; k++) {
			Node node4 = nodes[k];
			if (node4.type == BlockPathTypes.WATER && this.prefersShallowSwimming && node4.y < this.mob.level.getSeaLevel() - 10) {
				node4.costMalus++;
			}
		}

		return i;
	}

	@Override
	protected double getFloorLevel(BlockPos blockPos) {
		return this.mob.isInWater() ? (double)blockPos.getY() + 0.5 : super.getFloorLevel(blockPos);
	}

	@Override
	protected boolean isAmphibious() {
		return true;
	}

	@Override
	public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int j, int k) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		BlockPathTypes blockPathTypes = getBlockPathTypeRaw(blockGetter, mutableBlockPos.set(i, j, k));
		if (blockPathTypes == BlockPathTypes.WATER) {
			for (Direction direction : Direction.values()) {
				BlockPathTypes blockPathTypes2 = getBlockPathTypeRaw(blockGetter, mutableBlockPos.set(i, j, k).move(direction));
				if (blockPathTypes2 == BlockPathTypes.BLOCKED) {
					return BlockPathTypes.WATER_BORDER;
				}
			}

			return BlockPathTypes.WATER;
		} else {
			return getBlockPathTypeStatic(blockGetter, mutableBlockPos);
		}
	}
}
