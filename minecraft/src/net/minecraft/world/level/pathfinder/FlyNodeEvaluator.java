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
			i = Mth.floor(this.mob.getBoundingBox().minY);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(this.mob.x, (double)i, this.mob.z);

			for (Block block = this.level.getBlockState(mutableBlockPos).getBlock(); block == Blocks.WATER; block = this.level.getBlockState(mutableBlockPos).getBlock()) {
				mutableBlockPos.set(this.mob.x, (double)(++i), this.mob.z);
			}
		} else {
			i = Mth.floor(this.mob.getBoundingBox().minY + 0.5);
		}

		BlockPos blockPos = new BlockPos(this.mob);
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
		if (node2 != null && !node2.closed) {
			nodes[i++] = node2;
		}

		Node node3 = this.getNode(node.x - 1, node.y, node.z);
		if (node3 != null && !node3.closed) {
			nodes[i++] = node3;
		}

		Node node4 = this.getNode(node.x + 1, node.y, node.z);
		if (node4 != null && !node4.closed) {
			nodes[i++] = node4;
		}

		Node node5 = this.getNode(node.x, node.y, node.z - 1);
		if (node5 != null && !node5.closed) {
			nodes[i++] = node5;
		}

		Node node6 = this.getNode(node.x, node.y + 1, node.z);
		if (node6 != null && !node6.closed) {
			nodes[i++] = node6;
		}

		Node node7 = this.getNode(node.x, node.y - 1, node.z);
		if (node7 != null && !node7.closed) {
			nodes[i++] = node7;
		}

		Node node8 = this.getNode(node.x + 1, node.y, node.z - 1);
		if (node8 != null && !node8.closed && node5 != null && node5.costMalus >= 0.0F && node4 != null && node4.costMalus >= 0.0F) {
			nodes[i++] = node8;
		}

		Node node9 = this.getNode(node.x + 1, node.y, node.z + 1);
		if (node9 != null && !node9.closed && node2 != null && node2.costMalus >= 0.0F && node4 != null && node4.costMalus >= 0.0F) {
			nodes[i++] = node9;
		}

		Node node10 = this.getNode(node.x - 1, node.y, node.z - 1);
		if (node10 != null && !node10.closed && node5 != null && node5.costMalus >= 0.0F && node3 != null && node3.costMalus >= 0.0F) {
			nodes[i++] = node10;
		}

		Node node11 = this.getNode(node.x - 1, node.y, node.z + 1);
		if (node11 != null && !node11.closed && node2 != null && node2.costMalus >= 0.0F && node3 != null && node3.costMalus >= 0.0F) {
			nodes[i++] = node11;
		}

		Node node12 = this.getNode(node.x + 1, node.y + 1, node.z - 1);
		if (node12 != null && !node12.closed && node8 != null && node8.costMalus >= 0.0F && node6 != null && node6.costMalus >= 0.0F) {
			nodes[i++] = node12;
		}

		Node node13 = this.getNode(node.x + 1, node.y + 1, node.z + 1);
		if (node13 != null && !node13.closed && node9 != null && node9.costMalus >= 0.0F && node6 != null && node6.costMalus >= 0.0F) {
			nodes[i++] = node13;
		}

		Node node14 = this.getNode(node.x - 1, node.y + 1, node.z - 1);
		if (node14 != null && !node14.closed && node10 != null && node10.costMalus >= 0.0F && node6 != null && node6.costMalus >= 0.0F) {
			nodes[i++] = node14;
		}

		Node node15 = this.getNode(node.x - 1, node.y + 1, node.z + 1);
		if (node15 != null && !node15.closed && node11 != null && node11.costMalus >= 0.0F && node6 != null && node6.costMalus >= 0.0F) {
			nodes[i++] = node15;
		}

		Node node16 = this.getNode(node.x + 1, node.y - 1, node.z - 1);
		if (node16 != null && !node16.closed && node8 != null && node8.costMalus >= 0.0F && node7 != null && node7.costMalus >= 0.0F) {
			nodes[i++] = node16;
		}

		Node node17 = this.getNode(node.x + 1, node.y - 1, node.z + 1);
		if (node17 != null && !node17.closed && node9 != null && node9.costMalus >= 0.0F && node7 != null && node7.costMalus >= 0.0F) {
			nodes[i++] = node17;
		}

		Node node18 = this.getNode(node.x - 1, node.y - 1, node.z - 1);
		if (node18 != null && !node18.closed && node10 != null && node10.costMalus >= 0.0F && node7 != null && node7.costMalus >= 0.0F) {
			nodes[i++] = node18;
		}

		Node node19 = this.getNode(node.x - 1, node.y - 1, node.z + 1);
		if (node19 != null && !node19.closed && node11 != null && node11.costMalus >= 0.0F && node7 != null && node7.costMalus >= 0.0F) {
			nodes[i++] = node19;
		}

		return i;
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
		BlockPos blockPos = new BlockPos(mob);
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
		BlockPathTypes blockPathTypes = this.getBlockPathTypeRaw(blockGetter, i, j, k);
		if (blockPathTypes == BlockPathTypes.OPEN && j >= 1) {
			Block block = blockGetter.getBlockState(new BlockPos(i, j - 1, k)).getBlock();
			BlockPathTypes blockPathTypes2 = this.getBlockPathTypeRaw(blockGetter, i, j - 1, k);
			if (blockPathTypes2 == BlockPathTypes.DAMAGE_FIRE || block == Blocks.MAGMA_BLOCK || blockPathTypes2 == BlockPathTypes.LAVA || block == Blocks.CAMPFIRE) {
				blockPathTypes = BlockPathTypes.DAMAGE_FIRE;
			} else if (blockPathTypes2 == BlockPathTypes.DAMAGE_CACTUS) {
				blockPathTypes = BlockPathTypes.DAMAGE_CACTUS;
			} else if (blockPathTypes2 == BlockPathTypes.DAMAGE_OTHER) {
				blockPathTypes = BlockPathTypes.DAMAGE_OTHER;
			} else {
				blockPathTypes = blockPathTypes2 != BlockPathTypes.WALKABLE && blockPathTypes2 != BlockPathTypes.OPEN && blockPathTypes2 != BlockPathTypes.WATER
					? BlockPathTypes.WALKABLE
					: BlockPathTypes.OPEN;
			}
		}

		return this.checkNeighbourBlocks(blockGetter, i, j, k, blockPathTypes);
	}

	private BlockPathTypes getBlockPathType(Mob mob, BlockPos blockPos) {
		return this.getBlockPathType(mob, blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	private BlockPathTypes getBlockPathType(Mob mob, int i, int j, int k) {
		return this.getBlockPathType(this.level, i, j, k, mob, this.entityWidth, this.entityHeight, this.entityDepth, this.canOpenDoors(), this.canPassDoors());
	}
}
