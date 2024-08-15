package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LeavesBlock extends Block implements SimpleWaterloggedBlock {
	public static final MapCodec<LeavesBlock> CODEC = simpleCodec(LeavesBlock::new);
	public static final int DECAY_DISTANCE = 7;
	public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE;
	public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final int TICK_DELAY = 1;

	@Override
	public MapCodec<? extends LeavesBlock> codec() {
		return CODEC;
	}

	public LeavesBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(DISTANCE, Integer.valueOf(7)).setValue(PERSISTENT, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false))
		);
	}

	@Override
	protected VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return Shapes.empty();
	}

	@Override
	protected boolean isRandomlyTicking(BlockState blockState) {
		return (Integer)blockState.getValue(DISTANCE) == 7 && !(Boolean)blockState.getValue(PERSISTENT);
	}

	@Override
	protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (this.decaying(blockState)) {
			dropResources(blockState, serverLevel, blockPos);
			serverLevel.removeBlock(blockPos, false);
		}
	}

	protected boolean decaying(BlockState blockState) {
		return !(Boolean)blockState.getValue(PERSISTENT) && (Integer)blockState.getValue(DISTANCE) == 7;
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		serverLevel.setBlock(blockPos, updateDistance(blockState, serverLevel, blockPos), 3);
	}

	@Override
	protected int getLightBlock(BlockState blockState) {
		return 1;
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		int i = getDistanceAt(blockState2) + 1;
		if (i != 1 || (Integer)blockState.getValue(DISTANCE) != i) {
			levelAccessor.scheduleTick(blockPos, this, 1);
		}

		return blockState;
	}

	private static BlockState updateDistance(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
		int i = 7;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Direction direction : Direction.values()) {
			mutableBlockPos.setWithOffset(blockPos, direction);
			i = Math.min(i, getDistanceAt(levelAccessor.getBlockState(mutableBlockPos)) + 1);
			if (i == 1) {
				break;
			}
		}

		return blockState.setValue(DISTANCE, Integer.valueOf(i));
	}

	private static int getDistanceAt(BlockState blockState) {
		return getOptionalDistanceAt(blockState).orElse(7);
	}

	public static OptionalInt getOptionalDistanceAt(BlockState blockState) {
		if (blockState.is(BlockTags.LOGS)) {
			return OptionalInt.of(0);
		} else {
			return blockState.hasProperty(DISTANCE) ? OptionalInt.of((Integer)blockState.getValue(DISTANCE)) : OptionalInt.empty();
		}
	}

	@Override
	protected FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		if (level.isRainingAt(blockPos.above())) {
			if (randomSource.nextInt(15) == 1) {
				BlockPos blockPos2 = blockPos.below();
				BlockState blockState2 = level.getBlockState(blockPos2);
				if (!blockState2.canOcclude() || !blockState2.isFaceSturdy(level, blockPos2, Direction.UP)) {
					ParticleUtils.spawnParticleBelow(level, blockPos, randomSource, ParticleTypes.DRIPPING_WATER);
				}
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(DISTANCE, PERSISTENT, WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
		BlockState blockState = this.defaultBlockState()
			.setValue(PERSISTENT, Boolean.valueOf(true))
			.setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
		return updateDistance(blockState, blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
	}
}
