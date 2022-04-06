package net.minecraft.world.level.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class GrassBlock extends SpreadingSnowyDirtBlock implements BonemealableBlock {
	public GrassBlock(BlockBehaviour.Properties properties) {
		super(properties);
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
		BlockPos blockPos2 = blockPos.above();
		BlockState blockState2 = Blocks.GRASS.defaultBlockState();

		label46:
		for (int i = 0; i < 128; i++) {
			BlockPos blockPos3 = blockPos2;

			for (int j = 0; j < i / 16; j++) {
				blockPos3 = blockPos3.offset(randomSource.nextInt(3) - 1, (randomSource.nextInt(3) - 1) * randomSource.nextInt(3) / 2, randomSource.nextInt(3) - 1);
				if (!serverLevel.getBlockState(blockPos3.below()).is(this) || serverLevel.getBlockState(blockPos3).isCollisionShapeFullBlock(serverLevel, blockPos3)) {
					continue label46;
				}
			}

			BlockState blockState3 = serverLevel.getBlockState(blockPos3);
			if (blockState3.is(blockState2.getBlock()) && randomSource.nextInt(10) == 0) {
				((BonemealableBlock)blockState2.getBlock()).performBonemeal(serverLevel, randomSource, blockPos3, blockState3);
			}

			if (blockState3.isAir()) {
				Holder<PlacedFeature> holder;
				if (randomSource.nextInt(8) == 0) {
					List<ConfiguredFeature<?, ?>> list = serverLevel.getBiome(blockPos3).value().getGenerationSettings().getFlowerFeatures();
					if (list.isEmpty()) {
						continue;
					}

					holder = ((RandomPatchConfiguration)((ConfiguredFeature)list.get(0)).config()).feature();
				} else {
					holder = VegetationPlacements.GRASS_BONEMEAL;
				}

				holder.value().place(serverLevel, serverLevel.getChunkSource().getGenerator(), randomSource, blockPos3);
			}
		}
	}
}
