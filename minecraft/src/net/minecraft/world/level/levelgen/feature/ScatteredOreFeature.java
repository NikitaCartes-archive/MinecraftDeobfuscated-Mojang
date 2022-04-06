package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class ScatteredOreFeature extends Feature<OreConfiguration> {
	private static final int MAX_DIST_FROM_ORIGIN = 7;

	ScatteredOreFeature(Codec<OreConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<OreConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		RandomSource randomSource = featurePlaceContext.random();
		OreConfiguration oreConfiguration = featurePlaceContext.config();
		BlockPos blockPos = featurePlaceContext.origin();
		int i = randomSource.nextInt(oreConfiguration.size + 1);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int j = 0; j < i; j++) {
			this.offsetTargetPos(mutableBlockPos, randomSource, blockPos, Math.min(j, 7));
			BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos);

			for (OreConfiguration.TargetBlockState targetBlockState : oreConfiguration.targetStates) {
				if (OreFeature.canPlaceOre(blockState, worldGenLevel::getBlockState, randomSource, oreConfiguration, targetBlockState, mutableBlockPos)) {
					worldGenLevel.setBlock(mutableBlockPos, targetBlockState.state, 2);
					break;
				}
			}
		}

		return true;
	}

	private void offsetTargetPos(BlockPos.MutableBlockPos mutableBlockPos, RandomSource randomSource, BlockPos blockPos, int i) {
		int j = this.getRandomPlacementInOneAxisRelativeToOrigin(randomSource, i);
		int k = this.getRandomPlacementInOneAxisRelativeToOrigin(randomSource, i);
		int l = this.getRandomPlacementInOneAxisRelativeToOrigin(randomSource, i);
		mutableBlockPos.setWithOffset(blockPos, j, k, l);
	}

	private int getRandomPlacementInOneAxisRelativeToOrigin(RandomSource randomSource, int i) {
		return Math.round((randomSource.nextFloat() - randomSource.nextFloat()) * (float)i);
	}
}
