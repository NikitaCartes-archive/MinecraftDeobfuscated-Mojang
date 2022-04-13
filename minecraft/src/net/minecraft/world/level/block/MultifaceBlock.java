package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class MultifaceBlock extends Block {
	private static final float AABB_OFFSET = 1.0F;
	private static final VoxelShape UP_AABB = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape DOWN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
	private static final VoxelShape WEST_AABB = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
	private static final VoxelShape EAST_AABB = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
	private static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
	private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;
	private static final Map<Direction, VoxelShape> SHAPE_BY_DIRECTION = Util.make(Maps.newEnumMap(Direction.class), enumMap -> {
		enumMap.put(Direction.NORTH, NORTH_AABB);
		enumMap.put(Direction.EAST, EAST_AABB);
		enumMap.put(Direction.SOUTH, SOUTH_AABB);
		enumMap.put(Direction.WEST, WEST_AABB);
		enumMap.put(Direction.UP, UP_AABB);
		enumMap.put(Direction.DOWN, DOWN_AABB);
	});
	protected static final Direction[] DIRECTIONS = Direction.values();
	private final ImmutableMap<BlockState, VoxelShape> shapesCache;
	private final boolean canRotate;
	private final boolean canMirrorX;
	private final boolean canMirrorZ;

	public MultifaceBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(getDefaultMultifaceState(this.stateDefinition));
		this.shapesCache = this.getShapeForEachState(MultifaceBlock::calculateMultifaceShape);
		this.canRotate = Direction.Plane.HORIZONTAL.stream().allMatch(this::isFaceSupported);
		this.canMirrorX = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.X).filter(this::isFaceSupported).count() % 2L == 0L;
		this.canMirrorZ = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.Z).filter(this::isFaceSupported).count() % 2L == 0L;
	}

	public static Set<Direction> availableFaces(BlockState blockState) {
		if (!(blockState.getBlock() instanceof MultifaceBlock)) {
			return Set.of();
		} else {
			Set<Direction> set = EnumSet.noneOf(Direction.class);

			for (Direction direction : Direction.values()) {
				if (hasFace(blockState, direction)) {
					set.add(direction);
				}
			}

			return set;
		}
	}

	@Nullable
	public static Set<Direction> unpack(byte b) {
		if (b == -1) {
			return null;
		} else {
			Set<Direction> set = EnumSet.noneOf(Direction.class);

			for (Direction direction : Direction.values()) {
				if ((b & (byte)(1 << direction.ordinal())) > 0) {
					set.add(direction);
				}
			}

			return set;
		}
	}

	public static byte pack(@Nullable Collection<Direction> collection) {
		if (collection == null) {
			return -1;
		} else {
			byte b = 0;

			for (Direction direction : collection) {
				b = (byte)(b | 1 << direction.ordinal());
			}

			return b;
		}
	}

	protected boolean isFaceSupported(Direction direction) {
		return true;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		for (Direction direction : DIRECTIONS) {
			if (this.isFaceSupported(direction)) {
				builder.add(getFaceProperty(direction));
			}
		}
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (!hasAnyFace(blockState)) {
			return Blocks.AIR.defaultBlockState();
		} else {
			return hasFace(blockState, direction) && !canAttachTo(levelAccessor, direction, blockPos2, blockState2)
				? removeFace(blockState, getFaceProperty(direction))
				: blockState;
		}
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return this.shapesCache.get(blockState);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		boolean bl = false;

		for (Direction direction : DIRECTIONS) {
			if (hasFace(blockState, direction)) {
				BlockPos blockPos2 = blockPos.relative(direction);
				if (!canAttachTo(levelReader, direction, blockPos2, levelReader.getBlockState(blockPos2))) {
					return false;
				}

				bl = true;
			}
		}

		return bl;
	}

	@Override
	public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
		return hasAnyVacantFace(blockState);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		Level level = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		return (BlockState)Arrays.stream(blockPlaceContext.getNearestLookingDirections())
			.map(direction -> this.getStateForPlacement(blockState, level, blockPos, direction))
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}

	public boolean isValidStateForPlacement(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos, Direction direction) {
		if (this.isFaceSupported(direction) && (!blockState.is(this) || !hasFace(blockState, direction))) {
			BlockPos blockPos2 = blockPos.relative(direction);
			return canAttachTo(blockGetter, direction, blockPos2, blockGetter.getBlockState(blockPos2));
		} else {
			return false;
		}
	}

	@Nullable
	public BlockState getStateForPlacement(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		if (!this.isValidStateForPlacement(blockGetter, blockState, blockPos, direction)) {
			return null;
		} else {
			BlockState blockState2;
			if (blockState.is(this)) {
				blockState2 = blockState;
			} else if (this.isWaterloggable() && blockState.getFluidState().isSourceOfType(Fluids.WATER)) {
				blockState2 = this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
			} else {
				blockState2 = this.defaultBlockState();
			}

			return blockState2.setValue(getFaceProperty(direction), Boolean.valueOf(true));
		}
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return !this.canRotate ? blockState : this.mapDirections(blockState, rotation::rotate);
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		if (mirror == Mirror.FRONT_BACK && !this.canMirrorX) {
			return blockState;
		} else {
			return mirror == Mirror.LEFT_RIGHT && !this.canMirrorZ ? blockState : this.mapDirections(blockState, mirror::mirror);
		}
	}

	private BlockState mapDirections(BlockState blockState, Function<Direction, Direction> function) {
		BlockState blockState2 = blockState;

		for (Direction direction : DIRECTIONS) {
			if (this.isFaceSupported(direction)) {
				blockState2 = blockState2.setValue(getFaceProperty((Direction)function.apply(direction)), (Boolean)blockState.getValue(getFaceProperty(direction)));
			}
		}

		return blockState2;
	}

	public static boolean hasFace(BlockState blockState, Direction direction) {
		BooleanProperty booleanProperty = getFaceProperty(direction);
		return blockState.hasProperty(booleanProperty) && (Boolean)blockState.getValue(booleanProperty);
	}

	public static boolean canAttachTo(BlockGetter blockGetter, Direction direction, BlockPos blockPos, BlockState blockState) {
		return Block.isFaceFull(blockState.getBlockSupportShape(blockGetter, blockPos), direction.getOpposite())
			|| Block.isFaceFull(blockState.getCollisionShape(blockGetter, blockPos), direction.getOpposite());
	}

	private boolean isWaterloggable() {
		return this.stateDefinition.getProperties().contains(BlockStateProperties.WATERLOGGED);
	}

	private static BlockState removeFace(BlockState blockState, BooleanProperty booleanProperty) {
		BlockState blockState2 = blockState.setValue(booleanProperty, Boolean.valueOf(false));
		return hasAnyFace(blockState2) ? blockState2 : Blocks.AIR.defaultBlockState();
	}

	public static BooleanProperty getFaceProperty(Direction direction) {
		return (BooleanProperty)PROPERTY_BY_DIRECTION.get(direction);
	}

	private static BlockState getDefaultMultifaceState(StateDefinition<Block, BlockState> stateDefinition) {
		BlockState blockState = stateDefinition.any();

		for (BooleanProperty booleanProperty : PROPERTY_BY_DIRECTION.values()) {
			if (blockState.hasProperty(booleanProperty)) {
				blockState = blockState.setValue(booleanProperty, Boolean.valueOf(false));
			}
		}

		return blockState;
	}

	private static VoxelShape calculateMultifaceShape(BlockState blockState) {
		VoxelShape voxelShape = Shapes.empty();

		for (Direction direction : DIRECTIONS) {
			if (hasFace(blockState, direction)) {
				voxelShape = Shapes.or(voxelShape, (VoxelShape)SHAPE_BY_DIRECTION.get(direction));
			}
		}

		return voxelShape.isEmpty() ? Shapes.block() : voxelShape;
	}

	protected static boolean hasAnyFace(BlockState blockState) {
		return Arrays.stream(DIRECTIONS).anyMatch(direction -> hasFace(blockState, direction));
	}

	private static boolean hasAnyVacantFace(BlockState blockState) {
		return Arrays.stream(DIRECTIONS).anyMatch(direction -> !hasFace(blockState, direction));
	}

	public abstract MultifaceSpreader getSpreader();
}
