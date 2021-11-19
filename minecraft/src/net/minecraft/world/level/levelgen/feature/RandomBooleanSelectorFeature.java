package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomBooleanSelectorFeature extends Feature<RandomBooleanFeatureConfiguration> {
	public RandomBooleanSelectorFeature(Codec<RandomBooleanFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<RandomBooleanFeatureConfiguration> featurePlaceContext) {
		Random random = featurePlaceContext.random();
		RandomBooleanFeatureConfiguration randomBooleanFeatureConfiguration = featurePlaceContext.config();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		ChunkGenerator chunkGenerator = featurePlaceContext.chunkGenerator();
		BlockPos blockPos = featurePlaceContext.origin();
		boolean bl = random.nextBoolean();
		return bl
			? ((PlacedFeature)randomBooleanFeatureConfiguration.featureTrue.get()).place(worldGenLevel, chunkGenerator, random, blockPos)
			: ((PlacedFeature)randomBooleanFeatureConfiguration.featureFalse.get()).place(worldGenLevel, chunkGenerator, random, blockPos);
	}
}
