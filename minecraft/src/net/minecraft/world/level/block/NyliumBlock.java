package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.NetherForestVegetationFeature;
import net.minecraft.world.level.levelgen.feature.TwistingVinesFeature;
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
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (!canBeNylium(blockState, serverLevel, blockPos)) {
			serverLevel.setBlockAndUpdate(blockPos, Blocks.NETHERRACK.defaultBlockState());
		}
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
		return blockGetter.getBlockState(blockPos.above()).isAir();
	}

	@Override
	public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
		Block block = serverLevel.getBlockState(blockPos).getBlock();
		BlockPos blockPos2 = blockPos.above();
		if (block == Blocks.CRIMSON_NYLIUM) {
			NetherForestVegetationFeature.place(serverLevel, random, blockPos2, BiomeDefaultFeatures.CRIMSON_FOREST_CONFIG, 3, 1);
		} else if (block == Blocks.WARPED_NYLIUM) {
			NetherForestVegetationFeature.place(serverLevel, random, blockPos2, BiomeDefaultFeatures.WARPED_FOREST_CONFIG, 3, 1);
			NetherForestVegetationFeature.place(serverLevel, random, blockPos2, BiomeDefaultFeatures.NETHER_SPROUTS_CONFIG, 3, 1);
			if (random.nextInt(8) == 0) {
				TwistingVinesFeature.place(serverLevel, random, blockPos2, 3, 1, 2);
			}
		}
	}
}
