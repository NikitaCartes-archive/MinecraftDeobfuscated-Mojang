package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class RootedDirtBlock extends Block implements BonemealableBlock {
	public static final MapCodec<RootedDirtBlock> CODEC = simpleCodec(RootedDirtBlock::new);

	@Override
	public MapCodec<RootedDirtBlock> codec() {
		return CODEC;
	}

	public RootedDirtBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return levelReader.getBlockState(blockPos.below()).isAir();
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		serverLevel.setBlockAndUpdate(blockPos.below(), Blocks.HANGING_ROOTS.defaultBlockState());
	}

	@Override
	public BlockPos getParticlePos(BlockPos blockPos) {
		return blockPos.below();
	}
}
