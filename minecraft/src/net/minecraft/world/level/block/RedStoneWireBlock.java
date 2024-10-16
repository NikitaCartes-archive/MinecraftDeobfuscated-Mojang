package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.redstone.DefaultRedstoneWireEvaluator;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.ExperimentalRedstoneWireEvaluator;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.redstone.RedstoneWireEvaluator;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RedStoneWireBlock extends Block {
	public static final MapCodec<RedStoneWireBlock> CODEC = simpleCodec(RedStoneWireBlock::new);
	public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
	public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
	public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
	public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
	public static final IntegerProperty POWER = BlockStateProperties.POWER;
	public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = Maps.newEnumMap(
		ImmutableMap.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST)
	);
	protected static final int H = 1;
	protected static final int W = 3;
	protected static final int E = 13;
	protected static final int N = 3;
	protected static final int S = 13;
	private static final VoxelShape SHAPE_DOT = Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 13.0);
	private static final Map<Direction, VoxelShape> SHAPES_FLOOR = Maps.newEnumMap(
		ImmutableMap.of(
			Direction.NORTH,
			Block.box(3.0, 0.0, 0.0, 13.0, 1.0, 13.0),
			Direction.SOUTH,
			Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 16.0),
			Direction.EAST,
			Block.box(3.0, 0.0, 3.0, 16.0, 1.0, 13.0),
			Direction.WEST,
			Block.box(0.0, 0.0, 3.0, 13.0, 1.0, 13.0)
		)
	);
	private static final Map<Direction, VoxelShape> SHAPES_UP = Maps.newEnumMap(
		ImmutableMap.of(
			Direction.NORTH,
			Shapes.or((VoxelShape)SHAPES_FLOOR.get(Direction.NORTH), Block.box(3.0, 0.0, 0.0, 13.0, 16.0, 1.0)),
			Direction.SOUTH,
			Shapes.or((VoxelShape)SHAPES_FLOOR.get(Direction.SOUTH), Block.box(3.0, 0.0, 15.0, 13.0, 16.0, 16.0)),
			Direction.EAST,
			Shapes.or((VoxelShape)SHAPES_FLOOR.get(Direction.EAST), Block.box(15.0, 0.0, 3.0, 16.0, 16.0, 13.0)),
			Direction.WEST,
			Shapes.or((VoxelShape)SHAPES_FLOOR.get(Direction.WEST), Block.box(0.0, 0.0, 3.0, 1.0, 16.0, 13.0))
		)
	);
	private static final Map<BlockState, VoxelShape> SHAPES_CACHE = Maps.<BlockState, VoxelShape>newHashMap();
	private static final int[] COLORS = Util.make(new int[16], is -> {
		for (int i = 0; i <= 15; i++) {
			float f = (float)i / 15.0F;
			float g = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
			float h = Mth.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
			float j = Mth.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
			is[i] = ARGB.colorFromFloat(1.0F, g, h, j);
		}
	});
	private static final float PARTICLE_DENSITY = 0.2F;
	private final BlockState crossState;
	private final RedstoneWireEvaluator evaluator = new DefaultRedstoneWireEvaluator(this);
	private boolean shouldSignal = true;

	@Override
	public MapCodec<RedStoneWireBlock> codec() {
		return CODEC;
	}

	public RedStoneWireBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(NORTH, RedstoneSide.NONE)
				.setValue(EAST, RedstoneSide.NONE)
				.setValue(SOUTH, RedstoneSide.NONE)
				.setValue(WEST, RedstoneSide.NONE)
				.setValue(POWER, Integer.valueOf(0))
		);
		this.crossState = this.defaultBlockState()
			.setValue(NORTH, RedstoneSide.SIDE)
			.setValue(EAST, RedstoneSide.SIDE)
			.setValue(SOUTH, RedstoneSide.SIDE)
			.setValue(WEST, RedstoneSide.SIDE);

		for (BlockState blockState : this.getStateDefinition().getPossibleStates()) {
			if ((Integer)blockState.getValue(POWER) == 0) {
				SHAPES_CACHE.put(blockState, this.calculateShape(blockState));
			}
		}
	}

	private VoxelShape calculateShape(BlockState blockState) {
		VoxelShape voxelShape = SHAPE_DOT;

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			RedstoneSide redstoneSide = blockState.getValue((Property<RedstoneSide>)PROPERTY_BY_DIRECTION.get(direction));
			if (redstoneSide == RedstoneSide.SIDE) {
				voxelShape = Shapes.or(voxelShape, (VoxelShape)SHAPES_FLOOR.get(direction));
			} else if (redstoneSide == RedstoneSide.UP) {
				voxelShape = Shapes.or(voxelShape, (VoxelShape)SHAPES_UP.get(direction));
			}
		}

		return voxelShape;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return (VoxelShape)SHAPES_CACHE.get(blockState.setValue(POWER, Integer.valueOf(0)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.getConnectionState(blockPlaceContext.getLevel(), this.crossState, blockPlaceContext.getClickedPos());
	}

	private BlockState getConnectionState(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos) {
		boolean bl = isDot(blockState);
		blockState = this.getMissingConnections(blockGetter, this.defaultBlockState().setValue(POWER, (Integer)blockState.getValue(POWER)), blockPos);
		if (bl && isDot(blockState)) {
			return blockState;
		} else {
			boolean bl2 = ((RedstoneSide)blockState.getValue(NORTH)).isConnected();
			boolean bl3 = ((RedstoneSide)blockState.getValue(SOUTH)).isConnected();
			boolean bl4 = ((RedstoneSide)blockState.getValue(EAST)).isConnected();
			boolean bl5 = ((RedstoneSide)blockState.getValue(WEST)).isConnected();
			boolean bl6 = !bl2 && !bl3;
			boolean bl7 = !bl4 && !bl5;
			if (!bl5 && bl6) {
				blockState = blockState.setValue(WEST, RedstoneSide.SIDE);
			}

			if (!bl4 && bl6) {
				blockState = blockState.setValue(EAST, RedstoneSide.SIDE);
			}

			if (!bl2 && bl7) {
				blockState = blockState.setValue(NORTH, RedstoneSide.SIDE);
			}

			if (!bl3 && bl7) {
				blockState = blockState.setValue(SOUTH, RedstoneSide.SIDE);
			}

			return blockState;
		}
	}

	private BlockState getMissingConnections(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos) {
		boolean bl = !blockGetter.getBlockState(blockPos.above()).isRedstoneConductor(blockGetter, blockPos);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (!((RedstoneSide)blockState.getValue((Property)PROPERTY_BY_DIRECTION.get(direction))).isConnected()) {
				RedstoneSide redstoneSide = this.getConnectingSide(blockGetter, blockPos, direction, bl);
				blockState = blockState.setValue((Property)PROPERTY_BY_DIRECTION.get(direction), redstoneSide);
			}
		}

		return blockState;
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
		if (direction == Direction.DOWN) {
			return !this.canSurviveOn(levelReader, blockPos2, blockState2) ? Blocks.AIR.defaultBlockState() : blockState;
		} else if (direction == Direction.UP) {
			return this.getConnectionState(levelReader, blockState, blockPos);
		} else {
			RedstoneSide redstoneSide = this.getConnectingSide(levelReader, blockPos, direction);
			return redstoneSide.isConnected() == ((RedstoneSide)blockState.getValue((Property)PROPERTY_BY_DIRECTION.get(direction))).isConnected()
					&& !isCross(blockState)
				? blockState.setValue((Property)PROPERTY_BY_DIRECTION.get(direction), redstoneSide)
				: this.getConnectionState(
					levelReader,
					this.crossState.setValue(POWER, (Integer)blockState.getValue(POWER)).setValue((Property)PROPERTY_BY_DIRECTION.get(direction), redstoneSide),
					blockPos
				);
		}
	}

	private static boolean isCross(BlockState blockState) {
		return ((RedstoneSide)blockState.getValue(NORTH)).isConnected()
			&& ((RedstoneSide)blockState.getValue(SOUTH)).isConnected()
			&& ((RedstoneSide)blockState.getValue(EAST)).isConnected()
			&& ((RedstoneSide)blockState.getValue(WEST)).isConnected();
	}

	private static boolean isDot(BlockState blockState) {
		return !((RedstoneSide)blockState.getValue(NORTH)).isConnected()
			&& !((RedstoneSide)blockState.getValue(SOUTH)).isConnected()
			&& !((RedstoneSide)blockState.getValue(EAST)).isConnected()
			&& !((RedstoneSide)blockState.getValue(WEST)).isConnected();
	}

	@Override
	protected void updateIndirectNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, int i, int j) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			RedstoneSide redstoneSide = blockState.getValue((Property<RedstoneSide>)PROPERTY_BY_DIRECTION.get(direction));
			if (redstoneSide != RedstoneSide.NONE && !levelAccessor.getBlockState(mutableBlockPos.setWithOffset(blockPos, direction)).is(this)) {
				mutableBlockPos.move(Direction.DOWN);
				BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos);
				if (blockState2.is(this)) {
					BlockPos blockPos2 = mutableBlockPos.relative(direction.getOpposite());
					levelAccessor.neighborShapeChanged(direction.getOpposite(), mutableBlockPos, blockPos2, levelAccessor.getBlockState(blockPos2), i, j);
				}

				mutableBlockPos.setWithOffset(blockPos, direction).move(Direction.UP);
				BlockState blockState3 = levelAccessor.getBlockState(mutableBlockPos);
				if (blockState3.is(this)) {
					BlockPos blockPos3 = mutableBlockPos.relative(direction.getOpposite());
					levelAccessor.neighborShapeChanged(direction.getOpposite(), mutableBlockPos, blockPos3, levelAccessor.getBlockState(blockPos3), i, j);
				}
			}
		}
	}

	private RedstoneSide getConnectingSide(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return this.getConnectingSide(blockGetter, blockPos, direction, !blockGetter.getBlockState(blockPos.above()).isRedstoneConductor(blockGetter, blockPos));
	}

	private RedstoneSide getConnectingSide(BlockGetter blockGetter, BlockPos blockPos, Direction direction, boolean bl) {
		BlockPos blockPos2 = blockPos.relative(direction);
		BlockState blockState = blockGetter.getBlockState(blockPos2);
		if (bl) {
			boolean bl2 = blockState.getBlock() instanceof TrapDoorBlock || this.canSurviveOn(blockGetter, blockPos2, blockState);
			if (bl2 && shouldConnectTo(blockGetter.getBlockState(blockPos2.above()))) {
				if (blockState.isFaceSturdy(blockGetter, blockPos2, direction.getOpposite())) {
					return RedstoneSide.UP;
				}

				return RedstoneSide.SIDE;
			}
		}

		return !shouldConnectTo(blockState, direction)
				&& (blockState.isRedstoneConductor(blockGetter, blockPos2) || !shouldConnectTo(blockGetter.getBlockState(blockPos2.below())))
			? RedstoneSide.NONE
			: RedstoneSide.SIDE;
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState2 = levelReader.getBlockState(blockPos2);
		return this.canSurviveOn(levelReader, blockPos2, blockState2);
	}

	private boolean canSurviveOn(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		return blockState.isFaceSturdy(blockGetter, blockPos, Direction.UP) || blockState.is(Blocks.HOPPER);
	}

	private void updatePowerStrength(Level level, BlockPos blockPos, BlockState blockState, @Nullable Orientation orientation, boolean bl) {
		if (useExperimentalEvaluator(level)) {
			new ExperimentalRedstoneWireEvaluator(this).updatePowerStrength(level, blockPos, blockState, orientation, bl);
		} else {
			this.evaluator.updatePowerStrength(level, blockPos, blockState, orientation, bl);
		}
	}

	public int getBlockSignal(Level level, BlockPos blockPos) {
		this.shouldSignal = false;
		int i = level.getBestNeighborSignal(blockPos);
		this.shouldSignal = true;
		return i;
	}

	private void checkCornerChangeAt(Level level, BlockPos blockPos) {
		if (level.getBlockState(blockPos).is(this)) {
			level.updateNeighborsAt(blockPos, this);

			for (Direction direction : Direction.values()) {
				level.updateNeighborsAt(blockPos.relative(direction), this);
			}
		}
	}

	@Override
	protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState2.is(blockState.getBlock()) && !level.isClientSide) {
			this.updatePowerStrength(level, blockPos, blockState, null, true);

			for (Direction direction : Direction.Plane.VERTICAL) {
				level.updateNeighborsAt(blockPos.relative(direction), this);
			}

			this.updateNeighborsOfNeighboringWires(level, blockPos);
		}
	}

	@Override
	protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!bl && !blockState.is(blockState2.getBlock())) {
			super.onRemove(blockState, level, blockPos, blockState2, bl);
			if (!level.isClientSide) {
				for (Direction direction : Direction.values()) {
					level.updateNeighborsAt(blockPos.relative(direction), this);
				}

				this.updatePowerStrength(level, blockPos, blockState, null, false);
				this.updateNeighborsOfNeighboringWires(level, blockPos);
			}
		}
	}

	private void updateNeighborsOfNeighboringWires(Level level, BlockPos blockPos) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			this.checkCornerChangeAt(level, blockPos.relative(direction));
		}

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos blockPos2 = blockPos.relative(direction);
			if (level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)) {
				this.checkCornerChangeAt(level, blockPos2.above());
			} else {
				this.checkCornerChangeAt(level, blockPos2.below());
			}
		}
	}

	@Override
	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
		if (!level.isClientSide) {
			if (block != this || !useExperimentalEvaluator(level)) {
				if (blockState.canSurvive(level, blockPos)) {
					this.updatePowerStrength(level, blockPos, blockState, orientation, false);
				} else {
					dropResources(blockState, level, blockPos);
					level.removeBlock(blockPos, false);
				}
			}
		}
	}

	private static boolean useExperimentalEvaluator(Level level) {
		return level.enabledFeatures().contains(FeatureFlags.REDSTONE_EXPERIMENTS);
	}

	@Override
	protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return !this.shouldSignal ? 0 : blockState.getSignal(blockGetter, blockPos, direction);
	}

	@Override
	protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		if (this.shouldSignal && direction != Direction.DOWN) {
			int i = (Integer)blockState.getValue(POWER);
			if (i == 0) {
				return 0;
			} else {
				return direction != Direction.UP
						&& !((RedstoneSide)this.getConnectionState(blockGetter, blockState, blockPos).getValue((Property)PROPERTY_BY_DIRECTION.get(direction.getOpposite())))
							.isConnected()
					? 0
					: i;
			}
		} else {
			return 0;
		}
	}

	protected static boolean shouldConnectTo(BlockState blockState) {
		return shouldConnectTo(blockState, null);
	}

	protected static boolean shouldConnectTo(BlockState blockState, @Nullable Direction direction) {
		if (blockState.is(Blocks.REDSTONE_WIRE)) {
			return true;
		} else if (blockState.is(Blocks.REPEATER)) {
			Direction direction2 = blockState.getValue(RepeaterBlock.FACING);
			return direction2 == direction || direction2.getOpposite() == direction;
		} else {
			return blockState.is(Blocks.OBSERVER) ? direction == blockState.getValue(ObserverBlock.FACING) : blockState.isSignalSource() && direction != null;
		}
	}

	@Override
	protected boolean isSignalSource(BlockState blockState) {
		return this.shouldSignal;
	}

	public static int getColorForPower(int i) {
		return COLORS[i];
	}

	private static void spawnParticlesAlongLine(
		Level level, RandomSource randomSource, BlockPos blockPos, int i, Direction direction, Direction direction2, float f, float g
	) {
		float h = g - f;
		if (!(randomSource.nextFloat() >= 0.2F * h)) {
			float j = 0.4375F;
			float k = f + h * randomSource.nextFloat();
			double d = 0.5 + (double)(0.4375F * (float)direction.getStepX()) + (double)(k * (float)direction2.getStepX());
			double e = 0.5 + (double)(0.4375F * (float)direction.getStepY()) + (double)(k * (float)direction2.getStepY());
			double l = 0.5 + (double)(0.4375F * (float)direction.getStepZ()) + (double)(k * (float)direction2.getStepZ());
			level.addParticle(new DustParticleOptions(i, 1.0F), (double)blockPos.getX() + d, (double)blockPos.getY() + e, (double)blockPos.getZ() + l, 0.0, 0.0, 0.0);
		}
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		int i = (Integer)blockState.getValue(POWER);
		if (i != 0) {
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				RedstoneSide redstoneSide = blockState.getValue((Property<RedstoneSide>)PROPERTY_BY_DIRECTION.get(direction));
				switch (redstoneSide) {
					case UP:
						spawnParticlesAlongLine(level, randomSource, blockPos, COLORS[i], direction, Direction.UP, -0.5F, 0.5F);
					case SIDE:
						spawnParticlesAlongLine(level, randomSource, blockPos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.5F);
						break;
					case NONE:
					default:
						spawnParticlesAlongLine(level, randomSource, blockPos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.3F);
				}
			}
		}
	}

	@Override
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		switch (rotation) {
			case CLOCKWISE_180:
				return blockState.setValue(NORTH, (RedstoneSide)blockState.getValue(SOUTH))
					.setValue(EAST, (RedstoneSide)blockState.getValue(WEST))
					.setValue(SOUTH, (RedstoneSide)blockState.getValue(NORTH))
					.setValue(WEST, (RedstoneSide)blockState.getValue(EAST));
			case COUNTERCLOCKWISE_90:
				return blockState.setValue(NORTH, (RedstoneSide)blockState.getValue(EAST))
					.setValue(EAST, (RedstoneSide)blockState.getValue(SOUTH))
					.setValue(SOUTH, (RedstoneSide)blockState.getValue(WEST))
					.setValue(WEST, (RedstoneSide)blockState.getValue(NORTH));
			case CLOCKWISE_90:
				return blockState.setValue(NORTH, (RedstoneSide)blockState.getValue(WEST))
					.setValue(EAST, (RedstoneSide)blockState.getValue(NORTH))
					.setValue(SOUTH, (RedstoneSide)blockState.getValue(EAST))
					.setValue(WEST, (RedstoneSide)blockState.getValue(SOUTH));
			default:
				return blockState;
		}
	}

	@Override
	protected BlockState mirror(BlockState blockState, Mirror mirror) {
		switch (mirror) {
			case LEFT_RIGHT:
				return blockState.setValue(NORTH, (RedstoneSide)blockState.getValue(SOUTH)).setValue(SOUTH, (RedstoneSide)blockState.getValue(NORTH));
			case FRONT_BACK:
				return blockState.setValue(EAST, (RedstoneSide)blockState.getValue(WEST)).setValue(WEST, (RedstoneSide)blockState.getValue(EAST));
			default:
				return super.mirror(blockState, mirror);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, POWER);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (!player.getAbilities().mayBuild) {
			return InteractionResult.PASS;
		} else {
			if (isCross(blockState) || isDot(blockState)) {
				BlockState blockState2 = isCross(blockState) ? this.defaultBlockState() : this.crossState;
				blockState2 = blockState2.setValue(POWER, (Integer)blockState.getValue(POWER));
				blockState2 = this.getConnectionState(level, blockState2, blockPos);
				if (blockState2 != blockState) {
					level.setBlock(blockPos, blockState2, 3);
					this.updatesOnShapeChange(level, blockPos, blockState, blockState2);
					return InteractionResult.SUCCESS;
				}
			}

			return InteractionResult.PASS;
		}
	}

	private void updatesOnShapeChange(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2) {
		Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, null, Direction.UP);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos blockPos2 = blockPos.relative(direction);
			if (((RedstoneSide)blockState.getValue((Property)PROPERTY_BY_DIRECTION.get(direction))).isConnected()
					!= ((RedstoneSide)blockState2.getValue((Property)PROPERTY_BY_DIRECTION.get(direction))).isConnected()
				&& level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)) {
				level.updateNeighborsAtExceptFromFacing(
					blockPos2, blockState2.getBlock(), direction.getOpposite(), ExperimentalRedstoneUtils.withFront(orientation, direction)
				);
			}
		}
	}
}
