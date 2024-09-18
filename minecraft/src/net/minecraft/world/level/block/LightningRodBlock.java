package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;

public class LightningRodBlock extends RodBlock implements SimpleWaterloggedBlock {
	public static final MapCodec<LightningRodBlock> CODEC = simpleCodec(LightningRodBlock::new);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	private static final int ACTIVATION_TICKS = 8;
	public static final int RANGE = 128;
	private static final int SPARK_CYCLE = 200;

	@Override
	public MapCodec<LightningRodBlock> codec() {
		return CODEC;
	}

	public LightningRodBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(POWERED, Boolean.valueOf(false))
		);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
		boolean bl = fluidState.getType() == Fluids.WATER;
		return this.defaultBlockState().setValue(FACING, blockPlaceContext.getClickedFace()).setValue(WATERLOGGED, Boolean.valueOf(bl));
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
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
		}

		return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
	}

	@Override
	protected FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return blockState.getValue(POWERED) ? 15 : 0;
	}

	@Override
	protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return blockState.getValue(POWERED) && blockState.getValue(FACING) == direction ? 15 : 0;
	}

	public void onLightningStrike(BlockState blockState, Level level, BlockPos blockPos) {
		level.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(true)), 3);
		this.updateNeighbours(blockState, level, blockPos);
		level.scheduleTick(blockPos, this, 8);
		level.levelEvent(3002, blockPos, ((Direction)blockState.getValue(FACING)).getAxis().ordinal());
	}

	private void updateNeighbours(BlockState blockState, Level level, BlockPos blockPos) {
		Direction direction = ((Direction)blockState.getValue(FACING)).getOpposite();
		level.updateNeighborsAt(blockPos.relative(direction), this, ExperimentalRedstoneUtils.initialOrientation(level, direction, null));
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		serverLevel.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(false)), 3);
		this.updateNeighbours(blockState, serverLevel, blockPos);
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		if (level.isThundering()
			&& (long)level.random.nextInt(200) <= level.getGameTime() % 200L
			&& blockPos.getY() == level.getHeight(Heightmap.Types.WORLD_SURFACE, blockPos.getX(), blockPos.getZ()) - 1) {
			ParticleUtils.spawnParticlesAlongAxis(
				((Direction)blockState.getValue(FACING)).getAxis(), level, blockPos, 0.125, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(1, 2)
			);
		}
	}

	@Override
	protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			if ((Boolean)blockState.getValue(POWERED)) {
				this.updateNeighbours(blockState, level, blockPos);
			}

			super.onRemove(blockState, level, blockPos, blockState2, bl);
		}
	}

	@Override
	protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			if ((Boolean)blockState.getValue(POWERED) && !level.getBlockTicks().hasScheduledTick(blockPos, this)) {
				level.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(false)), 18);
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED, WATERLOGGED);
	}

	@Override
	protected boolean isSignalSource(BlockState blockState) {
		return true;
	}
}
