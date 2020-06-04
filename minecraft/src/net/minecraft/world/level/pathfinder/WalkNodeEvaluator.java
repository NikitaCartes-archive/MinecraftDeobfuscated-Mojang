package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WalkNodeEvaluator extends NodeEvaluator {
	protected float oldWaterCost;
	private final Long2ObjectMap<BlockPathTypes> pathTypesByPosCache = new Long2ObjectOpenHashMap<>();
	private final Object2BooleanMap<AABB> collisionCache = new Object2BooleanOpenHashMap<>();

	@Override
	public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
		super.prepare(pathNavigationRegion, mob);
		this.oldWaterCost = mob.getPathfindingMalus(BlockPathTypes.WATER);
	}

	@Override
	public void done() {
		this.mob.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
		this.pathTypesByPosCache.clear();
		this.collisionCache.clear();
		super.done();
	}

	@Override
	public Node getStart() {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int i = Mth.floor(this.mob.getY());
		BlockState blockState = this.level.getBlockState(mutableBlockPos.set(this.mob.getX(), (double)i, this.mob.getZ()));
		if (!this.mob.canStandOnFluid(blockState.getFluidState().getType())) {
			if (this.canFloat() && this.mob.isInWater()) {
				while (true) {
					if (blockState.getBlock() != Blocks.WATER && blockState.getFluidState() != Fluids.WATER.getSource(false)) {
						i--;
						break;
					}

					blockState = this.level.getBlockState(mutableBlockPos.set(this.mob.getX(), (double)(++i), this.mob.getZ()));
				}
			} else if (this.mob.isOnGround()) {
				i = Mth.floor(this.mob.getY() + 0.5);
			} else {
				BlockPos blockPos = this.mob.blockPosition();

				while (
					(this.level.getBlockState(blockPos).isAir() || this.level.getBlockState(blockPos).isPathfindable(this.level, blockPos, PathComputationType.LAND))
						&& blockPos.getY() > 0
				) {
					blockPos = blockPos.below();
				}

				i = blockPos.above().getY();
			}
		} else {
			while (this.mob.canStandOnFluid(blockState.getFluidState().getType())) {
				blockState = this.level.getBlockState(mutableBlockPos.set(this.mob.getX(), (double)(++i), this.mob.getZ()));
			}

			i--;
		}

		BlockPos blockPos = this.mob.blockPosition();
		BlockPathTypes blockPathTypes = this.getCachedBlockType(this.mob, blockPos.getX(), i, blockPos.getZ());
		if (this.mob.getPathfindingMalus(blockPathTypes) < 0.0F) {
			AABB aABB = this.mob.getBoundingBox();
			if (this.hasPositiveMalus(mutableBlockPos.set(aABB.minX, (double)i, aABB.minZ))
				|| this.hasPositiveMalus(mutableBlockPos.set(aABB.minX, (double)i, aABB.maxZ))
				|| this.hasPositiveMalus(mutableBlockPos.set(aABB.maxX, (double)i, aABB.minZ))
				|| this.hasPositiveMalus(mutableBlockPos.set(aABB.maxX, (double)i, aABB.maxZ))) {
				Node node = this.getNode(mutableBlockPos);
				node.type = this.getBlockPathType(this.mob, node.asBlockPos());
				node.costMalus = this.mob.getPathfindingMalus(node.type);
				return node;
			}
		}

		Node node2 = this.getNode(blockPos.getX(), i, blockPos.getZ());
		node2.type = this.getBlockPathType(this.mob, node2.asBlockPos());
		node2.costMalus = this.mob.getPathfindingMalus(node2.type);
		return node2;
	}

	private boolean hasPositiveMalus(BlockPos blockPos) {
		BlockPathTypes blockPathTypes = this.getBlockPathType(this.mob, blockPos);
		return this.mob.getPathfindingMalus(blockPathTypes) >= 0.0F;
	}

	@Override
	public Target getGoal(double d, double e, double f) {
		return new Target(this.getNode(Mth.floor(d), Mth.floor(e), Mth.floor(f)));
	}

	@Override
	public int getNeighbors(Node[] nodes, Node node) {
		int i = 0;
		int j = 0;
		BlockPathTypes blockPathTypes = this.getCachedBlockType(this.mob, node.x, node.y + 1, node.z);
		BlockPathTypes blockPathTypes2 = this.getCachedBlockType(this.mob, node.x, node.y, node.z);
		if (this.mob.getPathfindingMalus(blockPathTypes) >= 0.0F && blockPathTypes2 != BlockPathTypes.STICKY_HONEY) {
			j = Mth.floor(Math.max(1.0F, this.mob.maxUpStep));
		}

		double d = getFloorLevel(this.level, new BlockPos(node.x, node.y, node.z));
		Node node2 = this.getLandNode(node.x, node.y, node.z + 1, j, d, Direction.SOUTH, blockPathTypes2);
		if (this.isNeighborValid(node2, node)) {
			nodes[i++] = node2;
		}

		Node node3 = this.getLandNode(node.x - 1, node.y, node.z, j, d, Direction.WEST, blockPathTypes2);
		if (this.isNeighborValid(node3, node)) {
			nodes[i++] = node3;
		}

		Node node4 = this.getLandNode(node.x + 1, node.y, node.z, j, d, Direction.EAST, blockPathTypes2);
		if (this.isNeighborValid(node4, node)) {
			nodes[i++] = node4;
		}

		Node node5 = this.getLandNode(node.x, node.y, node.z - 1, j, d, Direction.NORTH, blockPathTypes2);
		if (this.isNeighborValid(node5, node)) {
			nodes[i++] = node5;
		}

		Node node6 = this.getLandNode(node.x - 1, node.y, node.z - 1, j, d, Direction.NORTH, blockPathTypes2);
		if (this.isDiagonalValid(node, node3, node5, node6)) {
			nodes[i++] = node6;
		}

		Node node7 = this.getLandNode(node.x + 1, node.y, node.z - 1, j, d, Direction.NORTH, blockPathTypes2);
		if (this.isDiagonalValid(node, node4, node5, node7)) {
			nodes[i++] = node7;
		}

		Node node8 = this.getLandNode(node.x - 1, node.y, node.z + 1, j, d, Direction.SOUTH, blockPathTypes2);
		if (this.isDiagonalValid(node, node3, node2, node8)) {
			nodes[i++] = node8;
		}

		Node node9 = this.getLandNode(node.x + 1, node.y, node.z + 1, j, d, Direction.SOUTH, blockPathTypes2);
		if (this.isDiagonalValid(node, node4, node2, node9)) {
			nodes[i++] = node9;
		}

		return i;
	}

	private boolean isNeighborValid(Node node, Node node2) {
		return node != null && !node.closed && (node.costMalus >= 0.0F || node2.costMalus < 0.0F);
	}

	private boolean isDiagonalValid(Node node, @Nullable Node node2, @Nullable Node node3, @Nullable Node node4) {
		if (node4 == null || node3 == null || node2 == null) {
			return false;
		} else if (node4.closed) {
			return false;
		} else if (node3.y <= node.y && node2.y <= node.y) {
			boolean bl = node3.type == BlockPathTypes.FENCE && node2.type == BlockPathTypes.FENCE && (double)this.mob.getBbWidth() < 0.5;
			return node4.costMalus >= 0.0F && (node3.y < node.y || node3.costMalus >= 0.0F || bl) && (node2.y < node.y || node2.costMalus >= 0.0F || bl);
		} else {
			return false;
		}
	}

	private boolean canReachWithoutCollision(Node node) {
		Vec3 vec3 = new Vec3((double)node.x - this.mob.getX(), (double)node.y - this.mob.getY(), (double)node.z - this.mob.getZ());
		AABB aABB = this.mob.getBoundingBox();
		int i = Mth.ceil(vec3.length() / aABB.getSize());
		vec3 = vec3.scale((double)(1.0F / (float)i));

		for (int j = 1; j <= i; j++) {
			aABB = aABB.move(vec3);
			if (this.hasCollisions(aABB)) {
				return false;
			}
		}

		return true;
	}

	public static double getFloorLevel(BlockGetter blockGetter, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		VoxelShape voxelShape = blockGetter.getBlockState(blockPos2).getCollisionShape(blockGetter, blockPos2);
		return (double)blockPos2.getY() + (voxelShape.isEmpty() ? 0.0 : voxelShape.max(Direction.Axis.Y));
	}

	@Nullable
	private Node getLandNode(int i, int j, int k, int l, double d, Direction direction, BlockPathTypes blockPathTypes) {
		Node node = null;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		double e = getFloorLevel(this.level, mutableBlockPos.set(i, j, k));
		if (e - d > 1.125) {
			return null;
		} else {
			BlockPathTypes blockPathTypes2 = this.getCachedBlockType(this.mob, i, j, k);
			float f = this.mob.getPathfindingMalus(blockPathTypes2);
			double g = (double)this.mob.getBbWidth() / 2.0;
			if (f >= 0.0F) {
				node = this.getNode(i, j, k);
				node.type = blockPathTypes2;
				node.costMalus = Math.max(node.costMalus, f);
			}

			if (blockPathTypes == BlockPathTypes.FENCE && node != null && node.costMalus >= 0.0F && !this.canReachWithoutCollision(node)) {
				node = null;
			}

			if (blockPathTypes2 == BlockPathTypes.WALKABLE) {
				return node;
			} else {
				if ((node == null || node.costMalus < 0.0F)
					&& l > 0
					&& blockPathTypes2 != BlockPathTypes.FENCE
					&& blockPathTypes2 != BlockPathTypes.UNPASSABLE_RAIL
					&& blockPathTypes2 != BlockPathTypes.TRAPDOOR) {
					node = this.getLandNode(i, j + 1, k, l - 1, d, direction, blockPathTypes);
					if (node != null && (node.type == BlockPathTypes.OPEN || node.type == BlockPathTypes.WALKABLE) && this.mob.getBbWidth() < 1.0F) {
						double h = (double)(i - direction.getStepX()) + 0.5;
						double m = (double)(k - direction.getStepZ()) + 0.5;
						AABB aABB = new AABB(
							h - g,
							getFloorLevel(this.level, mutableBlockPos.set(h, (double)(j + 1), m)) + 0.001,
							m - g,
							h + g,
							(double)this.mob.getBbHeight() + getFloorLevel(this.level, mutableBlockPos.set((double)node.x, (double)node.y, (double)node.z)) - 0.002,
							m + g
						);
						if (this.hasCollisions(aABB)) {
							node = null;
						}
					}
				}

				if (blockPathTypes2 == BlockPathTypes.WATER && !this.canFloat()) {
					if (this.getCachedBlockType(this.mob, i, j - 1, k) != BlockPathTypes.WATER) {
						return node;
					}

					while (j > 0) {
						blockPathTypes2 = this.getCachedBlockType(this.mob, i, --j, k);
						if (blockPathTypes2 != BlockPathTypes.WATER) {
							return node;
						}

						node = this.getNode(i, j, k);
						node.type = blockPathTypes2;
						node.costMalus = Math.max(node.costMalus, this.mob.getPathfindingMalus(blockPathTypes2));
					}
				}

				if (blockPathTypes2 == BlockPathTypes.OPEN) {
					AABB aABB2 = new AABB(
						(double)i - g + 0.5, (double)j + 0.001, (double)k - g + 0.5, (double)i + g + 0.5, (double)((float)j + this.mob.getBbHeight()), (double)k + g + 0.5
					);
					if (this.hasCollisions(aABB2)) {
						return null;
					}

					if (this.mob.getBbWidth() >= 1.0F) {
						BlockPathTypes blockPathTypes3 = this.getCachedBlockType(this.mob, i, j - 1, k);
						if (blockPathTypes3 == BlockPathTypes.BLOCKED) {
							node = this.getNode(i, j, k);
							node.type = BlockPathTypes.WALKABLE;
							node.costMalus = Math.max(node.costMalus, f);
							return node;
						}
					}

					int n = 0;
					int o = j;

					while (blockPathTypes2 == BlockPathTypes.OPEN) {
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

						blockPathTypes2 = this.getCachedBlockType(this.mob, i, j, k);
						f = this.mob.getPathfindingMalus(blockPathTypes2);
						if (blockPathTypes2 != BlockPathTypes.OPEN && f >= 0.0F) {
							node = node2;
							node2.type = blockPathTypes2;
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

				if (blockPathTypes2 == BlockPathTypes.FENCE) {
					node = this.getNode(i, j, k);
					node.closed = true;
					node.type = blockPathTypes2;
					node.costMalus = blockPathTypes2.getMalus();
				}

				return node;
			}
		}
	}

	private boolean hasCollisions(AABB aABB) {
		return (Boolean)this.collisionCache.computeIfAbsent(aABB, aABB2 -> !this.level.noCollision(this.mob, aABB));
	}

	@Override
	public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int j, int k, Mob mob, int l, int m, int n, boolean bl, boolean bl2) {
		EnumSet<BlockPathTypes> enumSet = EnumSet.noneOf(BlockPathTypes.class);
		BlockPathTypes blockPathTypes = BlockPathTypes.BLOCKED;
		BlockPos blockPos = mob.blockPosition();
		blockPathTypes = this.getBlockPathTypes(blockGetter, i, j, k, l, m, n, bl, bl2, enumSet, blockPathTypes, blockPos);
		if (enumSet.contains(BlockPathTypes.FENCE)) {
			return BlockPathTypes.FENCE;
		} else if (enumSet.contains(BlockPathTypes.UNPASSABLE_RAIL)) {
			return BlockPathTypes.UNPASSABLE_RAIL;
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
			blockPathTypes = BlockPathTypes.UNPASSABLE_RAIL;
		}

		if (blockPathTypes == BlockPathTypes.LEAVES) {
			blockPathTypes = BlockPathTypes.BLOCKED;
		}

		return blockPathTypes;
	}

	private BlockPathTypes getBlockPathType(Mob mob, BlockPos blockPos) {
		return this.getCachedBlockType(mob, blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	private BlockPathTypes getCachedBlockType(Mob mob, int i, int j, int k) {
		return this.pathTypesByPosCache
			.computeIfAbsent(
				BlockPos.asLong(i, j, k),
				l -> this.getBlockPathType(this.level, i, j, k, mob, this.entityWidth, this.entityHeight, this.entityDepth, this.canOpenDoors(), this.canPassDoors())
			);
	}

	@Override
	public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int j, int k) {
		return getBlockPathTypeStatic(blockGetter, new BlockPos.MutableBlockPos(i, j, k));
	}

	public static BlockPathTypes getBlockPathTypeStatic(BlockGetter blockGetter, BlockPos.MutableBlockPos mutableBlockPos) {
		int i = mutableBlockPos.getX();
		int j = mutableBlockPos.getY();
		int k = mutableBlockPos.getZ();
		BlockPathTypes blockPathTypes = getBlockPathTypeRaw(blockGetter, mutableBlockPos);
		if (blockPathTypes == BlockPathTypes.OPEN && j >= 1) {
			BlockPathTypes blockPathTypes2 = getBlockPathTypeRaw(blockGetter, mutableBlockPos.set(i, j - 1, k));
			blockPathTypes = blockPathTypes2 != BlockPathTypes.WALKABLE
					&& blockPathTypes2 != BlockPathTypes.OPEN
					&& blockPathTypes2 != BlockPathTypes.WATER
					&& blockPathTypes2 != BlockPathTypes.LAVA
				? BlockPathTypes.WALKABLE
				: BlockPathTypes.OPEN;
			if (blockPathTypes2 == BlockPathTypes.DAMAGE_FIRE) {
				blockPathTypes = BlockPathTypes.DAMAGE_FIRE;
			}

			if (blockPathTypes2 == BlockPathTypes.DAMAGE_CACTUS) {
				blockPathTypes = BlockPathTypes.DAMAGE_CACTUS;
			}

			if (blockPathTypes2 == BlockPathTypes.DAMAGE_OTHER) {
				blockPathTypes = BlockPathTypes.DAMAGE_OTHER;
			}

			if (blockPathTypes2 == BlockPathTypes.STICKY_HONEY) {
				blockPathTypes = BlockPathTypes.STICKY_HONEY;
			}
		}

		if (blockPathTypes == BlockPathTypes.WALKABLE) {
			blockPathTypes = checkNeighbourBlocks(blockGetter, mutableBlockPos.set(i, j, k), blockPathTypes);
		}

		return blockPathTypes;
	}

	public static BlockPathTypes checkNeighbourBlocks(BlockGetter blockGetter, BlockPos.MutableBlockPos mutableBlockPos, BlockPathTypes blockPathTypes) {
		int i = mutableBlockPos.getX();
		int j = mutableBlockPos.getY();
		int k = mutableBlockPos.getZ();

		for (int l = -1; l <= 1; l++) {
			for (int m = -1; m <= 1; m++) {
				for (int n = -1; n <= 1; n++) {
					if (l != 0 || n != 0) {
						mutableBlockPos.set(i + l, j + m, k + n);
						BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
						if (blockState.is(Blocks.CACTUS)) {
							return BlockPathTypes.DANGER_CACTUS;
						}

						if (blockState.is(Blocks.SWEET_BERRY_BUSH)) {
							return BlockPathTypes.DANGER_OTHER;
						}

						if (isBurningBlock(blockState)) {
							return BlockPathTypes.DANGER_FIRE;
						}
					}
				}
			}
		}

		return blockPathTypes;
	}

	protected static BlockPathTypes getBlockPathTypeRaw(BlockGetter blockGetter, BlockPos blockPos) {
		BlockState blockState = blockGetter.getBlockState(blockPos);
		Block block = blockState.getBlock();
		Material material = blockState.getMaterial();
		if (blockState.isAir()) {
			return BlockPathTypes.OPEN;
		} else if (blockState.is(BlockTags.TRAPDOORS) || blockState.is(Blocks.LILY_PAD)) {
			return BlockPathTypes.TRAPDOOR;
		} else if (blockState.is(Blocks.CACTUS)) {
			return BlockPathTypes.DAMAGE_CACTUS;
		} else if (blockState.is(Blocks.SWEET_BERRY_BUSH)) {
			return BlockPathTypes.DAMAGE_OTHER;
		} else if (blockState.is(Blocks.HONEY_BLOCK)) {
			return BlockPathTypes.STICKY_HONEY;
		} else if (blockState.is(Blocks.COCOA)) {
			return BlockPathTypes.COCOA;
		} else if (isBurningBlock(blockState)) {
			return BlockPathTypes.DAMAGE_FIRE;
		} else if (DoorBlock.isWoodenDoor(blockState) && !(Boolean)blockState.getValue(DoorBlock.OPEN)) {
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
			if (!blockState.isPathfindable(blockGetter, blockPos, PathComputationType.LAND)) {
				return BlockPathTypes.BLOCKED;
			} else {
				FluidState fluidState = blockGetter.getFluidState(blockPos);
				if (fluidState.is(FluidTags.WATER)) {
					return BlockPathTypes.WATER;
				} else {
					return fluidState.is(FluidTags.LAVA) ? BlockPathTypes.LAVA : BlockPathTypes.OPEN;
				}
			}
		} else {
			return BlockPathTypes.FENCE;
		}
	}

	private static boolean isBurningBlock(BlockState blockState) {
		return blockState.is(BlockTags.FIRE) || blockState.is(Blocks.LAVA) || blockState.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(blockState);
	}
}
