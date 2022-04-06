package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.features.NetherFeatures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.lighting.LayerLightEngine;

public class NyliumBlock extends Block implements BonemealableBlock {
	protected NyliumBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	private static boolean canBeNylium(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.above();
		BlockState blockState2 = levelReader.getBlockState(blockPos2);
		int i = LayerLightEngine.getLightBlockInto(
			levelReader, blockState, blockPos, blockState2, blockPos2, Direction.UP, blockState2.getLightBlock(levelReader, blockPos2)
		);
		return i < levelReader.getMaxLightLevel();
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (!canBeNylium(blockState, serverLevel, blockPos)) {
			serverLevel.setBlockAndUpdate(blockPos, Blocks.NETHERRACK.defaultBlockState());
		}
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
		return blockGetter.getBlockState(blockPos.above()).isAir();
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		BlockState blockState2 = serverLevel.getBlockState(blockPos);
		BlockPos blockPos2 = blockPos.above();
		ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
		if (blockState2.is(Blocks.CRIMSON_NYLIUM)) {
			NetherFeatures.CRIMSON_FOREST_VEGETATION_BONEMEAL.value().place(serverLevel, chunkGenerator, randomSource, blockPos2);
		} else if (blockState2.is(Blocks.WARPED_NYLIUM)) {
			NetherFeatures.WARPED_FOREST_VEGETATION_BONEMEAL.value().place(serverLevel, chunkGenerator, randomSource, blockPos2);
			NetherFeatures.NETHER_SPROUTS_BONEMEAL.value().place(serverLevel, chunkGenerator, randomSource, blockPos2);
			if (randomSource.nextInt(8) == 0) {
				NetherFeatures.TWISTING_VINES_BONEMEAL.value().place(serverLevel, chunkGenerator, randomSource, blockPos2);
			}
		}
	}
}
