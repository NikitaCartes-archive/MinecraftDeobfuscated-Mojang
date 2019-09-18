package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class BushBlock extends Block {
	protected BushBlock(Block.Properties properties) {
		super(properties);
	}

	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		Block block = blockState.getBlock();
		return block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.PODZOL || block == Blocks.FARMLAND;
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return !blockState.canSurvive(levelAccessor, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		return this.mayPlaceOn(levelReader.getBlockState(blockPos2), levelReader, blockPos2);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return true;
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return pathComputationType == PathComputationType.AIR && !this.hasCollision
			? true
			: super.isPathfindable(blockState, blockGetter, blockPos, pathComputationType);
	}
}
