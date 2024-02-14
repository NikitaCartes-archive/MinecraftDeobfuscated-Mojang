package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class FlyNodeEvaluator extends WalkNodeEvaluator {
	private final Long2ObjectMap<PathType> pathTypeByPosCache = new Long2ObjectOpenHashMap<>();
	private static final float SMALL_MOB_INFLATED_START_NODE_BOUNDING_BOX = 1.5F;
	private static final int MAX_START_NODE_CANDIDATES = 10;

	@Override
	public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
		super.prepare(pathNavigationRegion, mob);
		this.pathTypeByPosCache.clear();
		mob.onPathfindingStart();
	}

	@Override
	public void done() {
		this.mob.onPathfindingDone();
		this.pathTypeByPosCache.clear();
		super.done();
	}

	@Override
	public Node getStart() {
		int i;
		if (this.canFloat() && this.mob.isInWater()) {
			i = this.mob.getBlockY();
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(this.mob.getX(), (double)i, this.mob.getZ());

			for (BlockState blockState = this.level.getBlockState(mutableBlockPos); blockState.is(Blocks.WATER); blockState = this.level.getBlockState(mutableBlockPos)) {
				mutableBlockPos.set(this.mob.getX(), (double)(++i), this.mob.getZ());
			}
		} else {
			i = Mth.floor(this.mob.getY() + 0.5);
		}

		BlockPos blockPos = BlockPos.containing(this.mob.getX(), (double)i, this.mob.getZ());
		if (!this.canStartAt(blockPos)) {
			for (BlockPos blockPos2 : this.iteratePathfindingStartNodeCandidatePositions(this.mob)) {
				if (this.canStartAt(blockPos2)) {
					return super.getStartNode(blockPos2);
				}
			}
		}

		return super.getStartNode(blockPos);
	}

	@Override
	protected boolean canStartAt(BlockPos blockPos) {
		PathType pathType = this.getCachedPathType(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		return this.mob.getPathfindingMalus(pathType) >= 0.0F;
	}

	@Override
	public Target getTarget(double d, double e, double f) {
		return this.getTargetNodeAt(d, e, f);
	}

	@Override
	public int getNeighbors(Node[] nodes, Node node) {
		int i = 0;
		Node node2 = this.findAcceptedNode(node.x, node.y, node.z + 1);
		if (this.isOpen(node2)) {
			nodes[i++] = node2;
		}

		Node node3 = this.findAcceptedNode(node.x - 1, node.y, node.z);
		if (this.isOpen(node3)) {
			nodes[i++] = node3;
		}

		Node node4 = this.findAcceptedNode(node.x + 1, node.y, node.z);
		if (this.isOpen(node4)) {
			nodes[i++] = node4;
		}

		Node node5 = this.findAcceptedNode(node.x, node.y, node.z - 1);
		if (this.isOpen(node5)) {
			nodes[i++] = node5;
		}

		Node node6 = this.findAcceptedNode(node.x, node.y + 1, node.z);
		if (this.isOpen(node6)) {
			nodes[i++] = node6;
		}

		Node node7 = this.findAcceptedNode(node.x, node.y - 1, node.z);
		if (this.isOpen(node7)) {
			nodes[i++] = node7;
		}

		Node node8 = this.findAcceptedNode(node.x, node.y + 1, node.z + 1);
		if (this.isOpen(node8) && this.hasMalus(node2) && this.hasMalus(node6)) {
			nodes[i++] = node8;
		}

		Node node9 = this.findAcceptedNode(node.x - 1, node.y + 1, node.z);
		if (this.isOpen(node9) && this.hasMalus(node3) && this.hasMalus(node6)) {
			nodes[i++] = node9;
		}

		Node node10 = this.findAcceptedNode(node.x + 1, node.y + 1, node.z);
		if (this.isOpen(node10) && this.hasMalus(node4) && this.hasMalus(node6)) {
			nodes[i++] = node10;
		}

		Node node11 = this.findAcceptedNode(node.x, node.y + 1, node.z - 1);
		if (this.isOpen(node11) && this.hasMalus(node5) && this.hasMalus(node6)) {
			nodes[i++] = node11;
		}

		Node node12 = this.findAcceptedNode(node.x, node.y - 1, node.z + 1);
		if (this.isOpen(node12) && this.hasMalus(node2) && this.hasMalus(node7)) {
			nodes[i++] = node12;
		}

		Node node13 = this.findAcceptedNode(node.x - 1, node.y - 1, node.z);
		if (this.isOpen(node13) && this.hasMalus(node3) && this.hasMalus(node7)) {
			nodes[i++] = node13;
		}

		Node node14 = this.findAcceptedNode(node.x + 1, node.y - 1, node.z);
		if (this.isOpen(node14) && this.hasMalus(node4) && this.hasMalus(node7)) {
			nodes[i++] = node14;
		}

		Node node15 = this.findAcceptedNode(node.x, node.y - 1, node.z - 1);
		if (this.isOpen(node15) && this.hasMalus(node5) && this.hasMalus(node7)) {
			nodes[i++] = node15;
		}

		Node node16 = this.findAcceptedNode(node.x + 1, node.y, node.z - 1);
		if (this.isOpen(node16) && this.hasMalus(node5) && this.hasMalus(node4)) {
			nodes[i++] = node16;
		}

		Node node17 = this.findAcceptedNode(node.x + 1, node.y, node.z + 1);
		if (this.isOpen(node17) && this.hasMalus(node2) && this.hasMalus(node4)) {
			nodes[i++] = node17;
		}

		Node node18 = this.findAcceptedNode(node.x - 1, node.y, node.z - 1);
		if (this.isOpen(node18) && this.hasMalus(node5) && this.hasMalus(node3)) {
			nodes[i++] = node18;
		}

		Node node19 = this.findAcceptedNode(node.x - 1, node.y, node.z + 1);
		if (this.isOpen(node19) && this.hasMalus(node2) && this.hasMalus(node3)) {
			nodes[i++] = node19;
		}

		Node node20 = this.findAcceptedNode(node.x + 1, node.y + 1, node.z - 1);
		if (this.isOpen(node20)
			&& this.hasMalus(node16)
			&& this.hasMalus(node5)
			&& this.hasMalus(node4)
			&& this.hasMalus(node6)
			&& this.hasMalus(node11)
			&& this.hasMalus(node10)) {
			nodes[i++] = node20;
		}

		Node node21 = this.findAcceptedNode(node.x + 1, node.y + 1, node.z + 1);
		if (this.isOpen(node21)
			&& this.hasMalus(node17)
			&& this.hasMalus(node2)
			&& this.hasMalus(node4)
			&& this.hasMalus(node6)
			&& this.hasMalus(node8)
			&& this.hasMalus(node10)) {
			nodes[i++] = node21;
		}

		Node node22 = this.findAcceptedNode(node.x - 1, node.y + 1, node.z - 1);
		if (this.isOpen(node22)
			&& this.hasMalus(node18)
			&& this.hasMalus(node5)
			&& this.hasMalus(node3)
			&& this.hasMalus(node6)
			&& this.hasMalus(node11)
			&& this.hasMalus(node9)) {
			nodes[i++] = node22;
		}

		Node node23 = this.findAcceptedNode(node.x - 1, node.y + 1, node.z + 1);
		if (this.isOpen(node23)
			&& this.hasMalus(node19)
			&& this.hasMalus(node2)
			&& this.hasMalus(node3)
			&& this.hasMalus(node6)
			&& this.hasMalus(node8)
			&& this.hasMalus(node9)) {
			nodes[i++] = node23;
		}

		Node node24 = this.findAcceptedNode(node.x + 1, node.y - 1, node.z - 1);
		if (this.isOpen(node24)
			&& this.hasMalus(node16)
			&& this.hasMalus(node5)
			&& this.hasMalus(node4)
			&& this.hasMalus(node7)
			&& this.hasMalus(node15)
			&& this.hasMalus(node14)) {
			nodes[i++] = node24;
		}

		Node node25 = this.findAcceptedNode(node.x + 1, node.y - 1, node.z + 1);
		if (this.isOpen(node25)
			&& this.hasMalus(node17)
			&& this.hasMalus(node2)
			&& this.hasMalus(node4)
			&& this.hasMalus(node7)
			&& this.hasMalus(node12)
			&& this.hasMalus(node14)) {
			nodes[i++] = node25;
		}

		Node node26 = this.findAcceptedNode(node.x - 1, node.y - 1, node.z - 1);
		if (this.isOpen(node26)
			&& this.hasMalus(node18)
			&& this.hasMalus(node5)
			&& this.hasMalus(node3)
			&& this.hasMalus(node7)
			&& this.hasMalus(node15)
			&& this.hasMalus(node13)) {
			nodes[i++] = node26;
		}

		Node node27 = this.findAcceptedNode(node.x - 1, node.y - 1, node.z + 1);
		if (this.isOpen(node27)
			&& this.hasMalus(node19)
			&& this.hasMalus(node2)
			&& this.hasMalus(node3)
			&& this.hasMalus(node7)
			&& this.hasMalus(node12)
			&& this.hasMalus(node13)) {
			nodes[i++] = node27;
		}

		return i;
	}

	private boolean hasMalus(@Nullable Node node) {
		return node != null && node.costMalus >= 0.0F;
	}

	private boolean isOpen(@Nullable Node node) {
		return node != null && !node.closed;
	}

	@Nullable
	protected Node findAcceptedNode(int i, int j, int k) {
		Node node = null;
		PathType pathType = this.getCachedPathType(i, j, k);
		float f = this.mob.getPathfindingMalus(pathType);
		if (f >= 0.0F) {
			node = this.getNode(i, j, k);
			node.type = pathType;
			node.costMalus = Math.max(node.costMalus, f);
			if (pathType == PathType.WALKABLE) {
				node.costMalus++;
			}
		}

		return node;
	}

	@Override
	protected PathType getCachedPathType(int i, int j, int k) {
		return this.pathTypeByPosCache
			.computeIfAbsent(BlockPos.asLong(i, j, k), (Long2ObjectFunction<? extends PathType>)(l -> this.getPathTypeOfMob(this.level, i, j, k, this.mob)));
	}

	@Override
	public PathType getPathType(BlockGetter blockGetter, int i, int j, int k) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		PathType pathType = getPathTypeFromState(blockGetter, mutableBlockPos.set(i, j, k));
		if (pathType == PathType.OPEN && j >= blockGetter.getMinBuildHeight() + 1) {
			PathType pathType2 = getPathTypeFromState(blockGetter, mutableBlockPos.set(i, j - 1, k));
			if (pathType2 == PathType.DAMAGE_FIRE || pathType2 == PathType.LAVA) {
				pathType = PathType.DAMAGE_FIRE;
			} else if (pathType2 == PathType.DAMAGE_OTHER) {
				pathType = PathType.DAMAGE_OTHER;
			} else if (pathType2 == PathType.COCOA) {
				pathType = PathType.COCOA;
			} else if (pathType2 == PathType.FENCE) {
				if (!mutableBlockPos.equals(this.mob.blockPosition())) {
					pathType = PathType.FENCE;
				}
			} else {
				pathType = pathType2 != PathType.WALKABLE && pathType2 != PathType.OPEN && pathType2 != PathType.WATER ? PathType.WALKABLE : PathType.OPEN;
			}
		}

		if (pathType == PathType.WALKABLE || pathType == PathType.OPEN) {
			pathType = checkNeighbourBlocks(blockGetter, mutableBlockPos.set(i, j, k), pathType);
		}

		return pathType;
	}

	private Iterable<BlockPos> iteratePathfindingStartNodeCandidatePositions(Mob mob) {
		float f = 1.0F;
		AABB aABB = mob.getBoundingBox();
		boolean bl = aABB.getSize() < 1.0;
		if (!bl) {
			return List.of(
				BlockPos.containing(aABB.minX, (double)mob.getBlockY(), aABB.minZ),
				BlockPos.containing(aABB.minX, (double)mob.getBlockY(), aABB.maxZ),
				BlockPos.containing(aABB.maxX, (double)mob.getBlockY(), aABB.minZ),
				BlockPos.containing(aABB.maxX, (double)mob.getBlockY(), aABB.maxZ)
			);
		} else {
			double d = Math.max(0.0, (1.5 - aABB.getZsize()) / 2.0);
			double e = Math.max(0.0, (1.5 - aABB.getXsize()) / 2.0);
			double g = Math.max(0.0, (1.5 - aABB.getYsize()) / 2.0);
			AABB aABB2 = aABB.inflate(e, g, d);
			return BlockPos.randomBetweenClosed(
				mob.getRandom(),
				10,
				Mth.floor(aABB2.minX),
				Mth.floor(aABB2.minY),
				Mth.floor(aABB2.minZ),
				Mth.floor(aABB2.maxX),
				Mth.floor(aABB2.maxY),
				Mth.floor(aABB2.maxZ)
			);
		}
	}
}
