package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WalkNodeEvaluator extends NodeEvaluator {
	public static final double SPACE_BETWEEN_WALL_POSTS = 0.5;
	private static final double DEFAULT_MOB_JUMP_HEIGHT = 1.125;
	private final Long2ObjectMap<BlockPathTypes> pathTypesByPosCache = new Long2ObjectOpenHashMap<>();
	private final Object2BooleanMap<AABB> collisionCache = new Object2BooleanOpenHashMap<>();

	@Override
	public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
		super.prepare(pathNavigationRegion, mob);
		mob.onPathfindingStart();
	}

	@Override
	public void done() {
		this.mob.onPathfindingDone();
		this.pathTypesByPosCache.clear();
		this.collisionCache.clear();
		super.done();
	}

	@Override
	public Node getStart() {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int i = this.mob.getBlockY();
		BlockState blockState = this.level.getBlockState(mutableBlockPos.set(this.mob.getX(), (double)i, this.mob.getZ()));
		if (!this.mob.canStandOnFluid(blockState.getFluidState())) {
			if (this.canFloat() && this.mob.isInWater()) {
				while (true) {
					if (!blockState.is(Blocks.WATER) && blockState.getFluidState() != Fluids.WATER.getSource(false)) {
						i--;
						break;
					}

					blockState = this.level.getBlockState(mutableBlockPos.set(this.mob.getX(), (double)(++i), this.mob.getZ()));
				}
			} else if (this.mob.onGround()) {
				i = Mth.floor(this.mob.getY() + 0.5);
			} else {
				BlockPos blockPos = this.mob.blockPosition();

				while (
					(this.level.getBlockState(blockPos).isAir() || this.level.getBlockState(blockPos).isPathfindable(this.level, blockPos, PathComputationType.LAND))
						&& blockPos.getY() > this.mob.level().getMinBuildHeight()
				) {
					blockPos = blockPos.below();
				}

				i = blockPos.above().getY();
			}
		} else {
			while (this.mob.canStandOnFluid(blockState.getFluidState())) {
				blockState = this.level.getBlockState(mutableBlockPos.set(this.mob.getX(), (double)(++i), this.mob.getZ()));
			}

			i--;
		}

		BlockPos blockPos = this.mob.blockPosition();
		if (!this.canStartAt(mutableBlockPos.set(blockPos.getX(), i, blockPos.getZ()))) {
			AABB aABB = this.mob.getBoundingBox();
			if (this.canStartAt(mutableBlockPos.set(aABB.minX, (double)i, aABB.minZ))
				|| this.canStartAt(mutableBlockPos.set(aABB.minX, (double)i, aABB.maxZ))
				|| this.canStartAt(mutableBlockPos.set(aABB.maxX, (double)i, aABB.minZ))
				|| this.canStartAt(mutableBlockPos.set(aABB.maxX, (double)i, aABB.maxZ))) {
				return this.getStartNode(mutableBlockPos);
			}
		}

		return this.getStartNode(new BlockPos(blockPos.getX(), i, blockPos.getZ()));
	}

	protected Node getStartNode(BlockPos blockPos) {
		Node node = this.getNode(blockPos);
		node.type = this.getBlockPathType(this.mob, node.asBlockPos());
		node.costMalus = this.mob.getPathfindingMalus(node.type);
		return node;
	}

	protected boolean canStartAt(BlockPos blockPos) {
		BlockPathTypes blockPathTypes = this.getBlockPathType(this.mob, blockPos);
		return blockPathTypes != BlockPathTypes.OPEN && this.mob.getPathfindingMalus(blockPathTypes) >= 0.0F;
	}

	@Override
	public Target getGoal(double d, double e, double f) {
		return this.getTargetFromNode(this.getNode(Mth.floor(d), Mth.floor(e), Mth.floor(f)));
	}

	@Override
	public int getNeighbors(Node[] nodes, Node node) {
		int i = 0;
		int j = 0;
		BlockPathTypes blockPathTypes = this.getCachedBlockType(this.mob, node.x, node.y + 1, node.z);
		BlockPathTypes blockPathTypes2 = this.getCachedBlockType(this.mob, node.x, node.y, node.z);
		if (this.mob.getPathfindingMalus(blockPathTypes) >= 0.0F && blockPathTypes2 != BlockPathTypes.STICKY_HONEY) {
			j = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
		}

		double d = this.getFloorLevel(new BlockPos(node.x, node.y, node.z));
		Node node2 = this.findAcceptedNode(node.x, node.y, node.z + 1, j, d, Direction.SOUTH, blockPathTypes2);
		if (this.isNeighborValid(node2, node)) {
			nodes[i++] = node2;
		}

		Node node3 = this.findAcceptedNode(node.x - 1, node.y, node.z, j, d, Direction.WEST, blockPathTypes2);
		if (this.isNeighborValid(node3, node)) {
			nodes[i++] = node3;
		}

		Node node4 = this.findAcceptedNode(node.x + 1, node.y, node.z, j, d, Direction.EAST, blockPathTypes2);
		if (this.isNeighborValid(node4, node)) {
			nodes[i++] = node4;
		}

		Node node5 = this.findAcceptedNode(node.x, node.y, node.z - 1, j, d, Direction.NORTH, blockPathTypes2);
		if (this.isNeighborValid(node5, node)) {
			nodes[i++] = node5;
		}

		Node node6 = this.findAcceptedNode(node.x - 1, node.y, node.z - 1, j, d, Direction.NORTH, blockPathTypes2);
		if (this.isDiagonalValid(node, node3, node5, node6)) {
			nodes[i++] = node6;
		}

		Node node7 = this.findAcceptedNode(node.x + 1, node.y, node.z - 1, j, d, Direction.NORTH, blockPathTypes2);
		if (this.isDiagonalValid(node, node4, node5, node7)) {
			nodes[i++] = node7;
		}

		Node node8 = this.findAcceptedNode(node.x - 1, node.y, node.z + 1, j, d, Direction.SOUTH, blockPathTypes2);
		if (this.isDiagonalValid(node, node3, node2, node8)) {
			nodes[i++] = node8;
		}

		Node node9 = this.findAcceptedNode(node.x + 1, node.y, node.z + 1, j, d, Direction.SOUTH, blockPathTypes2);
		if (this.isDiagonalValid(node, node4, node2, node9)) {
			nodes[i++] = node9;
		}

		return i;
	}

	protected boolean isNeighborValid(@Nullable Node node, Node node2) {
		return node != null && !node.closed && (node.costMalus >= 0.0F || node2.costMalus < 0.0F);
	}

	protected boolean isDiagonalValid(Node node, @Nullable Node node2, @Nullable Node node3, @Nullable Node node4) {
		if (node4 == null || node3 == null || node2 == null) {
			return false;
		} else if (node4.closed) {
			return false;
		} else if (node3.y > node.y || node2.y > node.y) {
			return false;
		} else if (node2.type != BlockPathTypes.WALKABLE_DOOR && node3.type != BlockPathTypes.WALKABLE_DOOR && node4.type != BlockPathTypes.WALKABLE_DOOR) {
			boolean bl = node3.type == BlockPathTypes.FENCE && node2.type == BlockPathTypes.FENCE && (double)this.mob.getBbWidth() < 0.5;
			return node4.costMalus >= 0.0F && (node3.y < node.y || node3.costMalus >= 0.0F || bl) && (node2.y < node.y || node2.costMalus >= 0.0F || bl);
		} else {
			return false;
		}
	}

	private static boolean doesBlockHavePartialCollision(BlockPathTypes blockPathTypes) {
		return blockPathTypes == BlockPathTypes.FENCE || blockPathTypes == BlockPathTypes.DOOR_WOOD_CLOSED || blockPathTypes == BlockPathTypes.DOOR_IRON_CLOSED;
	}

	private boolean canReachWithoutCollision(Node node) {
		AABB aABB = this.mob.getBoundingBox();
		Vec3 vec3 = new Vec3(
			(double)node.x - this.mob.getX() + aABB.getXsize() / 2.0,
			(double)node.y - this.mob.getY() + aABB.getYsize() / 2.0,
			(double)node.z - this.mob.getZ() + aABB.getZsize() / 2.0
		);
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

	protected double getFloorLevel(BlockPos blockPos) {
		return (this.canFloat() || this.isAmphibious()) && this.level.getFluidState(blockPos).is(FluidTags.WATER)
			? (double)blockPos.getY() + 0.5
			: getFloorLevel(this.level, blockPos);
	}

	public static double getFloorLevel(BlockGetter blockGetter, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		VoxelShape voxelShape = blockGetter.getBlockState(blockPos2).getCollisionShape(blockGetter, blockPos2);
		return (double)blockPos2.getY() + (voxelShape.isEmpty() ? 0.0 : voxelShape.max(Direction.Axis.Y));
	}

	protected boolean isAmphibious() {
		return false;
	}

	@Nullable
	protected Node findAcceptedNode(int i, int j, int k, int l, double d, Direction direction, BlockPathTypes blockPathTypes) {
		Node node = null;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		double e = this.getFloorLevel(mutableBlockPos.set(i, j, k));
		if (e - d > this.getMobJumpHeight()) {
			return null;
		} else {
			BlockPathTypes blockPathTypes2 = this.getCachedBlockType(this.mob, i, j, k);
			float f = this.mob.getPathfindingMalus(blockPathTypes2);
			double g = (double)this.mob.getBbWidth() / 2.0;
			if (f >= 0.0F) {
				node = this.getNodeAndUpdateCostToMax(i, j, k, blockPathTypes2, f);
			}

			if (doesBlockHavePartialCollision(blockPathTypes) && node != null && node.costMalus >= 0.0F && !this.canReachWithoutCollision(node)) {
				node = null;
			}

			if (blockPathTypes2 != BlockPathTypes.WALKABLE && (!this.isAmphibious() || blockPathTypes2 != BlockPathTypes.WATER)) {
				if ((node == null || node.costMalus < 0.0F)
					&& l > 0
					&& (blockPathTypes2 != BlockPathTypes.FENCE || this.canWalkOverFences())
					&& blockPathTypes2 != BlockPathTypes.UNPASSABLE_RAIL
					&& blockPathTypes2 != BlockPathTypes.TRAPDOOR
					&& blockPathTypes2 != BlockPathTypes.POWDER_SNOW) {
					node = this.findAcceptedNode(i, j + 1, k, l - 1, d, direction, blockPathTypes);
					if (node != null && (node.type == BlockPathTypes.OPEN || node.type == BlockPathTypes.WALKABLE) && this.mob.getBbWidth() < 1.0F) {
						double h = (double)(i - direction.getStepX()) + 0.5;
						double m = (double)(k - direction.getStepZ()) + 0.5;
						AABB aABB = new AABB(
							h - g,
							this.getFloorLevel(mutableBlockPos.set(h, (double)(j + 1), m)) + 0.001,
							m - g,
							h + g,
							(double)this.mob.getBbHeight() + this.getFloorLevel(mutableBlockPos.set((double)node.x, (double)node.y, (double)node.z)) - 0.002,
							m + g
						);
						if (this.hasCollisions(aABB)) {
							node = null;
						}
					}
				}

				if (!this.isAmphibious() && blockPathTypes2 == BlockPathTypes.WATER && !this.canFloat()) {
					if (this.getCachedBlockType(this.mob, i, j - 1, k) != BlockPathTypes.WATER) {
						return node;
					}

					while (j > this.mob.level().getMinBuildHeight()) {
						blockPathTypes2 = this.getCachedBlockType(this.mob, i, --j, k);
						if (blockPathTypes2 != BlockPathTypes.WATER) {
							return node;
						}

						node = this.getNodeAndUpdateCostToMax(i, j, k, blockPathTypes2, this.mob.getPathfindingMalus(blockPathTypes2));
					}
				}

				if (blockPathTypes2 == BlockPathTypes.OPEN) {
					int n = 0;
					int o = j;

					while (blockPathTypes2 == BlockPathTypes.OPEN) {
						if (--j < this.mob.level().getMinBuildHeight()) {
							return this.getBlockedNode(i, o, k);
						}

						if (n++ >= this.mob.getMaxFallDistance()) {
							return this.getBlockedNode(i, j, k);
						}

						blockPathTypes2 = this.getCachedBlockType(this.mob, i, j, k);
						f = this.mob.getPathfindingMalus(blockPathTypes2);
						if (blockPathTypes2 != BlockPathTypes.OPEN && f >= 0.0F) {
							node = this.getNodeAndUpdateCostToMax(i, j, k, blockPathTypes2, f);
							break;
						}

						if (f < 0.0F) {
							return this.getBlockedNode(i, j, k);
						}
					}
				}

				if (doesBlockHavePartialCollision(blockPathTypes2) && node == null) {
					node = this.getNode(i, j, k);
					node.closed = true;
					node.type = blockPathTypes2;
					node.costMalus = blockPathTypes2.getMalus();
				}

				return node;
			} else {
				return node;
			}
		}
	}

	private double getMobJumpHeight() {
		return Math.max(1.125, (double)this.mob.maxUpStep());
	}

	private Node getNodeAndUpdateCostToMax(int i, int j, int k, BlockPathTypes blockPathTypes, float f) {
		Node node = this.getNode(i, j, k);
		node.type = blockPathTypes;
		node.costMalus = Math.max(node.costMalus, f);
		return node;
	}

	private Node getBlockedNode(int i, int j, int k) {
		Node node = this.getNode(i, j, k);
		node.type = BlockPathTypes.BLOCKED;
		node.costMalus = -1.0F;
		return node;
	}

	private boolean hasCollisions(AABB aABB) {
		return this.collisionCache.computeIfAbsent(aABB, (Object2BooleanFunction<? super AABB>)(object -> !this.level.noCollision(this.mob, aABB)));
	}

	@Override
	public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int j, int k, Mob mob) {
		EnumSet<BlockPathTypes> enumSet = EnumSet.noneOf(BlockPathTypes.class);
		BlockPathTypes blockPathTypes = BlockPathTypes.BLOCKED;
		blockPathTypes = this.getBlockPathTypes(blockGetter, i, j, k, enumSet, blockPathTypes, mob.blockPosition());
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

			return blockPathTypes == BlockPathTypes.OPEN && mob.getPathfindingMalus(blockPathTypes2) == 0.0F && this.entityWidth <= 1
				? BlockPathTypes.OPEN
				: blockPathTypes2;
		}
	}

	public BlockPathTypes getBlockPathTypes(
		BlockGetter blockGetter, int i, int j, int k, EnumSet<BlockPathTypes> enumSet, BlockPathTypes blockPathTypes, BlockPos blockPos
	) {
		for (int l = 0; l < this.entityWidth; l++) {
			for (int m = 0; m < this.entityHeight; m++) {
				for (int n = 0; n < this.entityDepth; n++) {
					int o = l + i;
					int p = m + j;
					int q = n + k;
					BlockPathTypes blockPathTypes2 = this.getBlockPathType(blockGetter, o, p, q);
					blockPathTypes2 = this.evaluateBlockPathType(blockGetter, blockPos, blockPathTypes2);
					if (l == 0 && m == 0 && n == 0) {
						blockPathTypes = blockPathTypes2;
					}

					enumSet.add(blockPathTypes2);
				}
			}
		}

		return blockPathTypes;
	}

	protected BlockPathTypes evaluateBlockPathType(BlockGetter blockGetter, BlockPos blockPos, BlockPathTypes blockPathTypes) {
		boolean bl = this.canPassDoors();
		if (blockPathTypes == BlockPathTypes.DOOR_WOOD_CLOSED && this.canOpenDoors() && bl) {
			blockPathTypes = BlockPathTypes.WALKABLE_DOOR;
		}

		if (blockPathTypes == BlockPathTypes.DOOR_OPEN && !bl) {
			blockPathTypes = BlockPathTypes.BLOCKED;
		}

		if (blockPathTypes == BlockPathTypes.RAIL
			&& !(blockGetter.getBlockState(blockPos).getBlock() instanceof BaseRailBlock)
			&& !(blockGetter.getBlockState(blockPos.below()).getBlock() instanceof BaseRailBlock)) {
			blockPathTypes = BlockPathTypes.UNPASSABLE_RAIL;
		}

		return blockPathTypes;
	}

	protected BlockPathTypes getBlockPathType(Mob mob, BlockPos blockPos) {
		return this.getCachedBlockType(mob, blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	protected BlockPathTypes getCachedBlockType(Mob mob, int i, int j, int k) {
		return this.pathTypesByPosCache
			.computeIfAbsent(BlockPos.asLong(i, j, k), (Long2ObjectFunction<? extends BlockPathTypes>)(l -> this.getBlockPathType(this.level, i, j, k, mob)));
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
		if (blockPathTypes == BlockPathTypes.OPEN && j >= blockGetter.getMinBuildHeight() + 1) {
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

			if (blockPathTypes2 == BlockPathTypes.DAMAGE_OTHER) {
				blockPathTypes = BlockPathTypes.DAMAGE_OTHER;
			}

			if (blockPathTypes2 == BlockPathTypes.STICKY_HONEY) {
				blockPathTypes = BlockPathTypes.STICKY_HONEY;
			}

			if (blockPathTypes2 == BlockPathTypes.POWDER_SNOW) {
				blockPathTypes = BlockPathTypes.DANGER_POWDER_SNOW;
			}

			if (blockPathTypes2 == BlockPathTypes.DAMAGE_CAUTIOUS) {
				blockPathTypes = BlockPathTypes.DAMAGE_CAUTIOUS;
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
						if (blockState.is(Blocks.CACTUS) || blockState.is(Blocks.SWEET_BERRY_BUSH)) {
							return BlockPathTypes.DANGER_OTHER;
						}

						if (isBurningBlock(blockState)) {
							return BlockPathTypes.DANGER_FIRE;
						}

						if (blockGetter.getFluidState(mutableBlockPos).is(FluidTags.WATER)) {
							return BlockPathTypes.WATER_BORDER;
						}

						if (blockState.is(Blocks.WITHER_ROSE) || blockState.is(Blocks.POINTED_DRIPSTONE)) {
							return BlockPathTypes.DAMAGE_CAUTIOUS;
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
		if (blockState.isAir()) {
			return BlockPathTypes.OPEN;
		} else if (blockState.is(BlockTags.TRAPDOORS) || blockState.is(Blocks.LILY_PAD) || blockState.is(Blocks.BIG_DRIPLEAF)) {
			return BlockPathTypes.TRAPDOOR;
		} else if (blockState.is(Blocks.POWDER_SNOW)) {
			return BlockPathTypes.POWDER_SNOW;
		} else if (blockState.is(Blocks.CACTUS) || blockState.is(Blocks.SWEET_BERRY_BUSH)) {
			return BlockPathTypes.DAMAGE_OTHER;
		} else if (blockState.is(Blocks.HONEY_BLOCK)) {
			return BlockPathTypes.STICKY_HONEY;
		} else if (blockState.is(Blocks.COCOA)) {
			return BlockPathTypes.COCOA;
		} else if (!blockState.is(Blocks.WITHER_ROSE) && !blockState.is(Blocks.POINTED_DRIPSTONE)) {
			FluidState fluidState = blockGetter.getFluidState(blockPos);
			if (fluidState.is(FluidTags.LAVA)) {
				return BlockPathTypes.LAVA;
			} else if (isBurningBlock(blockState)) {
				return BlockPathTypes.DAMAGE_FIRE;
			} else if (block instanceof DoorBlock doorBlock) {
				if ((Boolean)blockState.getValue(DoorBlock.OPEN)) {
					return BlockPathTypes.DOOR_OPEN;
				} else {
					return doorBlock.type().canOpenByHand() ? BlockPathTypes.DOOR_WOOD_CLOSED : BlockPathTypes.DOOR_IRON_CLOSED;
				}
			} else if (block instanceof BaseRailBlock) {
				return BlockPathTypes.RAIL;
			} else if (block instanceof LeavesBlock) {
				return BlockPathTypes.LEAVES;
			} else if (!blockState.is(BlockTags.FENCES)
				&& !blockState.is(BlockTags.WALLS)
				&& (!(block instanceof FenceGateBlock) || (Boolean)blockState.getValue(FenceGateBlock.OPEN))) {
				if (!blockState.isPathfindable(blockGetter, blockPos, PathComputationType.LAND)) {
					return BlockPathTypes.BLOCKED;
				} else {
					return fluidState.is(FluidTags.WATER) ? BlockPathTypes.WATER : BlockPathTypes.OPEN;
				}
			} else {
				return BlockPathTypes.FENCE;
			}
		} else {
			return BlockPathTypes.DAMAGE_CAUTIOUS;
		}
	}

	public static boolean isBurningBlock(BlockState blockState) {
		return blockState.is(BlockTags.FIRE)
			|| blockState.is(Blocks.LAVA)
			|| blockState.is(Blocks.MAGMA_BLOCK)
			|| CampfireBlock.isLitCampfire(blockState)
			|| blockState.is(Blocks.LAVA_CAULDRON);
	}
}
