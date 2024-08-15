package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;

public class ObserverBlock extends DirectionalBlock {
	public static final MapCodec<ObserverBlock> CODEC = simpleCodec(ObserverBlock::new);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	@Override
	public MapCodec<ObserverBlock> codec() {
		return CODEC;
	}

	public ObserverBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(POWERED, Boolean.valueOf(false)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED);
	}

	@Override
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if ((Boolean)blockState.getValue(POWERED)) {
			serverLevel.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(false)), 2);
		} else {
			serverLevel.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(true)), 2);
			serverLevel.scheduleTick(blockPos, this, 2);
		}

		this.updateNeighborsInFront(serverLevel, blockPos, blockState);
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (blockState.getValue(FACING) == direction && !(Boolean)blockState.getValue(POWERED)) {
			this.startSignal(levelAccessor, blockPos);
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	private void startSignal(LevelAccessor levelAccessor, BlockPos blockPos) {
		if (!levelAccessor.isClientSide() && !levelAccessor.getBlockTicks().hasScheduledTick(blockPos, this)) {
			levelAccessor.scheduleTick(blockPos, this, 2);
		}
	}

	protected void updateNeighborsInFront(Level level, BlockPos blockPos, BlockState blockState) {
		Direction direction = blockState.getValue(FACING);
		BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
		Orientation orientation = ExperimentalRedstoneUtils.randomOrientation(level, direction.getOpposite(), null);
		level.neighborChanged(blockPos2, this, orientation);
		level.updateNeighborsAtExceptFromFacing(blockPos2, this, direction, orientation);
	}

	@Override
	protected boolean isSignalSource(BlockState blockState) {
		return true;
	}

	@Override
	protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return blockState.getSignal(blockGetter, blockPos, direction);
	}

	@Override
	protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return blockState.getValue(POWERED) && blockState.getValue(FACING) == direction ? 15 : 0;
	}

	@Override
	protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			if (!level.isClientSide() && (Boolean)blockState.getValue(POWERED) && !level.getBlockTicks().hasScheduledTick(blockPos, this)) {
				BlockState blockState3 = blockState.setValue(POWERED, Boolean.valueOf(false));
				level.setBlock(blockPos, blockState3, 18);
				this.updateNeighborsInFront(level, blockPos, blockState3);
			}
		}
	}

	@Override
	protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			if (!level.isClientSide && (Boolean)blockState.getValue(POWERED) && level.getBlockTicks().hasScheduledTick(blockPos, this)) {
				this.updateNeighborsInFront(level, blockPos, blockState.setValue(POWERED, Boolean.valueOf(false)));
			}
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(FACING, blockPlaceContext.getNearestLookingDirection().getOpposite().getOpposite());
	}
}
