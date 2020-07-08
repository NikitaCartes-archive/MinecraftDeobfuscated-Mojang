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

	public boolean place(
		WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, RandomFeatureConfiguration randomFeatureConfiguration
	) {
		for (WeightedConfiguredFeature weightedConfiguredFeature : randomFeatureConfiguration.features) {
			if (random.nextFloat() < weightedConfiguredFeature.chance) {
				return weightedConfiguredFeature.place(worldGenLevel, chunkGenerator, random, blockPos);
			}
		}

		return ((ConfiguredFeature)randomFeatureConfiguration.defaultFeature.get()).place(worldGenLevel, chunkGenerator, random, blockPos);
	}
}
