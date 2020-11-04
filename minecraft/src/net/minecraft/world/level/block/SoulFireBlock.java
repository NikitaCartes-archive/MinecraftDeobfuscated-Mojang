package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SoulFireBlock extends BaseFireBlock {
	public SoulFireBlock(BlockBehaviour.Properties properties) {
		super(properties, 2.0F);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return this.canSurvive(blockState, levelAccessor, blockPos) ? this.defaultBlockState() : Blocks.AIR.defaultBlockState();
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return canSurviveOnBlock(levelReader.getBlockState(blockPos.below()));
	}

	public static boolean canSurviveOnBlock(BlockState blockState) {
		return blockState.is(BlockTags.SOUL_FIRE_BASE_BLOCKS);
	}

	@Override
	protected boolean canBurn(BlockState blockState) {
		return true;
	}
}
