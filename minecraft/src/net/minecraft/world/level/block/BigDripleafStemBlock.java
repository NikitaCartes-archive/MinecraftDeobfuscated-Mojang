package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BigDripleafStemBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final VoxelShape NORTH_SHAPE = Block.box(5.0, 0.0, 8.0, 11.0, 16.0, 14.0);
	private static final VoxelShape SOUTH_SHAPE = Block.box(5.0, 0.0, 2.0, 11.0, 16.0, 8.0);
	private static final VoxelShape EAST_SHAPE = Block.box(2.0, 0.0, 5.0, 8.0, 16.0, 11.0);
	private static final VoxelShape WEST_SHAPE = Block.box(8.0, 0.0, 5.0, 14.0, 16.0, 11.0);

	protected BigDripleafStemBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.NORTH));
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		switch ((Direction)blockState.getValue(FACING)) {
			case SOUTH:
				return SOUTH_SHAPE;
			case NORTH:
			default:
				return NORTH_SHAPE;
			case WEST:
				return WEST_SHAPE;
			case EAST:
				return EAST_SHAPE;
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED, FACING);
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState2 = levelReader.getBlockState(blockPos2);
		BlockState blockState3 = levelReader.getBlockState(blockPos.above());
		Block block = blockState3.getBlock();
		return (blockState2.is(this) || blockState2.isFaceSturdy(levelReader, blockPos2, Direction.UP)) && (block == this || block == Blocks.BIG_DRIPLEAF);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (!blockState.canSurvive(levelAccessor, blockPos)) {
			levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
		}

		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (!blockState.canSurvive(serverLevel, blockPos)) {
			serverLevel.destroyBlock(blockPos, true);
		}
	}
}
