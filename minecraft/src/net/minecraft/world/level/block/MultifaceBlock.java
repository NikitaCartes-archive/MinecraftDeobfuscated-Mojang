package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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

public class MultifaceBlock extends Block {
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

	@Nullable
	public BlockState getStateForPlacement(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		if (!this.isFaceSupported(direction)) {
			return null;
		} else {
			BlockState blockState2;
			if (blockState.is(this)) {
				if (hasFace(blockState, direction)) {
					return null;
				}

				blockState2 = blockState;
			} else if (this.isWaterloggable() && blockState.getFluidState().isSourceOfType(Fluids.WATER)) {
				blockState2 = getEmptyState(this).setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
			} else {
				blockState2 = getEmptyState(this);
			}

			BlockPos blockPos2 = blockPos.relative(direction);
			return canAttachTo(blockGetter, direction, blockPos2, blockGetter.getBlockState(blockPos2))
				? blockState2.setValue(getFaceProperty(direction), Boolean.valueOf(true))
				: null;
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

	public boolean spreadFromRandomFaceTowardRandomDirection(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		List<Direction> list = Lists.<Direction>newArrayList(DIRECTIONS);
		Collections.shuffle(list);
		return list.stream()
			.filter(direction -> hasFace(blockState, direction))
			.anyMatch(direction -> this.spreadFromFaceTowardRandomDirection(blockState, serverLevel, blockPos, direction, random, false));
	}

	public boolean spreadFromFaceTowardRandomDirection(
		BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, Random random, boolean bl
	) {
		List<Direction> list = Arrays.asList(DIRECTIONS);
		Collections.shuffle(list, random);
		return list.stream().anyMatch(direction2 -> this.spreadFromFaceTowardDirection(blockState, levelAccessor, blockPos, direction, direction2, bl));
	}

	public boolean spreadFromFaceTowardDirection(
		BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, Direction direction2, boolean bl
	) {
		Optional<Pair<BlockPos, Direction>> optional = this.getSpreadFromFaceTowardDirection(blockState, levelAccessor, blockPos, direction, direction2);
		if (optional.isPresent()) {
			Pair<BlockPos, Direction> pair = (Pair<BlockPos, Direction>)optional.get();
			return this.spreadToFace(levelAccessor, pair.getFirst(), pair.getSecond(), bl);
		} else {
			return false;
		}
	}

	protected boolean canSpread(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return Stream.of(DIRECTIONS)
			.anyMatch(direction2 -> this.getSpreadFromFaceTowardDirection(blockState, blockGetter, blockPos, direction, direction2).isPresent());
	}

	private Optional<Pair<BlockPos, Direction>> getSpreadFromFaceTowardDirection(
		BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction, Direction direction2
	) {
		if (direction2.getAxis() == direction.getAxis() || !hasFace(blockState, direction) || hasFace(blockState, direction2)) {
			return Optional.empty();
		} else if (this.canSpreadToFace(blockGetter, blockPos, direction2)) {
			return Optional.of(Pair.of(blockPos, direction2));
		} else {
			BlockPos blockPos2 = blockPos.relative(direction2);
			if (this.canSpreadToFace(blockGetter, blockPos2, direction)) {
				return Optional.of(Pair.of(blockPos2, direction));
			} else {
				BlockPos blockPos3 = blockPos2.relative(direction);
				Direction direction3 = direction2.getOpposite();
				return this.canSpreadToFace(blockGetter, blockPos3, direction3) ? Optional.of(Pair.of(blockPos3, direction3)) : Optional.empty();
			}
		}
	}

	private boolean canSpreadToFace(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		BlockState blockState = blockGetter.getBlockState(blockPos);
		if (!this.canSpreadInto(blockState)) {
			return false;
		} else {
			BlockState blockState2 = this.getStateForPlacement(blockState, blockGetter, blockPos, direction);
			return blockState2 != null;
		}
	}

	private boolean spreadToFace(LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, boolean bl) {
		BlockState blockState = levelAccessor.getBlockState(blockPos);
		BlockState blockState2 = this.getStateForPlacement(blockState, levelAccessor, blockPos, direction);
		if (blockState2 != null) {
			if (bl) {
				levelAccessor.getChunk(blockPos).markPosForPostprocessing(blockPos);
			}

			return levelAccessor.setBlock(blockPos, blockState2, 2);
		} else {
			return false;
		}
	}

	private boolean canSpreadInto(BlockState blockState) {
		return blockState.isAir() || blockState.is(this) || blockState.is(Blocks.WATER) && blockState.getFluidState().isSource();
	}

	private static boolean hasFace(BlockState blockState, Direction direction) {
		BooleanProperty booleanProperty = getFaceProperty(direction);
		return blockState.hasProperty(booleanProperty) && (Boolean)blockState.getValue(booleanProperty);
	}

	private static boolean canAttachTo(BlockGetter blockGetter, Direction direction, BlockPos blockPos, BlockState blockState) {
		return Block.isFaceFull(blockState.getCollisionShape(blockGetter, blockPos), direction.getOpposite());
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

	public static BlockState getEmptyState(Block block) {
		return getMultifaceStateWithAllFaces(block.defaultBlockState(), false);
	}

	private static BlockState getDefaultMultifaceState(StateDefinition<Block, BlockState> stateDefinition) {
		return getMultifaceStateWithAllFaces(stateDefinition.any(), true);
	}

	private static BlockState getMultifaceStateWithAllFaces(BlockState blockState, boolean bl) {
		for (BooleanProperty booleanProperty : PROPERTY_BY_DIRECTION.values()) {
			if (blockState.hasProperty(booleanProperty)) {
				blockState = blockState.setValue(booleanProperty, Boolean.valueOf(bl));
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

		return voxelShape;
	}

	private static boolean hasAnyFace(BlockState blockState) {
		return Arrays.stream(DIRECTIONS).anyMatch(direction -> hasFace(blockState, direction));
	}

	private static boolean hasAnyVacantFace(BlockState blockState) {
		return Arrays.stream(DIRECTIONS).anyMatch(direction -> !hasFace(blockState, direction));
	}
}
