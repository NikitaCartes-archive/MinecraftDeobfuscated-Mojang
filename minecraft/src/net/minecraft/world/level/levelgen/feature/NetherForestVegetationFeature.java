package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;

public class NetherForestVegetationFeature extends Feature<BlockPileConfiguration> {
	public NetherForestVegetationFeature(Codec<BlockPileConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<BlockPileConfiguration> featurePlaceContext) {
		return place(featurePlaceContext.level(), featurePlaceContext.random(), featurePlaceContext.origin(), featurePlaceContext.config(), 8, 4);
	}

	public static boolean place(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BlockPileConfiguration blockPileConfiguration, int i, int j) {
		BlockState blockState = levelAccessor.getBlockState(blockPos.below());
		if (!blockState.is(BlockTags.NYLIUM)) {
			return false;
		} else {
			int k = blockPos.getY();
			if (k >= levelAccessor.getMinBuildHeight() + 1 && k + 1 < levelAccessor.getMaxBuildHeight()) {
				int l = 0;

				for (int m = 0; m < i * i; m++) {
					BlockPos blockPos2 = blockPos.offset(random.nextInt(i) - random.nextInt(i), random.nextInt(j) - random.nextInt(j), random.nextInt(i) - random.nextInt(i));
					BlockState blockState2 = blockPileConfiguration.stateProvider.getState(random, blockPos2);
					if (levelAccessor.isEmptyBlock(blockPos2) && blockPos2.getY() > levelAccessor.getMinBuildHeight() && blockState2.canSurvive(levelAccessor, blockPos2)) {
						levelAccessor.setBlock(blockPos2, blockState2, 2);
						l++;
					}
				}

				return l > 0;
			} else {
				return false;
			}
		}
	}
}
