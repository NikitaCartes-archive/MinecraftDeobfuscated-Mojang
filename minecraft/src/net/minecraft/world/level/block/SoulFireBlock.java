package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SoulFireBlock extends BaseFireBlock {
	public static final MapCodec<SoulFireBlock> CODEC = simpleCodec(SoulFireBlock::new);

	@Override
	public MapCodec<SoulFireBlock> codec() {
		return CODEC;
	}

	public SoulFireBlock(BlockBehaviour.Properties properties) {
		super(properties, 2.0F);
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
		return this.canSurvive(blockState, levelReader, blockPos) ? this.defaultBlockState() : Blocks.AIR.defaultBlockState();
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
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
