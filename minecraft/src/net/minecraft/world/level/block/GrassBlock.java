package net.minecraft.world.level.block;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.AbstractFlowerFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class GrassBlock extends SpreadingSnowyDirtBlock implements BonemealableBlock {
	public GrassBlock(BlockBehaviour.Properties properties) {
		super(properties);
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
		BlockPos blockPos2 = blockPos.above();
		BlockState blockState2 = Blocks.GRASS.defaultBlockState();

		label48:
		for (int i = 0; i < 128; i++) {
			BlockPos blockPos3 = blockPos2;

			for (int j = 0; j < i / 16; j++) {
				blockPos3 = blockPos3.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
				if (!serverLevel.getBlockState(blockPos3.below()).is(this) || serverLevel.getBlockState(blockPos3).isCollisionShapeFullBlock(serverLevel, blockPos3)) {
					continue label48;
				}
			}

			BlockState blockState3 = serverLevel.getBlockState(blockPos3);
			if (blockState3.is(blockState2.getBlock()) && random.nextInt(10) == 0) {
				((BonemealableBlock)blockState2.getBlock()).performBonemeal(serverLevel, random, blockPos3, blockState3);
			}

			if (blockState3.isAir()) {
				BlockState blockState4;
				if (random.nextInt(8) == 0) {
					List<ConfiguredFeature<?, ?>> list = serverLevel.getBiome(blockPos3).getGenerationSettings().getFlowerFeatures();
					if (list.isEmpty()) {
						continue;
					}

					ConfiguredFeature<?, ?> configuredFeature = (ConfiguredFeature<?, ?>)list.get(0);
					AbstractFlowerFeature abstractFlowerFeature = (AbstractFlowerFeature)configuredFeature.feature;
					blockState4 = abstractFlowerFeature.getRandomFlower(random, blockPos3, configuredFeature.config());
				} else {
					blockState4 = blockState2;
				}

				if (blockState4.canSurvive(serverLevel, blockPos3)) {
					serverLevel.setBlock(blockPos3, blockState4, 3);
				}
			}
		}
	}
}
