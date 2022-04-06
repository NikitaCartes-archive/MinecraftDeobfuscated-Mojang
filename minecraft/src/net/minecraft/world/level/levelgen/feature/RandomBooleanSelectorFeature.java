package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;

public class RandomBooleanSelectorFeature extends Feature<RandomBooleanFeatureConfiguration> {
	public RandomBooleanSelectorFeature(Codec<RandomBooleanFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<RandomBooleanFeatureConfiguration> featurePlaceContext) {
		RandomSource randomSource = featurePlaceContext.random();
		RandomBooleanFeatureConfiguration randomBooleanFeatureConfiguration = featurePlaceContext.config();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		ChunkGenerator chunkGenerator = featurePlaceContext.chunkGenerator();
		BlockPos blockPos = featurePlaceContext.origin();
		boolean bl = randomSource.nextBoolean();
		return (bl ? randomBooleanFeatureConfiguration.featureTrue : randomBooleanFeatureConfiguration.featureFalse)
			.value()
			.place(worldGenLevel, chunkGenerator, randomSource, blockPos);
	}
}
