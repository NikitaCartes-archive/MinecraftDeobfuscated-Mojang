package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WalkNodeEvaluator extends NodeEvaluator {
	protected float oldWaterCost;

	@Override
	public void prepare(LevelReader levelReader, Mob mob) {
		super.prepare(levelReader, mob);
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

			for (BlockState blockState = this.level.getBlockState(mutableBlockPos);
				blockState.getBlock() == Blocks.WATER || blockState.getFluidState() == Fluids.WATER.getSource(false);
				blockState = this.level.getBlockState(mutableBlockPos)
			) {
				mutableBlockPos.set(this.mob.x, (double)(++i), this.mob.z);
			}

			i--;
		} else if (this.mob.onGround) {
			i = Mth.floor(this.mob.getBoundingBox().minY + 0.5);
		} else {
			BlockPos blockPos = new BlockPos(this.mob);

			while (
				(this.level.getBlockState(blockPos).isAir() || this.level.getBlockState(blockPos).isPathfindable(this.level, blockPos, PathComputationType.LAND))
					&& blockPos.getY() > 0
			) {
				blockPos = blockPos.below();
			}

			i = blockPos.above().getY();
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
					return this.getNode(blockPos2.getX(), blockPos2.getY(), blockPos2.getZ());
				}
			}
		}

		return this.getNode(blockPos.getX(), i, blockPos.getZ());
	}

	@Override
	public Target getGoal(double d, double e, double f) {
		return new Target(this.getNode(Mth.floor(d), Mth.floor(e), Mth.floor(f)));
	}

	@Override
	public int getNeighbors(Node[] nodes, Node node) {
		int i = 0;
		int j = 0;
		BlockPathTypes blockPathTypes = this.getBlockPathType(this.mob, node.x, node.y + 1, node.z);
		if (this.mob.getPathfindingMalus(blockPathTypes) >= 0.0F) {
			j = Mth.floor(Math.max(1.0F, this.mob.maxUpStep));
		}

		double d = getFloorLevel(this.level, new BlockPos(node.x, node.y, node.z));
		Node node2 = this.getLandNode(node.x, node.y, node.z + 1, j, d, Direction.SOUTH);
		if (node2 != null && !node2.closed && node2.costMalus >= 0.0F) {
			nodes[i++] = node2;
		}

		Node node3 = this.getLandNode(node.x - 1, node.y, node.z, j, d, Direction.WEST);
		if (node3 != null && !node3.closed && node3.costMalus >= 0.0F) {
			nodes[i++] = node3;
		}

		Node node4 = this.getLandNode(node.x + 1, node.y, node.z, j, d, Direction.EAST);
		if (node4 != null && !node4.closed && node4.costMalus >= 0.0F) {
			nodes[i++] = node4;
		}

		Node node5 = this.getLandNode(node.x, node.y, node.z - 1, j, d, Direction.NORTH);
		if (node5 != null && !node5.closed && node5.costMalus >= 0.0F) {
			nodes[i++] = node5;
		}

		Node node6 = this.getLandNode(node.x - 1, node.y, node.z - 1, j, d, Direction.NORTH);
		if (this.isDiagonalValid(node, node3, node5, node6)) {
			nodes[i++] = node6;
		}

		Node node7 = this.getLandNode(node.x + 1, node.y, node.z - 1, j, d, Direction.NORTH);
		if (this.isDiagonalValid(node, node4, node5, node7)) {
			nodes[i++] = node7;
		}

		Node node8 = this.getLandNode(node.x - 1, node.y, node.z + 1, j, d, Direction.SOUTH);
		if (this.isDiagonalValid(node, node3, node2, node8)) {
			nodes[i++] = node8;
		}

		Node node9 = this.getLandNode(node.x + 1, node.y, node.z + 1, j, d, Direction.SOUTH);
		if (this.isDiagonalValid(node, node4, node2, node9)) {
			nodes[i++] = node9;
		}

		return i;
	}

	private boolean isDiagonalValid(Node node, @Nullable Node node2, @Nullable Node node3, @Nullable Node node4) {
		if (node4 == null || node3 == null || node2 == null) {
			return false;
		} else if (node4.closed) {
			return false;
		} else {
			return node3.y <= node.y && node2.y <= node.y
				? node4.costMalus >= 0.0F && (node3.y < node.y || node3.costMalus >= 0.0F) && (node2.y < node.y || node2.costMalus >= 0.0F)
				: false;
		}
	}

	public static double getFloorLevel(BlockGetter blockGetter, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		VoxelShape voxelShape = blockGetter.getBlockState(blockPos2).getCollisionShape(blockGetter, blockPos2);
		return (double)blockPos2.getY() + (voxelShape.isEmpty() ? 0.0 : voxelShape.max(Direction.Axis.Y));
	}

	@Nullable
	private Node getLandNode(int i, int j, int k, int l, double d, Direction direction) {
		Node node = null;
		BlockPos blockPos = new BlockPos(i, j, k);
		double e = getFloorLevel(this.level, blockPos);
		if (e - d > 1.125) {
			return null;
		} else {
			BlockPathTypes blockPathTypes = this.getBlockPathType(this.mob, i, j, k);
			float f = this.mob.getPathfindingMalus(blockPathTypes);
			double g = (double)this.mob.getBbWidth() / 2.0;
			if (f >= 0.0F) {
				node = this.getNode(i, j, k);
				node.type = blockPathTypes;
				node.costMalus = Math.max(node.costMalus, f);
			}

			if (blockPathTypes == BlockPathTypes.WALKABLE) {
				return node;
			} else {
				if ((node == null || node.costMalus < 0.0F) && l > 0 && blockPathTypes != BlockPathTypes.FENCE && blockPathTypes != BlockPathTypes.TRAPDOOR) {
					node = this.getLandNode(i, j + 1, k, l - 1, d, direction);
					if (node != null && (node.type == BlockPathTypes.OPEN || node.type == BlockPathTypes.WALKABLE) && this.mob.getBbWidth() < 1.0F) {
						double h = (double)(i - direction.getStepX()) + 0.5;
						double m = (double)(k - direction.getStepZ()) + 0.5;
						AABB aABB = new AABB(
							h - g,
							getFloorLevel(this.level, new BlockPos(h, (double)(j + 1), m)) + 0.001,
							m - g,
							h + g,
							(double)this.mob.getBbHeight() + getFloorLevel(this.level, new BlockPos(node.x, node.y, node.z)) - 0.002,
							m + g
						);
						if (!this.level.noCollision(this.mob, aABB)) {
							node = null;
						}
					}
				}

				if (blockPathTypes == BlockPathTypes.WATER && !this.canFloat()) {
					if (this.getBlockPathType(this.mob, i, j - 1, k) != BlockPathTypes.WATER) {
						return node;
					}

					while (j > 0) {
						blockPathTypes = this.getBlockPathType(this.mob, i, --j, k);
						if (blockPathTypes != BlockPathTypes.WATER) {
							return node;
						}

						node = this.getNode(i, j, k);
						node.type = blockPathTypes;
						node.costMalus = Math.max(node.costMalus, this.mob.getPathfindingMalus(blockPathTypes));
					}
				}

				if (blockPathTypes == BlockPathTypes.OPEN) {
					AABB aABB2 = new AABB(
						(double)i - g + 0.5, (double)j + 0.001, (double)k - g + 0.5, (double)i + g + 0.5, (double)((float)j + this.mob.getBbHeight()), (double)k + g + 0.5
					);
					if (!this.level.noCollision(this.mob, aABB2)) {
						return null;
					}

					if (this.mob.getBbWidth() >= 1.0F) {
						BlockPathTypes blockPathTypes2 = this.getBlockPathType(this.mob, i, j - 1, k);
						if (blockPathTypes2 == BlockPathTypes.BLOCKED) {
							node = this.getNode(i, j, k);
							node.type = BlockPathTypes.WALKABLE;
							node.costMalus = Math.max(node.costMalus, f);
							return node;
						}
					}

					int n = 0;
					int o = j;

					while (blockPathTypes == BlockPathTypes.OPEN) {
						if (--j < 0) {
							Node node2 = this.getNode(i, o, k);
							node2.type = BlockPathTypes.BLOCKED;
							node2.costMalus = -1.0F;
							return node2;
						}

						Node node2 = this.getNode(i, j, k);
						if (n++ >= this.mob.getMaxFallDistance()) {
							node2.type = BlockPathTypes.BLOCKED;
							node2.costMalus = -1.0F;
							return node2;
						}

						blockPathTypes = this.getBlockPathType(this.mob, i, j, k);
						f = this.mob.getPathfindingMalus(blockPathTypes);
						if (blockPathTypes != BlockPathTypes.OPEN && f >= 0.0F) {
							node = node2;
							node2.type = blockPathTypes;
							node2.costMalus = Math.max(node2.costMalus, f);
							break;
						}

						if (f < 0.0F) {
							node2.type = BlockPathTypes.BLOCKED;
							node2.costMalus = -1.0F;
							return node2;
						}
					}
				}

				return node;
			}
		}
	}

	@Override
	public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int j, int k, Mob mob, int l, int m, int n, boolean bl, boolean bl2) {
		EnumSet<BlockPathTypes> enumSet = EnumSet.noneOf(BlockPathTypes.class);
		BlockPathTypes blockPathTypes = BlockPathTypes.BLOCKED;
		double d = (double)mob.getBbWidth() / 2.0;
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

	public BlockPathTypes getBlockPathTypes(
		BlockGetter blockGetter,
		int i,
		int j,
		int k,
		int l,
		int m,
		int n,
		boolean bl,
		boolean bl2,
		EnumSet<BlockPathTypes> enumSet,
		BlockPathTypes blockPathTypes,
		BlockPos blockPos
	) {
		for (int o = 0; o < l; o++) {
			for (int p = 0; p < m; p++) {
				for (int q = 0; q < n; q++) {
					int r = o + i;
					int s = p + j;
					int t = q + k;
					BlockPathTypes blockPathTypes2 = this.getBlockPathType(blockGetter, r, s, t);
					blockPathTypes2 = this.evaluateBlockPathType(blockGetter, bl, bl2, blockPos, blockPathTypes2);
					if (o == 0 && p == 0 && q == 0) {
						blockPathTypes = blockPathTypes2;
					}

					enumSet.add(blockPathTypes2);
				}
			}
		}

		return blockPathTypes;
	}

	protected BlockPathTypes evaluateBlockPathType(BlockGetter blockGetter, boolean bl, boolean bl2, BlockPos blockPos, BlockPathTypes blockPathTypes) {
		if (blockPathTypes == BlockPathTypes.DOOR_WOOD_CLOSED && bl && bl2) {
			blockPathTypes = BlockPathTypes.WALKABLE;
		}

		if (blockPathTypes == BlockPathTypes.DOOR_OPEN && !bl2) {
			blockPathTypes = BlockPathTypes.BLOCKED;
		}

		if (blockPathTypes == BlockPathTypes.RAIL
			&& !(blockGetter.getBlockState(blockPos).getBlock() instanceof BaseRailBlock)
			&& !(blockGetter.getBlockState(blockPos.below()).getBlock() instanceof BaseRailBlock)) {
			blockPathTypes = BlockPathTypes.FENCE;
		}

		if (blockPathTypes == BlockPathTypes.LEAVES) {
			blockPathTypes = BlockPathTypes.BLOCKED;
		}

		return blockPathTypes;
	}

	private BlockPathTypes getBlockPathType(Mob mob, BlockPos blockPos) {
		return this.getBlockPathType(mob, blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	private BlockPathTypes getBlockPathType(Mob mob, int i, int j, int k) {
		return this.getBlockPathType(this.level, i, j, k, mob, this.entityWidth, this.entityHeight, this.entityDepth, this.canOpenDoors(), this.canPassDoors());
	}

	@Override
	public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int j, int k) {
		BlockPathTypes blockPathTypes = this.getBlockPathTypeRaw(blockGetter, i, j, k);
		if (blockPathTypes == BlockPathTypes.OPEN && j >= 1) {
			Block block = blockGetter.getBlockState(new BlockPos(i, j - 1, k)).getBlock();
			BlockPathTypes blockPathTypes2 = this.getBlockPathTypeRaw(blockGetter, i, j - 1, k);
			blockPathTypes = blockPathTypes2 != BlockPathTypes.WALKABLE
					&& blockPathTypes2 != BlockPathTypes.OPEN
					&& blockPathTypes2 != BlockPathTypes.WATER
					&& blockPathTypes2 != BlockPathTypes.LAVA
				? BlockPathTypes.WALKABLE
				: BlockPathTypes.OPEN;
			if (blockPathTypes2 == BlockPathTypes.DAMAGE_FIRE || block == Blocks.MAGMA_BLOCK || block == Blocks.CAMPFIRE) {
				blockPathTypes = BlockPathTypes.DAMAGE_FIRE;
			}

			if (blockPathTypes2 == BlockPathTypes.DAMAGE_CACTUS) {
				blockPathTypes = BlockPathTypes.DAMAGE_CACTUS;
			}

			if (blockPathTypes2 == BlockPathTypes.DAMAGE_OTHER) {
				blockPathTypes = BlockPathTypes.DAMAGE_OTHER;
			}
		}

		return this.checkNeighbourBlocks(blockGetter, i, j, k, blockPathTypes);
	}

	public BlockPathTypes checkNeighbourBlocks(BlockGetter blockGetter, int i, int j, int k, BlockPathTypes blockPathTypes) {
		if (blockPathTypes == BlockPathTypes.WALKABLE) {
			try (BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.acquire()) {
				for (int l = -1; l <= 1; l++) {
					for (int m = -1; m <= 1; m++) {
						if (l != 0 || m != 0) {
							Block block = blockGetter.getBlockState(pooledMutableBlockPos.set(l + i, j, m + k)).getBlock();
							if (block == Blocks.CACTUS) {
								blockPathTypes = BlockPathTypes.DANGER_CACTUS;
							} else if (block == Blocks.FIRE) {
								blockPathTypes = BlockPathTypes.DANGER_FIRE;
							} else if (block == Blocks.SWEET_BERRY_BUSH) {
								blockPathTypes = BlockPathTypes.DANGER_OTHER;
							}
						}
					}
				}
			}
		}

		return blockPathTypes;
	}

	protected BlockPathTypes getBlockPathTypeRaw(BlockGetter blockGetter, int i, int j, int k) {
		BlockPos blockPos = new BlockPos(i, j, k);
		BlockState blockState = blockGetter.getBlockState(blockPos);
		Block block = blockState.getBlock();
		Material material = blockState.getMaterial();
		if (blockState.isAir()) {
			return BlockPathTypes.OPEN;
		} else if (block.is(BlockTags.TRAPDOORS) || block == Blocks.LILY_PAD) {
			return BlockPathTypes.TRAPDOOR;
		} else if (block == Blocks.FIRE) {
			return BlockPathTypes.DAMAGE_FIRE;
		} else if (block == Blocks.CACTUS) {
			return BlockPathTypes.DAMAGE_CACTUS;
		} else if (block == Blocks.SWEET_BERRY_BUSH) {
			return BlockPathTypes.DAMAGE_OTHER;
		} else if (block instanceof DoorBlock && material == Material.WOOD && !(Boolean)blockState.getValue(DoorBlock.OPEN)) {
			return BlockPathTypes.DOOR_WOOD_CLOSED;
		} else if (block instanceof DoorBlock && material == Material.METAL && !(Boolean)blockState.getValue(DoorBlock.OPEN)) {
			return BlockPathTypes.DOOR_IRON_CLOSED;
		} else if (block instanceof DoorBlock && (Boolean)blockState.getValue(DoorBlock.OPEN)) {
			return BlockPathTypes.DOOR_OPEN;
		} else if (block instanceof BaseRailBlock) {
			return BlockPathTypes.RAIL;
		} else if (block instanceof LeavesBlock) {
			return BlockPathTypes.LEAVES;
		} else if (!block.is(BlockTags.FENCES)
			&& !block.is(BlockTags.WALLS)
			&& (!(block instanceof FenceGateBlock) || (Boolean)blockState.getValue(FenceGateBlock.OPEN))) {
			FluidState fluidState = blockGetter.getFluidState(blockPos);
			if (fluidState.is(FluidTags.WATER)) {
				return BlockPathTypes.WATER;
			} else if (fluidState.is(FluidTags.LAVA)) {
				return BlockPathTypes.LAVA;
			} else {
				return blockState.isPathfindable(blockGetter, blockPos, PathComputationType.LAND) ? BlockPathTypes.OPEN : BlockPathTypes.BLOCKED;
			}
		} else {
			return BlockPathTypes.FENCE;
		}
	}
}
