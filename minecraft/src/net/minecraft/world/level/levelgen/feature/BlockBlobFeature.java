package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class BlockBlobFeature extends Feature<BlockStateConfiguration> {
	public BlockBlobFeature(Codec<BlockStateConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<BlockStateConfiguration> featurePlaceContext) {
		BlockPos blockPos = featurePlaceContext.origin();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		Random random = featurePlaceContext.random();

		BlockStateConfiguration blockStateConfiguration;
		for (blockStateConfiguration = featurePlaceContext.config(); blockPos.getY() > worldGenLevel.getMinBuildHeight() + 3; blockPos = blockPos.below()) {
			if (!worldGenLevel.isEmptyBlock(blockPos.below())) {
				BlockState blockState = worldGenLevel.getBlockState(blockPos.below());
				if (isDirt(blockState) || isStone(blockState)) {
					break;
				}
			}
		}

		if (blockPos.getY() <= worldGenLevel.getMinBuildHeight() + 3) {
			return false;
		} else {
			for (int i = 0; i < 3; i++) {
				int j = random.nextInt(2);
				int k = random.nextInt(2);
				int l = random.nextInt(2);
				float f = (float)(j + k + l) * 0.333F + 0.5F;

				for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-j, -k, -l), blockPos.offset(j, k, l))) {
					if (blockPos2.distSqr(blockPos) <= (double)(f * f)) {
						worldGenLevel.setBlock(blockPos2, blockStateConfiguration.state, 4);
					}
				}

				blockPos = blockPos.offset(-1 + random.nextInt(2), -random.nextInt(2), -1 + random.nextInt(2));
			}

			return true;
		}
	}
}
