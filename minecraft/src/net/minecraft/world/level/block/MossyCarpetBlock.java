package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MossyCarpetBlock extends Block implements BonemealableBlock {
	public static final MapCodec<MossyCarpetBlock> CODEC = simpleCodec(MossyCarpetBlock::new);
	public static final BooleanProperty BASE = BlockStateProperties.BOTTOM;
	private static final EnumProperty<WallSide> NORTH = BlockStateProperties.NORTH_WALL;
	private static final EnumProperty<WallSide> EAST = BlockStateProperties.EAST_WALL;
	private static final EnumProperty<WallSide> SOUTH = BlockStateProperties.SOUTH_WALL;
	private static final EnumProperty<WallSide> WEST = BlockStateProperties.WEST_WALL;
	private static final Map<Direction, EnumProperty<WallSide>> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf(
		Util.make(Maps.newEnumMap(Direction.class), enumMap -> {
			enumMap.put(Direction.NORTH, NORTH);
			enumMap.put(Direction.EAST, EAST);
			enumMap.put(Direction.SOUTH, SOUTH);
			enumMap.put(Direction.WEST, WEST);
		})
	);
	private static final float AABB_OFFSET = 1.0F;
	private static final VoxelShape DOWN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
	private static final VoxelShape WEST_AABB = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
	private static final VoxelShape EAST_AABB = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
	private static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
	private static final int SHORT_HEIGHT = 10;
	private static final VoxelShape WEST_SHORT_AABB = Block.box(0.0, 0.0, 0.0, 1.0, 10.0, 16.0);
	private static final VoxelShape EAST_SHORT_AABB = Block.box(15.0, 0.0, 0.0, 16.0, 10.0, 16.0);
	private static final VoxelShape NORTH_SHORT_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 1.0);
	private static final VoxelShape SOUTH_SHORT_AABB = Block.box(0.0, 0.0, 15.0, 16.0, 10.0, 16.0);
	private final Map<BlockState, VoxelShape> shapesCache;

	@Override
	public MapCodec<MossyCarpetBlock> codec() {
		return CODEC;
	}

	public MossyCarpetBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(BASE, Boolean.valueOf(true))
				.setValue(NORTH, WallSide.NONE)
				.setValue(EAST, WallSide.NONE)
				.setValue(SOUTH, WallSide.NONE)
				.setValue(WEST, WallSide.NONE)
		);
		this.shapesCache = ImmutableMap.copyOf(
			(Map<? extends BlockState, ? extends VoxelShape>)this.stateDefinition
				.getPossibleStates()
				.stream()
				.collect(Collectors.toMap(Function.identity(), MossyCarpetBlock::calculateShape))
		);
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState blockState) {
		return Shapes.empty();
	}

	private static VoxelShape calculateShape(BlockState blockState) {
		VoxelShape voxelShape = Shapes.empty();
		if ((Boolean)blockState.getValue(BASE)) {
			voxelShape = DOWN_AABB;
		}
		voxelShape = switch ((WallSide)blockState.getValue(NORTH)) {
			case NONE -> voxelShape;
			case LOW -> Shapes.or(voxelShape, NORTH_SHORT_AABB);
			case TALL -> Shapes.or(voxelShape, NORTH_AABB);
		};

		voxelShape = switch ((WallSide)blockState.getValue(SOUTH)) {
			case NONE -> voxelShape;
			case LOW -> Shapes.or(voxelShape, SOUTH_SHORT_AABB);
			case TALL -> Shapes.or(voxelShape, SOUTH_AABB);
		};

		voxelShape = switch ((WallSide)blockState.getValue(EAST)) {
			case NONE -> voxelShape;
			case LOW -> Shapes.or(voxelShape, EAST_SHORT_AABB);
			case TALL -> Shapes.or(voxelShape, EAST_AABB);
		};

		voxelShape = switch ((WallSide)blockState.getValue(WEST)) {
			case NONE -> voxelShape;
			case LOW -> Shapes.or(voxelShape, WEST_SHORT_AABB);
			case TALL -> Shapes.or(voxelShape, WEST_AABB);
		};
		return voxelShape.isEmpty() ? Shapes.block() : voxelShape;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return (VoxelShape)this.shapesCache.get(blockState);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return blockState.getValue(BASE) ? DOWN_AABB : Shapes.empty();
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState blockState) {
		return true;
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockState blockState2 = levelReader.getBlockState(blockPos.below());
		return blockState.getValue(BASE) ? !blockState2.isAir() : blockState2.is(this) && (Boolean)blockState2.getValue(BASE);
	}

	private static boolean hasFaces(BlockState blockState) {
		if ((Boolean)blockState.getValue(BASE)) {
			return true;
		} else {
			for (EnumProperty<WallSide> enumProperty : PROPERTY_BY_DIRECTION.values()) {
				if (blockState.getValue(enumProperty) != WallSide.NONE) {
					return true;
				}
			}

			return false;
		}
	}

	private static boolean canSupportAtFace(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		if (direction == Direction.UP) {
			return false;
		} else {
			BlockPos blockPos2 = blockPos.relative(direction);
			return MultifaceBlock.canAttachTo(blockGetter, direction, blockPos2, blockGetter.getBlockState(blockPos2));
		}
	}

	private static BlockState getUpdatedState(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, boolean bl) {
		BlockState blockState2 = null;
		BlockState blockState3 = null;
		bl |= blockState.getValue(BASE);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			EnumProperty<WallSide> enumProperty = getPropertyForFace(direction);
			WallSide wallSide = canSupportAtFace(blockGetter, blockPos, direction) ? (bl ? WallSide.LOW : blockState.getValue(enumProperty)) : WallSide.NONE;
			if (wallSide == WallSide.LOW) {
				if (blockState2 == null) {
					blockState2 = blockGetter.getBlockState(blockPos.above());
				}

				if (blockState2.is(Blocks.PALE_MOSS_CARPET) && blockState2.getValue(enumProperty) != WallSide.NONE && !(Boolean)blockState2.getValue(BASE)) {
					wallSide = WallSide.TALL;
				}

				if (!(Boolean)blockState.getValue(BASE)) {
					if (blockState3 == null) {
						blockState3 = blockGetter.getBlockState(blockPos.below());
					}

					if (blockState3.is(Blocks.PALE_MOSS_CARPET) && blockState3.getValue(enumProperty) == WallSide.NONE) {
						wallSide = WallSide.NONE;
					}
				}
			}

			blockState = blockState.setValue(enumProperty, wallSide);
		}

		return blockState;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return getUpdatedState(this.defaultBlockState(), blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), true);
	}

	public static void placeAt(LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource, int i) {
		BlockState blockState = Blocks.PALE_MOSS_CARPET.defaultBlockState();
		BlockState blockState2 = getUpdatedState(blockState, levelAccessor, blockPos, true);
		levelAccessor.setBlock(blockPos, blockState2, 3);
		BlockState blockState3 = createTopperWithSideChance(levelAccessor, blockPos, randomSource::nextBoolean);
		if (!blockState3.isAir()) {
			levelAccessor.setBlock(blockPos.above(), blockState3, i);
		}
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
		if (!level.isClientSide) {
			RandomSource randomSource = level.getRandom();
			BlockState blockState2 = createTopperWithSideChance(level, blockPos, randomSource::nextBoolean);
			if (!blockState2.isAir()) {
				level.setBlock(blockPos.above(), blockState2, 3);
			}
		}
	}

	private static BlockState createTopperWithSideChance(BlockGetter blockGetter, BlockPos blockPos, BooleanSupplier booleanSupplier) {
		BlockPos blockPos2 = blockPos.above();
		BlockState blockState = blockGetter.getBlockState(blockPos2);
		boolean bl = blockState.is(Blocks.PALE_MOSS_CARPET);
		if ((!bl || !(Boolean)blockState.getValue(BASE)) && (bl || blockState.canBeReplaced())) {
			BlockState blockState2 = Blocks.PALE_MOSS_CARPET.defaultBlockState().setValue(BASE, Boolean.valueOf(false));
			BlockState blockState3 = getUpdatedState(blockState2, blockGetter, blockPos.above(), true);

			for (Direction direction : Direction.Plane.HORIZONTAL) {
				EnumProperty<WallSide> enumProperty = getPropertyForFace(direction);
				if (blockState3.getValue(enumProperty) != WallSide.NONE && !booleanSupplier.getAsBoolean()) {
					blockState3 = blockState3.setValue(enumProperty, WallSide.NONE);
				}
			}

			return hasFaces(blockState3) && blockState3 != blockState ? blockState3 : Blocks.AIR.defaultBlockState();
		} else {
			return Blocks.AIR.defaultBlockState();
		}
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState,
		LevelReader levelReader,
		ScheduledTickAccess scheduledTickAccess,
		BlockPos blockPos,
		Direction direction,
		BlockPos blockPos2,
		BlockState blockState2,
		RandomSource randomSource
	) {
		if (!blockState.canSurvive(levelReader, blockPos)) {
			return Blocks.AIR.defaultBlockState();
		} else {
			BlockState blockState3 = getUpdatedState(blockState, levelReader, blockPos, false);
			return !hasFaces(blockState3) ? Blocks.AIR.defaultBlockState() : blockState3;
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BASE, NORTH, EAST, SOUTH, WEST);
	}

	@Override
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		return switch (rotation) {
			case CLOCKWISE_180 -> (BlockState)blockState.setValue(NORTH, (WallSide)blockState.getValue(SOUTH))
			.setValue(EAST, (WallSide)blockState.getValue(WEST))
			.setValue(SOUTH, (WallSide)blockState.getValue(NORTH))
			.setValue(WEST, (WallSide)blockState.getValue(EAST));
			case COUNTERCLOCKWISE_90 -> (BlockState)blockState.setValue(NORTH, (WallSide)blockState.getValue(EAST))
			.setValue(EAST, (WallSide)blockState.getValue(SOUTH))
			.setValue(SOUTH, (WallSide)blockState.getValue(WEST))
			.setValue(WEST, (WallSide)blockState.getValue(NORTH));
			case CLOCKWISE_90 -> (BlockState)blockState.setValue(NORTH, (WallSide)blockState.getValue(WEST))
			.setValue(EAST, (WallSide)blockState.getValue(NORTH))
			.setValue(SOUTH, (WallSide)blockState.getValue(EAST))
			.setValue(WEST, (WallSide)blockState.getValue(SOUTH));
			default -> blockState;
		};
	}

	@Override
	protected BlockState mirror(BlockState blockState, Mirror mirror) {
		return switch (mirror) {
			case LEFT_RIGHT -> (BlockState)blockState.setValue(NORTH, (WallSide)blockState.getValue(SOUTH)).setValue(SOUTH, (WallSide)blockState.getValue(NORTH));
			case FRONT_BACK -> (BlockState)blockState.setValue(EAST, (WallSide)blockState.getValue(WEST)).setValue(WEST, (WallSide)blockState.getValue(EAST));
			default -> super.mirror(blockState, mirror);
		};
	}

	@Nullable
	public static EnumProperty<WallSide> getPropertyForFace(Direction direction) {
		return (EnumProperty<WallSide>)PROPERTY_BY_DIRECTION.get(direction);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return (Boolean)blockState.getValue(BASE);
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return !createTopperWithSideChance(level, blockPos, () -> true).isAir();
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		BlockState blockState2 = createTopperWithSideChance(serverLevel, blockPos, () -> true);
		if (!blockState2.isAir()) {
			serverLevel.setBlock(blockPos.above(), blockState2, 3);
		}
	}
}
