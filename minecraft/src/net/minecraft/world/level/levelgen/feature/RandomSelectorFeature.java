package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;

public class RandomSelectorFeature extends Feature<RandomFeatureConfiguration> {
	public RandomSelectorFeature(Codec<RandomFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<RandomFeatureConfiguration> featurePlaceContext) {
		RandomFeatureConfiguration randomFeatureConfiguration = featurePlaceContext.config();
		Random random = featurePlaceContext.random();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		ChunkGenerator chunkGenerator = featurePlaceContext.chunkGenerator();
		BlockPos blockPos = featurePlaceContext.origin();

		for (WeightedConfiguredFeature weightedConfiguredFeature : randomFeatureConfiguration.features) {
			if (random.nextFloat() < weightedConfiguredFeature.chance) {
				return weightedConfiguredFeature.place(worldGenLevel, chunkGenerator, random, blockPos);
			}
		}

		return ((ConfiguredFeature)randomFeatureConfiguration.defaultFeature.get()).place(worldGenLevel, chunkGenerator, random, blockPos);
	}
}
