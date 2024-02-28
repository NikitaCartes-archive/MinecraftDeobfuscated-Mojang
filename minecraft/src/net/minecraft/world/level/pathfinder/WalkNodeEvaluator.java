package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
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
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
	private final Long2ObjectMap<PathType> pathTypesByPosCacheByMob = new Long2ObjectOpenHashMap<>();
	private final Object2BooleanMap<AABB> collisionCache = new Object2BooleanOpenHashMap<>();
	private final Node[] reusableNeighbors = new Node[Direction.Plane.HORIZONTAL.length()];

	@Override
	public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
		super.prepare(pathNavigationRegion, mob);
		mob.onPathfindingStart();
	}

	@Override
	public void done() {
		this.mob.onPathfindingDone();
		this.pathTypesByPosCacheByMob.clear();
		this.collisionCache.clear();
		super.done();
	}

	@Override
	public Node getStart() {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int i = this.mob.getBlockY();
		BlockState blockState = this.currentContext.getBlockState(mutableBlockPos.set(this.mob.getX(), (double)i, this.mob.getZ()));
		if (!this.mob.canStandOnFluid(blockState.getFluidState())) {
			if (this.canFloat() && this.mob.isInWater()) {
				while (true) {
					if (!blockState.is(Blocks.WATER) && blockState.getFluidState() != Fluids.WATER.getSource(false)) {
						i--;
						break;
					}

					blockState = this.currentContext.getBlockState(mutableBlockPos.set(this.mob.getX(), (double)(++i), this.mob.getZ()));
				}
			} else if (this.mob.onGround()) {
				i = Mth.floor(this.mob.getY() + 0.5);
			} else {
				mutableBlockPos.set(this.mob.getX(), this.mob.getY() + 1.0, this.mob.getZ());

				while (mutableBlockPos.getY() > this.currentContext.level().getMinBuildHeight()) {
					i = mutableBlockPos.getY();
					mutableBlockPos.setY(mutableBlockPos.getY() - 1);
					BlockState blockState2 = this.currentContext.getBlockState(mutableBlockPos);
					if (!blockState2.isAir() && !blockState2.isPathfindable(PathComputationType.LAND)) {
						break;
					}
				}
			}
		} else {
			while (this.mob.canStandOnFluid(blockState.getFluidState())) {
				blockState = this.currentContext.getBlockState(mutableBlockPos.set(this.mob.getX(), (double)(++i), this.mob.getZ()));
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
		node.type = this.getCachedPathType(node.x, node.y, node.z);
		node.costMalus = this.mob.getPathfindingMalus(node.type);
		return node;
	}

	protected boolean canStartAt(BlockPos blockPos) {
		PathType pathType = this.getCachedPathType(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		return pathType != PathType.OPEN && this.mob.getPathfindingMalus(pathType) >= 0.0F;
	}

	@Override
	public Target getTarget(double d, double e, double f) {
		return this.getTargetNodeAt(d, e, f);
	}

	@Override
	public int getNeighbors(Node[] nodes, Node node) {
		int i = 0;
		int j = 0;
		PathType pathType = this.getCachedPathType(node.x, node.y + 1, node.z);
		PathType pathType2 = this.getCachedPathType(node.x, node.y, node.z);
		if (this.mob.getPathfindingMalus(pathType) >= 0.0F && pathType2 != PathType.STICKY_HONEY) {
			j = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
		}

		double d = this.getFloorLevel(new BlockPos(node.x, node.y, node.z));

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			Node node2 = this.findAcceptedNode(node.x + direction.getStepX(), node.y, node.z + direction.getStepZ(), j, d, direction, pathType2);
			this.reusableNeighbors[direction.get2DDataValue()] = node2;
			if (this.isNeighborValid(node2, node)) {
				nodes[i++] = node2;
			}
		}

		for (Direction directionx : Direction.Plane.HORIZONTAL) {
			Direction direction2 = directionx.getClockWise();
			if (this.isDiagonalValid(node, this.reusableNeighbors[directionx.get2DDataValue()], this.reusableNeighbors[direction2.get2DDataValue()])) {
				Node node3 = this.findAcceptedNode(
					node.x + directionx.getStepX() + direction2.getStepX(), node.y, node.z + directionx.getStepZ() + direction2.getStepZ(), j, d, directionx, pathType2
				);
				if (this.isDiagonalValid(node3)) {
					nodes[i++] = node3;
				}
			}
		}

		return i;
	}

	protected boolean isNeighborValid(@Nullable Node node, Node node2) {
		return node != null && !node.closed && (node.costMalus >= 0.0F || node2.costMalus < 0.0F);
	}

	protected boolean isDiagonalValid(Node node, @Nullable Node node2, @Nullable Node node3) {
		if (node3 == null || node2 == null || node3.y > node.y || node2.y > node.y) {
			return false;
		} else if (node2.type != PathType.WALKABLE_DOOR && node3.type != PathType.WALKABLE_DOOR) {
			boolean bl = node3.type == PathType.FENCE && node2.type == PathType.FENCE && (double)this.mob.getBbWidth() < 0.5;
			return (node3.y < node.y || node3.costMalus >= 0.0F || bl) && (node2.y < node.y || node2.costMalus >= 0.0F || bl);
		} else {
			return false;
		}
	}

	protected boolean isDiagonalValid(@Nullable Node node) {
		if (node == null || node.closed) {
			return false;
		} else {
			return node.type == PathType.WALKABLE_DOOR ? false : node.costMalus >= 0.0F;
		}
	}

	private static boolean doesBlockHavePartialCollision(PathType pathType) {
		return pathType == PathType.FENCE || pathType == PathType.DOOR_WOOD_CLOSED || pathType == PathType.DOOR_IRON_CLOSED;
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
		BlockGetter blockGetter = this.currentContext.level();
		return (this.canFloat() || this.isAmphibious()) && blockGetter.getFluidState(blockPos).is(FluidTags.WATER)
			? (double)blockPos.getY() + 0.5
			: getFloorLevel(blockGetter, blockPos);
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
	protected Node findAcceptedNode(int i, int j, int k, int l, double d, Direction direction, PathType pathType) {
		Node node = null;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		double e = this.getFloorLevel(mutableBlockPos.set(i, j, k));
		if (e - d > this.getMobJumpHeight()) {
			return null;
		} else {
			PathType pathType2 = this.getCachedPathType(i, j, k);
			float f = this.mob.getPathfindingMalus(pathType2);
			if (f >= 0.0F) {
				node = this.getNodeAndUpdateCostToMax(i, j, k, pathType2, f);
			}

			if (doesBlockHavePartialCollision(pathType) && node != null && node.costMalus >= 0.0F && !this.canReachWithoutCollision(node)) {
				node = null;
			}

			if (pathType2 != PathType.WALKABLE && (!this.isAmphibious() || pathType2 != PathType.WATER)) {
				if ((node == null || node.costMalus < 0.0F)
					&& l > 0
					&& (pathType2 != PathType.FENCE || this.canWalkOverFences())
					&& pathType2 != PathType.UNPASSABLE_RAIL
					&& pathType2 != PathType.TRAPDOOR
					&& pathType2 != PathType.POWDER_SNOW) {
					node = this.tryJumpOn(i, j, k, l, d, direction, pathType, mutableBlockPos);
				} else if (!this.isAmphibious() && pathType2 == PathType.WATER && !this.canFloat()) {
					node = this.tryFindFirstNonWaterBelow(i, j, k, node);
				} else if (pathType2 == PathType.OPEN) {
					node = this.tryFindFirstGroundNodeBelow(i, j, k);
				} else if (doesBlockHavePartialCollision(pathType2) && node == null) {
					node = this.getClosedNode(i, j, k, pathType2);
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

	private Node getNodeAndUpdateCostToMax(int i, int j, int k, PathType pathType, float f) {
		Node node = this.getNode(i, j, k);
		node.type = pathType;
		node.costMalus = Math.max(node.costMalus, f);
		return node;
	}

	private Node getBlockedNode(int i, int j, int k) {
		Node node = this.getNode(i, j, k);
		node.type = PathType.BLOCKED;
		node.costMalus = -1.0F;
		return node;
	}

	private Node getClosedNode(int i, int j, int k, PathType pathType) {
		Node node = this.getNode(i, j, k);
		node.closed = true;
		node.type = pathType;
		node.costMalus = pathType.getMalus();
		return node;
	}

	@Nullable
	private Node tryJumpOn(int i, int j, int k, int l, double d, Direction direction, PathType pathType, BlockPos.MutableBlockPos mutableBlockPos) {
		Node node = this.findAcceptedNode(i, j + 1, k, l - 1, d, direction, pathType);
		if (node == null) {
			return null;
		} else if (this.mob.getBbWidth() >= 1.0F) {
			return node;
		} else if (node.type != PathType.OPEN && node.type != PathType.WALKABLE) {
			return node;
		} else {
			double e = (double)(i - direction.getStepX()) + 0.5;
			double f = (double)(k - direction.getStepZ()) + 0.5;
			double g = (double)this.mob.getBbWidth() / 2.0;
			AABB aABB = new AABB(
				e - g,
				this.getFloorLevel(mutableBlockPos.set(e, (double)(j + 1), f)) + 0.001,
				f - g,
				e + g,
				(double)this.mob.getBbHeight() + this.getFloorLevel(mutableBlockPos.set((double)node.x, (double)node.y, (double)node.z)) - 0.002,
				f + g
			);
			return this.hasCollisions(aABB) ? null : node;
		}
	}

	@Nullable
	private Node tryFindFirstNonWaterBelow(int i, int j, int k, @Nullable Node node) {
		j--;

		while (j > this.mob.level().getMinBuildHeight()) {
			PathType pathType = this.getCachedPathType(i, j, k);
			if (pathType != PathType.WATER) {
				return node;
			}

			node = this.getNodeAndUpdateCostToMax(i, j, k, pathType, this.mob.getPathfindingMalus(pathType));
			j--;
		}

		return node;
	}

	private Node tryFindFirstGroundNodeBelow(int i, int j, int k) {
		for (int l = j - 1; l >= this.mob.level().getMinBuildHeight(); l--) {
			if (j - l > this.mob.getMaxFallDistance()) {
				return this.getBlockedNode(i, l, k);
			}

			PathType pathType = this.getCachedPathType(i, l, k);
			float f = this.mob.getPathfindingMalus(pathType);
			if (pathType != PathType.OPEN) {
				if (f >= 0.0F) {
					return this.getNodeAndUpdateCostToMax(i, l, k, pathType, f);
				}

				return this.getBlockedNode(i, l, k);
			}
		}

		return this.getBlockedNode(i, j, k);
	}

	private boolean hasCollisions(AABB aABB) {
		return this.collisionCache.computeIfAbsent(aABB, (Object2BooleanFunction<? super AABB>)(object -> !this.currentContext.level().noCollision(this.mob, aABB)));
	}

	protected PathType getCachedPathType(int i, int j, int k) {
		return this.pathTypesByPosCacheByMob
			.computeIfAbsent(BlockPos.asLong(i, j, k), (Long2ObjectFunction<? extends PathType>)(l -> this.getPathTypeOfMob(this.currentContext, i, j, k, this.mob)));
	}

	@Override
	public PathType getPathTypeOfMob(PathfindingContext pathfindingContext, int i, int j, int k, Mob mob) {
		Set<PathType> set = this.getPathTypeWithinMobBB(pathfindingContext, i, j, k);
		if (set.contains(PathType.FENCE)) {
			return PathType.FENCE;
		} else if (set.contains(PathType.UNPASSABLE_RAIL)) {
			return PathType.UNPASSABLE_RAIL;
		} else {
			PathType pathType = PathType.BLOCKED;

			for (PathType pathType2 : set) {
				if (mob.getPathfindingMalus(pathType2) < 0.0F) {
					return pathType2;
				}

				if (mob.getPathfindingMalus(pathType2) >= mob.getPathfindingMalus(pathType)) {
					pathType = pathType2;
				}
			}

			return this.entityWidth <= 1
					&& pathType != PathType.OPEN
					&& mob.getPathfindingMalus(pathType) == 0.0F
					&& this.getPathType(pathfindingContext, i, j, k) == PathType.OPEN
				? PathType.OPEN
				: pathType;
		}
	}

	public Set<PathType> getPathTypeWithinMobBB(PathfindingContext pathfindingContext, int i, int j, int k) {
		EnumSet<PathType> enumSet = EnumSet.noneOf(PathType.class);

		for (int l = 0; l < this.entityWidth; l++) {
			for (int m = 0; m < this.entityHeight; m++) {
				for (int n = 0; n < this.entityDepth; n++) {
					int o = l + i;
					int p = m + j;
					int q = n + k;
					PathType pathType = this.getPathType(pathfindingContext, o, p, q);
					BlockPos blockPos = this.mob.blockPosition();
					boolean bl = this.canPassDoors();
					if (pathType == PathType.DOOR_WOOD_CLOSED && this.canOpenDoors() && bl) {
						pathType = PathType.WALKABLE_DOOR;
					}

					if (pathType == PathType.DOOR_OPEN && !bl) {
						pathType = PathType.BLOCKED;
					}

					if (pathType == PathType.RAIL
						&& this.getPathType(pathfindingContext, blockPos.getX(), blockPos.getY(), blockPos.getZ()) != PathType.RAIL
						&& this.getPathType(pathfindingContext, blockPos.getX(), blockPos.getY() - 1, blockPos.getZ()) != PathType.RAIL) {
						pathType = PathType.UNPASSABLE_RAIL;
					}

					enumSet.add(pathType);
				}
			}
		}

		return enumSet;
	}

	@Override
	public PathType getPathType(PathfindingContext pathfindingContext, int i, int j, int k) {
		return getPathTypeStatic(pathfindingContext, new BlockPos.MutableBlockPos(i, j, k));
	}

	public static PathType getPathTypeStatic(Mob mob, BlockPos blockPos) {
		return getPathTypeStatic(new PathfindingContext(mob.level(), mob), blockPos.mutable());
	}

	public static PathType getPathTypeStatic(PathfindingContext pathfindingContext, BlockPos.MutableBlockPos mutableBlockPos) {
		int i = mutableBlockPos.getX();
		int j = mutableBlockPos.getY();
		int k = mutableBlockPos.getZ();
		PathType pathType = pathfindingContext.getPathTypeFromState(i, j, k);
		if (pathType == PathType.OPEN && j >= pathfindingContext.level().getMinBuildHeight() + 1) {
			return switch (pathfindingContext.getPathTypeFromState(i, j - 1, k)) {
				case OPEN, WATER, LAVA, WALKABLE -> PathType.OPEN;
				case DAMAGE_FIRE -> PathType.DAMAGE_FIRE;
				case DAMAGE_OTHER -> PathType.DAMAGE_OTHER;
				case STICKY_HONEY -> PathType.STICKY_HONEY;
				case POWDER_SNOW -> PathType.DANGER_POWDER_SNOW;
				case DAMAGE_CAUTIOUS -> PathType.DAMAGE_CAUTIOUS;
				case TRAPDOOR -> PathType.DANGER_TRAPDOOR;
				default -> checkNeighbourBlocks(pathfindingContext, i, j, k, PathType.WALKABLE);
			};
		} else {
			return pathType;
		}
	}

	public static PathType checkNeighbourBlocks(PathfindingContext pathfindingContext, int i, int j, int k, PathType pathType) {
		for (int l = -1; l <= 1; l++) {
			for (int m = -1; m <= 1; m++) {
				for (int n = -1; n <= 1; n++) {
					if (l != 0 || n != 0) {
						PathType pathType2 = pathfindingContext.getPathTypeFromState(i + l, j + m, k + n);
						if (pathType2 == PathType.DAMAGE_OTHER) {
							return PathType.DANGER_OTHER;
						}

						if (pathType2 == PathType.DAMAGE_FIRE || pathType2 == PathType.LAVA) {
							return PathType.DANGER_FIRE;
						}

						if (pathType2 == PathType.WATER) {
							return PathType.WATER_BORDER;
						}

						if (pathType2 == PathType.DAMAGE_CAUTIOUS) {
							return PathType.DAMAGE_CAUTIOUS;
						}
					}
				}
			}
		}

		return pathType;
	}

	protected static PathType getPathTypeFromState(BlockGetter blockGetter, BlockPos blockPos) {
		BlockState blockState = blockGetter.getBlockState(blockPos);
		Block block = blockState.getBlock();
		if (blockState.isAir()) {
			return PathType.OPEN;
		} else if (blockState.is(BlockTags.TRAPDOORS) || blockState.is(Blocks.LILY_PAD) || blockState.is(Blocks.BIG_DRIPLEAF)) {
			return PathType.TRAPDOOR;
		} else if (blockState.is(Blocks.POWDER_SNOW)) {
			return PathType.POWDER_SNOW;
		} else if (blockState.is(Blocks.CACTUS) || blockState.is(Blocks.SWEET_BERRY_BUSH)) {
			return PathType.DAMAGE_OTHER;
		} else if (blockState.is(Blocks.HONEY_BLOCK)) {
			return PathType.STICKY_HONEY;
		} else if (blockState.is(Blocks.COCOA)) {
			return PathType.COCOA;
		} else if (!blockState.is(Blocks.WITHER_ROSE) && !blockState.is(Blocks.POINTED_DRIPSTONE)) {
			FluidState fluidState = blockState.getFluidState();
			if (fluidState.is(FluidTags.LAVA)) {
				return PathType.LAVA;
			} else if (isBurningBlock(blockState)) {
				return PathType.DAMAGE_FIRE;
			} else if (block instanceof DoorBlock doorBlock) {
				if ((Boolean)blockState.getValue(DoorBlock.OPEN)) {
					return PathType.DOOR_OPEN;
				} else {
					return doorBlock.type().canOpenByHand() ? PathType.DOOR_WOOD_CLOSED : PathType.DOOR_IRON_CLOSED;
				}
			} else if (block instanceof BaseRailBlock) {
				return PathType.RAIL;
			} else if (block instanceof LeavesBlock) {
				return PathType.LEAVES;
			} else if (!blockState.is(BlockTags.FENCES)
				&& !blockState.is(BlockTags.WALLS)
				&& (!(block instanceof FenceGateBlock) || (Boolean)blockState.getValue(FenceGateBlock.OPEN))) {
				if (!blockState.isPathfindable(PathComputationType.LAND)) {
					return PathType.BLOCKED;
				} else {
					return fluidState.is(FluidTags.WATER) ? PathType.WATER : PathType.OPEN;
				}
			} else {
				return PathType.FENCE;
			}
		} else {
			return PathType.DAMAGE_CAUTIOUS;
		}
	}
}
