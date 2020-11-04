package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VineBlock extends Block {
	public static final BooleanProperty UP = PipeBlock.UP;
	public static final BooleanProperty NORTH = PipeBlock.NORTH;
	public static final BooleanProperty EAST = PipeBlock.EAST;
	public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
	public static final BooleanProperty WEST = PipeBlock.WEST;
	public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = (Map<Direction, BooleanProperty>)PipeBlock.PROPERTY_BY_DIRECTION
		.entrySet()
		.stream()
		.filter(entry -> entry.getKey() != Direction.DOWN)
		.collect(Util.toMap());
	private static final VoxelShape UP_AABB = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape WEST_AABB = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
	private static final VoxelShape EAST_AABB = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
	private static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
	private final Map<BlockState, VoxelShape> shapesCache;

	public VineBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(UP, Boolean.valueOf(false))
				.setValue(NORTH, Boolean.valueOf(false))
				.setValue(EAST, Boolean.valueOf(false))
				.setValue(SOUTH, Boolean.valueOf(false))
				.setValue(WEST, Boolean.valueOf(false))
		);
		this.shapesCache = ImmutableMap.copyOf(
			(Map<? extends BlockState, ? extends VoxelShape>)this.stateDefinition
				.getPossibleStates()
				.stream()
				.collect(Collectors.toMap(Function.identity(), VineBlock::calculateShape))
		);
	}

	private static VoxelShape calculateShape(BlockState blockState) {
		VoxelShape voxelShape = Shapes.empty();
		if ((Boolean)blockState.getValue(UP)) {
			voxelShape = UP_AABB;
		}

		if ((Boolean)blockState.getValue(NORTH)) {
			voxelShape = Shapes.or(voxelShape, NORTH_AABB);
		}

		if ((Boolean)blockState.getValue(SOUTH)) {
			voxelShape = Shapes.or(voxelShape, SOUTH_AABB);
		}

		if ((Boolean)blockState.getValue(EAST)) {
			voxelShape = Shapes.or(voxelShape, EAST_AABB);
		}

		if ((Boolean)blockState.getValue(WEST)) {
			voxelShape = Shapes.or(voxelShape, WEST_AABB);
		}

		return voxelShape;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return (VoxelShape)this.shapesCache.get(blockState);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return this.hasFaces(this.getUpdatedState(blockState, levelReader, blockPos));
	}

	private boolean hasFaces(BlockState blockState) {
		return this.countFaces(blockState) > 0;
	}

	private int countFaces(BlockState blockState) {
		int i = 0;

		for (BooleanProperty booleanProperty : PROPERTY_BY_DIRECTION.values()) {
			if ((Boolean)blockState.getValue(booleanProperty)) {
				i++;
			}
		}

		return i;
	}

	private boolean canSupportAtFace(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		if (direction == Direction.DOWN) {
			return false;
		} else {
			BlockPos blockPos2 = blockPos.relative(direction);
			if (isAcceptableNeighbour(blockGetter, blockPos2, direction)) {
				return true;
			} else if (direction.getAxis() == Direction.Axis.Y) {
				return false;
			} else {
				BooleanProperty booleanProperty = (BooleanProperty)PROPERTY_BY_DIRECTION.get(direction);
				BlockState blockState = blockGetter.getBlockState(blockPos.above());
				return blockState.is(this) && (Boolean)blockState.getValue(booleanProperty);
			}
		}
	}

	public static boolean isAcceptableNeighbour(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		BlockState blockState = blockGetter.getBlockState(blockPos);
		return Block.isFaceFull(blockState.getCollisionShape(blockGetter, blockPos), direction.getOpposite());
	}

	private BlockState getUpdatedState(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.above();
		if ((Boolean)blockState.getValue(UP)) {
			blockState = blockState.setValue(UP, Boolean.valueOf(isAcceptableNeighbour(blockGetter, blockPos2, Direction.DOWN)));
		}

		BlockState blockState2 = null;

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BooleanProperty booleanProperty = getPropertyForFace(direction);
			if ((Boolean)blockState.getValue(booleanProperty)) {
				boolean bl = this.canSupportAtFace(blockGetter, blockPos, direction);
				if (!bl) {
					if (blockState2 == null) {
						blockState2 = blockGetter.getBlockState(blockPos2);
					}

					bl = blockState2.is(this) && (Boolean)blockState2.getValue(booleanProperty);
				}

				blockState = blockState.setValue(booleanProperty, Boolean.valueOf(bl));
			}
		}

		return blockState;
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (direction == Direction.DOWN) {
			return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
		} else {
			BlockState blockState3 = this.getUpdatedState(blockState, levelAccessor, blockPos);
			return !this.hasFaces(blockState3) ? Blocks.AIR.defaultBlockState() : blockState3;
		}
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (serverLevel.random.nextInt(4) == 0) {
			Direction direction = Direction.getRandom(random);
			BlockPos blockPos2 = blockPos.above();
			if (direction.getAxis().isHorizontal() && !(Boolean)blockState.getValue(getPropertyForFace(direction))) {
				if (this.canSpread(serverLevel, blockPos)) {
					BlockPos blockPos3 = blockPos.relative(direction);
					BlockState blockState2 = serverLevel.getBlockState(blockPos3);
					if (blockState2.isAir()) {
						Direction direction2 = direction.getClockWise();
						Direction direction3 = direction.getCounterClockWise();
						boolean bl = (Boolean)blockState.getValue(getPropertyForFace(direction2));
						boolean bl2 = (Boolean)blockState.getValue(getPropertyForFace(direction3));
						BlockPos blockPos4 = blockPos3.relative(direction2);
						BlockPos blockPos5 = blockPos3.relative(direction3);
						if (bl && isAcceptableNeighbour(serverLevel, blockPos4, direction2)) {
							serverLevel.setBlock(blockPos3, this.defaultBlockState().setValue(getPropertyForFace(direction2), Boolean.valueOf(true)), 2);
						} else if (bl2 && isAcceptableNeighbour(serverLevel, blockPos5, direction3)) {
							serverLevel.setBlock(blockPos3, this.defaultBlockState().setValue(getPropertyForFace(direction3), Boolean.valueOf(true)), 2);
						} else {
							Direction direction4 = direction.getOpposite();
							if (bl && serverLevel.isEmptyBlock(blockPos4) && isAcceptableNeighbour(serverLevel, blockPos.relative(direction2), direction4)) {
								serverLevel.setBlock(blockPos4, this.defaultBlockState().setValue(getPropertyForFace(direction4), Boolean.valueOf(true)), 2);
							} else if (bl2 && serverLevel.isEmptyBlock(blockPos5) && isAcceptableNeighbour(serverLevel, blockPos.relative(direction3), direction4)) {
								serverLevel.setBlock(blockPos5, this.defaultBlockState().setValue(getPropertyForFace(direction4), Boolean.valueOf(true)), 2);
							} else if ((double)serverLevel.random.nextFloat() < 0.05 && isAcceptableNeighbour(serverLevel, blockPos3.above(), Direction.UP)) {
								serverLevel.setBlock(blockPos3, this.defaultBlockState().setValue(UP, Boolean.valueOf(true)), 2);
							}
						}
					} else if (isAcceptableNeighbour(serverLevel, blockPos3, direction)) {
						serverLevel.setBlock(blockPos, blockState.setValue(getPropertyForFace(direction), Boolean.valueOf(true)), 2);
					}
				}
			} else {
				if (direction == Direction.UP && blockPos.getY() < serverLevel.getMaxBuildHeight() - 1) {
					if (this.canSupportAtFace(serverLevel, blockPos, direction)) {
						serverLevel.setBlock(blockPos, blockState.setValue(UP, Boolean.valueOf(true)), 2);
						return;
					}

					if (serverLevel.isEmptyBlock(blockPos2)) {
						if (!this.canSpread(serverLevel, blockPos)) {
							return;
						}

						BlockState blockState3 = blockState;

						for (Direction direction2 : Direction.Plane.HORIZONTAL) {
							if (random.nextBoolean() || !isAcceptableNeighbour(serverLevel, blockPos2.relative(direction2), Direction.UP)) {
								blockState3 = blockState3.setValue(getPropertyForFace(direction2), Boolean.valueOf(false));
							}
						}

						if (this.hasHorizontalConnection(blockState3)) {
							serverLevel.setBlock(blockPos2, blockState3, 2);
						}

						return;
					}
				}

				if (blockPos.getY() > serverLevel.getMinBuildHeight()) {
					BlockPos blockPos3 = blockPos.below();
					BlockState blockState2 = serverLevel.getBlockState(blockPos3);
					if (blockState2.isAir() || blockState2.is(this)) {
						BlockState blockState4 = blockState2.isAir() ? this.defaultBlockState() : blockState2;
						BlockState blockState5 = this.copyRandomFaces(blockState, blockState4, random);
						if (blockState4 != blockState5 && this.hasHorizontalConnection(blockState5)) {
							serverLevel.setBlock(blockPos3, blockState5, 2);
						}
					}
				}
			}
		}
	}

	private BlockState copyRandomFaces(BlockState blockState, BlockState blockState2, Random random) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (random.nextBoolean()) {
				BooleanProperty booleanProperty = getPropertyForFace(direction);
				if ((Boolean)blockState.getValue(booleanProperty)) {
					blockState2 = blockState2.setValue(booleanProperty, Boolean.valueOf(true));
				}
			}
		}

		return blockState2;
	}

	private boolean hasHorizontalConnection(BlockState blockState) {
		return (Boolean)blockState.getValue(NORTH) || (Boolean)blockState.getValue(EAST) || (Boolean)blockState.getValue(SOUTH) || (Boolean)blockState.getValue(WEST);
	}

	private boolean canSpread(BlockGetter blockGetter, BlockPos blockPos) {
		int i = 4;
		Iterable<BlockPos> iterable = BlockPos.betweenClosed(
			blockPos.getX() - 4, blockPos.getY() - 1, blockPos.getZ() - 4, blockPos.getX() + 4, blockPos.getY() + 1, blockPos.getZ() + 4
		);
		int j = 5;

		for (BlockPos blockPos2 : iterable) {
			if (blockGetter.getBlockState(blockPos2).is(this)) {
				if (--j <= 0) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
		BlockState blockState2 = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
		return blockState2.is(this) ? this.countFaces(blockState2) < PROPERTY_BY_DIRECTION.size() : super.canBeReplaced(blockState, blockPlaceContext);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
		boolean bl = blockState.is(this);
		BlockState blockState2 = bl ? blockState : this.defaultBlockState();

		for (Direction direction : blockPlaceContext.getNearestLookingDirections()) {
			if (direction != Direction.DOWN) {
				BooleanProperty booleanProperty = getPropertyForFace(direction);
				boolean bl2 = bl && (Boolean)blockState.getValue(booleanProperty);
				if (!bl2 && this.canSupportAtFace(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), direction)) {
					return blockState2.setValue(booleanProperty, Boolean.valueOf(true));
				}
			}
		}

		return bl ? blockState2 : null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(UP, NORTH, EAST, SOUTH, WEST);
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		switch (rotation) {
			case CLOCKWISE_180:
				return blockState.setValue(NORTH, blockState.getValue(SOUTH))
					.setValue(EAST, blockState.getValue(WEST))
					.setValue(SOUTH, blockState.getValue(NORTH))
					.setValue(WEST, blockState.getValue(EAST));
			case COUNTERCLOCKWISE_90:
				return blockState.setValue(NORTH, blockState.getValue(EAST))
					.setValue(EAST, blockState.getValue(SOUTH))
					.setValue(SOUTH, blockState.getValue(WEST))
					.setValue(WEST, blockState.getValue(NORTH));
			case CLOCKWISE_90:
				return blockState.setValue(NORTH, blockState.getValue(WEST))
					.setValue(EAST, blockState.getValue(NORTH))
					.setValue(SOUTH, blockState.getValue(EAST))
					.setValue(WEST, blockState.getValue(SOUTH));
			default:
				return blockState;
		}
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		switch (mirror) {
			case LEFT_RIGHT:
				return blockState.setValue(NORTH, blockState.getValue(SOUTH)).setValue(SOUTH, blockState.getValue(NORTH));
			case FRONT_BACK:
				return blockState.setValue(EAST, blockState.getValue(WEST)).setValue(WEST, blockState.getValue(EAST));
			default:
				return super.mirror(blockState, mirror);
		}
	}

	public static BooleanProperty getPropertyForFace(Direction direction) {
		return (BooleanProperty)PROPERTY_BY_DIRECTION.get(direction);
	}
}
