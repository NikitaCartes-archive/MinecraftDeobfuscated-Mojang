package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class FlyNodeEvaluator extends WalkNodeEvaluator {
	@Override
	public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
		super.prepare(pathNavigationRegion, mob);
		this.oldWaterCost = mob.getPathfindingMalus(BlockPathTypes.WATER);
	}

	@Override
	public void done() {
		this.mob.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
		super.done();
	}

	@Override
	public Node getStart() {
		int i;
		if (this.canFloat() && this.mob.isInWater()) {
			i = Mth.floor(this.mob.getY());
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(this.mob.getX(), (double)i, this.mob.getZ());

			for (Block block = this.level.getBlockState(mutableBlockPos).getBlock(); block == Blocks.WATER; block = this.level.getBlockState(mutableBlockPos).getBlock()) {
				mutableBlockPos.set(this.mob.getX(), (double)(++i), this.mob.getZ());
			}
		} else {
			i = Mth.floor(this.mob.getY() + 0.5);
		}

		BlockPos blockPos = this.mob.blockPosition();
		BlockPathTypes blockPathTypes = this.getBlockPathType(this.mob, blockPos.getX(), i, blockPos.getZ());
		if (this.mob.getPathfindingMalus(blockPathTypes) < 0.0F) {
			Set<BlockPos> set = Sets.<BlockPos>newHashSet();
			set.add(new BlockPos(this.mob.getBoundingBox().minX, (double)i, this.mob.getBoundingBox().minZ));
			set.add(new BlockPos(this.mob.getBoundingBox().minX, (double)i, this.mob.getBoundingBox().maxZ));
			set.add(new BlockPos(this.mob.getBoundingBox().maxX, (double)i, this.mob.getBoundingBox().minZ));
			set.add(new BlockPos(this.mob.getBoundingBox().maxX, (double)i, this.mob.getBoundingBox().maxZ));

			for (BlockPos blockPos2 : set) {
				BlockPathTypes blockPathTypes2 = this.getBlockPathType(this.mob, blockPos2);
				if (this.mob.getPathfindingMalus(blockPathTypes2) >= 0.0F) {
					return super.getNode(blockPos2.getX(), blockPos2.getY(), blockPos2.getZ());
				}
			}
		}

		return super.getNode(blockPos.getX(), i, blockPos.getZ());
	}

	@Override
	public Target getGoal(double d, double e, double f) {
		return new Target(super.getNode(Mth.floor(d), Mth.floor(e), Mth.floor(f)));
	}

	@Override
	public int getNeighbors(Node[] nodes, Node node) {
		int i = 0;
		Node node2 = this.getNode(node.x, node.y, node.z + 1);
		if (this.isOpen(node2)) {
			nodes[i++] = node2;
		}

		Node node3 = this.getNode(node.x - 1, node.y, node.z);
		if (this.isOpen(node3)) {
			nodes[i++] = node3;
		}

		Node node4 = this.getNode(node.x + 1, node.y, node.z);
		if (this.isOpen(node4)) {
			nodes[i++] = node4;
		}

		Node node5 = this.getNode(node.x, node.y, node.z - 1);
		if (this.isOpen(node5)) {
			nodes[i++] = node5;
		}

		Node node6 = this.getNode(node.x, node.y + 1, node.z);
		if (this.isOpen(node6)) {
			nodes[i++] = node6;
		}

		Node node7 = this.getNode(node.x, node.y - 1, node.z);
		if (this.isOpen(node7)) {
			nodes[i++] = node7;
		}

		Node node8 = this.getNode(node.x, node.y + 1, node.z + 1);
		if (this.isOpen(node8) && this.hasMalus(node2) && this.hasMalus(node6)) {
			nodes[i++] = node8;
		}

		Node node9 = this.getNode(node.x - 1, node.y + 1, node.z);
		if (this.isOpen(node9) && this.hasMalus(node3) && this.hasMalus(node6)) {
			nodes[i++] = node9;
		}

		Node node10 = this.getNode(node.x + 1, node.y + 1, node.z);
		if (this.isOpen(node10) && this.hasMalus(node4) && this.hasMalus(node6)) {
			nodes[i++] = node10;
		}

		Node node11 = this.getNode(node.x, node.y + 1, node.z - 1);
		if (this.isOpen(node11) && this.hasMalus(node5) && this.hasMalus(node6)) {
			nodes[i++] = node11;
		}

		Node node12 = this.getNode(node.x, node.y - 1, node.z + 1);
		if (this.isOpen(node12) && this.hasMalus(node2) && this.hasMalus(node7)) {
			nodes[i++] = node12;
		}

		Node node13 = this.getNode(node.x - 1, node.y - 1, node.z);
		if (this.isOpen(node13) && this.hasMalus(node3) && this.hasMalus(node7)) {
			nodes[i++] = node13;
		}

		Node node14 = this.getNode(node.x + 1, node.y - 1, node.z);
		if (this.isOpen(node14) && this.hasMalus(node4) && this.hasMalus(node7)) {
			nodes[i++] = node14;
		}

		Node node15 = this.getNode(node.x, node.y - 1, node.z - 1);
		if (this.isOpen(node15) && this.hasMalus(node5) && this.hasMalus(node7)) {
			nodes[i++] = node15;
		}

		Node node16 = this.getNode(node.x + 1, node.y, node.z - 1);
		if (this.isOpen(node16) && this.hasMalus(node5) && this.hasMalus(node4)) {
			nodes[i++] = node16;
		}

		Node node17 = this.getNode(node.x + 1, node.y, node.z + 1);
		if (this.isOpen(node17) && this.hasMalus(node2) && this.hasMalus(node4)) {
			nodes[i++] = node17;
		}

		Node node18 = this.getNode(node.x - 1, node.y, node.z - 1);
		if (this.isOpen(node18) && this.hasMalus(node5) && this.hasMalus(node3)) {
			nodes[i++] = node18;
		}

		Node node19 = this.getNode(node.x - 1, node.y, node.z + 1);
		if (this.isOpen(node19) && this.hasMalus(node2) && this.hasMalus(node3)) {
			nodes[i++] = node19;
		}

		Node node20 = this.getNode(node.x + 1, node.y + 1, node.z - 1);
		if (this.isOpen(node20) && this.hasMalus(node16) && this.hasMalus(node11) && this.hasMalus(node10)) {
			nodes[i++] = node20;
		}

		Node node21 = this.getNode(node.x + 1, node.y + 1, node.z + 1);
		if (this.isOpen(node21) && this.hasMalus(node17) && this.hasMalus(node8) && this.hasMalus(node10)) {
			nodes[i++] = node21;
		}

		Node node22 = this.getNode(node.x - 1, node.y + 1, node.z - 1);
		if (this.isOpen(node22) && this.hasMalus(node18) && this.hasMalus(node11) && this.hasMalus(node9)) {
			nodes[i++] = node22;
		}

		Node node23 = this.getNode(node.x - 1, node.y + 1, node.z + 1);
		if (this.isOpen(node23) && this.hasMalus(node19) && this.hasMalus(node8) && this.hasMalus(node9)) {
			nodes[i++] = node23;
		}

		Node node24 = this.getNode(node.x + 1, node.y - 1, node.z - 1);
		if (this.isOpen(node24) && this.hasMalus(node16) && this.hasMalus(node15) && this.hasMalus(node14)) {
			nodes[i++] = node24;
		}

		Node node25 = this.getNode(node.x + 1, node.y - 1, node.z + 1);
		if (this.isOpen(node25) && this.hasMalus(node17) && this.hasMalus(node12) && this.hasMalus(node14)) {
			nodes[i++] = node25;
		}

		Node node26 = this.getNode(node.x - 1, node.y - 1, node.z - 1);
		if (this.isOpen(node26) && this.hasMalus(node18) && this.hasMalus(node15) && this.hasMalus(node13)) {
			nodes[i++] = node26;
		}

		Node node27 = this.getNode(node.x - 1, node.y - 1, node.z + 1);
		if (this.isOpen(node27) && this.hasMalus(node19) && this.hasMalus(node12) && this.hasMalus(node13)) {
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
	@Override
	protected Node getNode(int i, int j, int k) {
		Node node = null;
		BlockPathTypes blockPathTypes = this.getBlockPathType(this.mob, i, j, k);
		float f = this.mob.getPathfindingMalus(blockPathTypes);
		if (f >= 0.0F) {
			node = super.getNode(i, j, k);
			node.type = blockPathTypes;
			node.costMalus = Math.max(node.costMalus, f);
			if (blockPathTypes == BlockPathTypes.WALKABLE) {
				node.costMalus++;
			}
		}

		return blockPathTypes != BlockPathTypes.OPEN && blockPathTypes != BlockPathTypes.WALKABLE ? node : node;
	}

	@Override
	public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int j, int k, Mob mob, int l, int m, int n, boolean bl, boolean bl2) {
		EnumSet<BlockPathTypes> enumSet = EnumSet.noneOf(BlockPathTypes.class);
		BlockPathTypes blockPathTypes = BlockPathTypes.BLOCKED;
		BlockPos blockPos = mob.blockPosition();
		blockPathTypes = this.getBlockPathTypes(blockGetter, i, j, k, l, m, n, bl, bl2, enumSet, blockPathTypes, blockPos);
		if (enumSet.contains(BlockPathTypes.FENCE)) {
			return BlockPathTypes.FENCE;
		} else {
			BlockPathTypes blockPathTypes2 = BlockPathTypes.BLOCKED;

			for (BlockPathTypes blockPathTypes3 : enumSet) {
				if (mob.getPathfindingMalus(blockPathTypes3) < 0.0F) {
					return blockPathTypes3;
				}

				if (mob.getPathfindingMalus(blockPathTypes3) >= mob.getPathfindingMalus(blockPathTypes2)) {
					blockPathTypes2 = blockPathTypes3;
				}
			}

			return blockPathTypes == BlockPathTypes.OPEN && mob.getPathfindingMalus(blockPathTypes2) == 0.0F ? BlockPathTypes.OPEN : blockPathTypes2;
		}
	}

	@Override
	public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int j, int k) {
		BlockPathTypes blockPathTypes = getBlockPathTypeRaw(blockGetter, i, j, k);
		if (blockPathTypes == BlockPathTypes.OPEN && j >= 1) {
			Block block = blockGetter.getBlockState(new BlockPos(i, j - 1, k)).getBlock();
			BlockPathTypes blockPathTypes2 = getBlockPathTypeRaw(blockGetter, i, j - 1, k);
			if (blockPathTypes2 == BlockPathTypes.DAMAGE_FIRE || block == Blocks.MAGMA_BLOCK || blockPathTypes2 == BlockPathTypes.LAVA || block == Blocks.CAMPFIRE) {
				blockPathTypes = BlockPathTypes.DAMAGE_FIRE;
			} else if (blockPathTypes2 == BlockPathTypes.DAMAGE_CACTUS) {
				blockPathTypes = BlockPathTypes.DAMAGE_CACTUS;
			} else if (blockPathTypes2 == BlockPathTypes.DAMAGE_OTHER) {
				blockPathTypes = BlockPathTypes.DAMAGE_OTHER;
			} else if (blockPathTypes2 == BlockPathTypes.COCOA) {
				blockPathTypes = BlockPathTypes.COCOA;
			} else if (blockPathTypes2 == BlockPathTypes.FENCE) {
				blockPathTypes = BlockPathTypes.FENCE;
			} else {
				blockPathTypes = blockPathTypes2 != BlockPathTypes.WALKABLE && blockPathTypes2 != BlockPathTypes.OPEN && blockPathTypes2 != BlockPathTypes.WATER
					? BlockPathTypes.WALKABLE
					: BlockPathTypes.OPEN;
			}
		}

		if (blockPathTypes == BlockPathTypes.WALKABLE || blockPathTypes == BlockPathTypes.OPEN) {
			blockPathTypes = checkNeighbourBlocks(blockGetter, i, j, k, blockPathTypes);
		}

		return blockPathTypes;
	}

	private BlockPathTypes getBlockPathType(Mob mob, BlockPos blockPos) {
		return this.getBlockPathType(mob, blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	private BlockPathTypes getBlockPathType(Mob mob, int i, int j, int k) {
		return this.getBlockPathType(this.level, i, j, k, mob, this.entityWidth, this.entityHeight, this.entityDepth, this.canOpenDoors(), this.canPassDoors());
	}
}
