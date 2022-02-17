package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.CentralBlockScatteredFeaturesConfiguration;

public class CentralBlockScatteredFeaturesFeature extends Feature<CentralBlockScatteredFeaturesConfiguration> {
	public CentralBlockScatteredFeaturesFeature(Codec<CentralBlockScatteredFeaturesConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<CentralBlockScatteredFeaturesConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		CentralBlockScatteredFeaturesConfiguration centralBlockScatteredFeaturesConfiguration = featurePlaceContext.config();
		Random random = featurePlaceContext.random();
		BlockPos blockPos = featurePlaceContext.origin();
		Predicate<BlockState> predicate = getTag(centralBlockScatteredFeaturesConfiguration.canPlaceCentralBlockOn);
		BlockState blockState = worldGenLevel.getBlockState(blockPos.below());
		if (predicate.test(blockState)) {
			worldGenLevel.setBlock(blockPos, centralBlockScatteredFeaturesConfiguration.centralState.getState(random, blockPos), 2);
			((ConfiguredFeature)centralBlockScatteredFeaturesConfiguration.centralFeature.get())
				.place(worldGenLevel, featurePlaceContext.chunkGenerator(), random, blockPos);
			float f = (float)centralBlockScatteredFeaturesConfiguration.maxFeatureDistance / 3.0F;
			int i = centralBlockScatteredFeaturesConfiguration.maxFeatureDistance * centralBlockScatteredFeaturesConfiguration.maxFeatureDistance;
			ConfiguredFeature<?, ?> configuredFeature = (ConfiguredFeature<?, ?>)centralBlockScatteredFeaturesConfiguration.scatteredFeature.get();
			int j = Mth.randomBetweenInclusive(
				random, centralBlockScatteredFeaturesConfiguration.featureCountMin, centralBlockScatteredFeaturesConfiguration.featureCountMax
			);

			for (int k = 0; k < j; k++) {
				double d = random.nextGaussian();
				double e = random.nextGaussian();
				BlockPos blockPos2 = blockPos.offset(d * (double)f, 0.0, e * (double)f);
				if (blockPos2.distSqr(blockPos, true) <= (double)i) {
					configuredFeature.place(worldGenLevel, featurePlaceContext.chunkGenerator(), random, blockPos2);
				}
			}

			return true;
		} else {
			return false;
		}
	}

	private static Predicate<BlockState> getTag(ResourceLocation resourceLocation) {
		Tag<Block> tag = BlockTags.getAllTags().getTag(resourceLocation);
		return tag == null ? blockState -> true : blockState -> blockState.is(tag);
	}
}
