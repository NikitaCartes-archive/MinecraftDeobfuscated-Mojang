package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TurtleNodeEvaluator extends WalkNodeEvaluator {
	private float oldWalkableCost;
	private float oldWaterBorderCost;

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
		int i = 0;
		int j = 1;
		BlockPos blockPos = new BlockPos(node.x, node.y, node.z);
		double d = this.inWaterDependentPosHeight(blockPos);
		Node node2 = this.getAcceptedNode(node.x, node.y, node.z + 1, 1, d);
		Node node3 = this.getAcceptedNode(node.x - 1, node.y, node.z, 1, d);
		Node node4 = this.getAcceptedNode(node.x + 1, node.y, node.z, 1, d);
		Node node5 = this.getAcceptedNode(node.x, node.y, node.z - 1, 1, d);
		Node node6 = this.getAcceptedNode(node.x, node.y + 1, node.z, 0, d);
		Node node7 = this.getAcceptedNode(node.x, node.y - 1, node.z, 1, d);
		if (node2 != null && !node2.closed) {
			nodes[i++] = node2;
		}

		if (node3 != null && !node3.closed) {
			nodes[i++] = node3;
		}

		if (node4 != null && !node4.closed) {
			nodes[i++] = node4;
		}

		if (node5 != null && !node5.closed) {
			nodes[i++] = node5;
		}

		if (node6 != null && !node6.closed) {
			nodes[i++] = node6;
		}

		if (node7 != null && !node7.closed) {
			nodes[i++] = node7;
		}

		boolean bl = node5 == null || node5.type == BlockPathTypes.OPEN || node5.costMalus != 0.0F;
		boolean bl2 = node2 == null || node2.type == BlockPathTypes.OPEN || node2.costMalus != 0.0F;
		boolean bl3 = node4 == null || node4.type == BlockPathTypes.OPEN || node4.costMalus != 0.0F;
		boolean bl4 = node3 == null || node3.type == BlockPathTypes.OPEN || node3.costMalus != 0.0F;
		if (bl && bl4) {
			Node node8 = this.getAcceptedNode(node.x - 1, node.y, node.z - 1, 1, d);
			if (node8 != null && !node8.closed) {
				nodes[i++] = node8;
			}
		}

		if (bl && bl3) {
			Node node8 = this.getAcceptedNode(node.x + 1, node.y, node.z - 1, 1, d);
			if (node8 != null && !node8.closed) {
				nodes[i++] = node8;
			}
		}

		if (bl2 && bl4) {
			Node node8 = this.getAcceptedNode(node.x - 1, node.y, node.z + 1, 1, d);
			if (node8 != null && !node8.closed) {
				nodes[i++] = node8;
			}
		}

		if (bl2 && bl3) {
			Node node8 = this.getAcceptedNode(node.x + 1, node.y, node.z + 1, 1, d);
			if (node8 != null && !node8.closed) {
				nodes[i++] = node8;
			}
		}

		return i;
	}

	private double inWaterDependentPosHeight(BlockPos blockPos) {
		if (!this.mob.isInWater()) {
			BlockPos blockPos2 = blockPos.below();
			VoxelShape voxelShape = this.level.getBlockState(blockPos2).getCollisionShape(this.level, blockPos2);
			return (double)blockPos2.getY() + (voxelShape.isEmpty() ? 0.0 : voxelShape.max(Direction.Axis.Y));
		} else {
			return (double)blockPos.getY() + 0.5;
		}
	}

	@Nullable
	private Node getAcceptedNode(int i, int j, int k, int l, double d) {
		Node node = null;
		BlockPos blockPos = new BlockPos(i, j, k);
		double e = this.inWaterDependentPosHeight(blockPos);
		if (e - d > 1.125) {
			return null;
		} else {
			BlockPathTypes blockPathTypes = this.getBlockPathType(this.level, i, j, k, this.mob, this.entityWidth, this.entityHeight, this.entityDepth, false, false);
			float f = this.mob.getPathfindingMalus(blockPathTypes);
			double g = (double)this.mob.getBbWidth() / 2.0;
			if (f >= 0.0F) {
				node = this.getNode(i, j, k);
				node.type = blockPathTypes;
				node.costMalus = Math.max(node.costMalus, f);
			}

			if (blockPathTypes != BlockPathTypes.WATER && blockPathTypes != BlockPathTypes.WALKABLE) {
				if (node == null && l > 0 && blockPathTypes != BlockPathTypes.FENCE && blockPathTypes != BlockPathTypes.TRAPDOOR) {
					node = this.getAcceptedNode(i, j + 1, k, l - 1, d);
				}

				if (blockPathTypes == BlockPathTypes.OPEN) {
					AABB aABB = new AABB(
						(double)i - g + 0.5, (double)j + 0.001, (double)k - g + 0.5, (double)i + g + 0.5, (double)((float)j + this.mob.getBbHeight()), (double)k + g + 0.5
					);
					if (!this.mob.level.noCollision(this.mob, aABB)) {
						return null;
					}

					BlockPathTypes blockPathTypes2 = this.getBlockPathType(
						this.level, i, j - 1, k, this.mob, this.entityWidth, this.entityHeight, this.entityDepth, false, false
					);
					if (blockPathTypes2 == BlockPathTypes.BLOCKED) {
						node = this.getNode(i, j, k);
						node.type = BlockPathTypes.WALKABLE;
						node.costMalus = Math.max(node.costMalus, f);
						return node;
					}

					if (blockPathTypes2 == BlockPathTypes.WATER) {
						node = this.getNode(i, j, k);
						node.type = BlockPathTypes.WATER;
						node.costMalus = Math.max(node.costMalus, f);
						return node;
					}

					int m = 0;

					while (j > 0 && blockPathTypes == BlockPathTypes.OPEN) {
						j--;
						if (m++ >= this.mob.getMaxFallDistance()) {
							return null;
						}

						blockPathTypes = this.getBlockPathType(this.level, i, j, k, this.mob, this.entityWidth, this.entityHeight, this.entityDepth, false, false);
						f = this.mob.getPathfindingMalus(blockPathTypes);
						if (blockPathTypes != BlockPathTypes.OPEN && f >= 0.0F) {
							node = this.getNode(i, j, k);
							node.type = blockPathTypes;
							node.costMalus = Math.max(node.costMalus, f);
							break;
						}

						if (f < 0.0F) {
							return null;
						}
					}
				}

				return node;
			} else {
				if (j < this.mob.level.getSeaLevel() - 10 && node != null) {
					node.costMalus++;
				}

				return node;
			}
		}
	}

	@Override
	protected BlockPathTypes evaluateBlockPathType(BlockGetter blockGetter, boolean bl, boolean bl2, BlockPos blockPos, BlockPathTypes blockPathTypes) {
		if (blockPathTypes == BlockPathTypes.RAIL
			&& !(blockGetter.getBlockState(blockPos).getBlock() instanceof BaseRailBlock)
			&& !(blockGetter.getBlockState(blockPos.below()).getBlock() instanceof BaseRailBlock)) {
			blockPathTypes = BlockPathTypes.FENCE;
		}

		if (blockPathTypes == BlockPathTypes.DOOR_OPEN || blockPathTypes == BlockPathTypes.DOOR_WOOD_CLOSED || blockPathTypes == BlockPathTypes.DOOR_IRON_CLOSED) {
			blockPathTypes = BlockPathTypes.BLOCKED;
		}

		if (blockPathTypes == BlockPathTypes.LEAVES) {
			blockPathTypes = BlockPathTypes.BLOCKED;
		}

		return blockPathTypes;
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
			if (blockPathTypes == BlockPathTypes.OPEN && j >= 1) {
				Block block = blockGetter.getBlockState(new BlockPos(i, j - 1, k)).getBlock();
				BlockPathTypes blockPathTypes3 = getBlockPathTypeRaw(blockGetter, mutableBlockPos.set(i, j - 1, k));
				if (blockPathTypes3 != BlockPathTypes.WALKABLE && blockPathTypes3 != BlockPathTypes.OPEN && blockPathTypes3 != BlockPathTypes.LAVA) {
					blockPathTypes = BlockPathTypes.WALKABLE;
				} else {
					blockPathTypes = BlockPathTypes.OPEN;
				}

				if (blockPathTypes3 == BlockPathTypes.DAMAGE_FIRE || block == Blocks.MAGMA_BLOCK || block.is(BlockTags.CAMPFIRES)) {
					blockPathTypes = BlockPathTypes.DAMAGE_FIRE;
				}

				if (blockPathTypes3 == BlockPathTypes.DAMAGE_CACTUS) {
					blockPathTypes = BlockPathTypes.DAMAGE_CACTUS;
				}

				if (blockPathTypes3 == BlockPathTypes.DAMAGE_OTHER) {
					blockPathTypes = BlockPathTypes.DAMAGE_OTHER;
				}
			}

			if (blockPathTypes == BlockPathTypes.WALKABLE) {
				blockPathTypes = checkNeighbourBlocks(blockGetter, mutableBlockPos.set(i, j, k), blockPathTypes);
			}

			return blockPathTypes;
		}
	}
}
