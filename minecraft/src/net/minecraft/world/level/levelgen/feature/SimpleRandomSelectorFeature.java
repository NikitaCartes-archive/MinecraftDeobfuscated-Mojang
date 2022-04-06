package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class SimpleRandomSelectorFeature extends Feature<SimpleRandomFeatureConfiguration> {
	public SimpleRandomSelectorFeature(Codec<SimpleRandomFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<SimpleRandomFeatureConfiguration> featurePlaceContext) {
		RandomSource randomSource = featurePlaceContext.random();
		SimpleRandomFeatureConfiguration simpleRandomFeatureConfiguration = featurePlaceContext.config();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		ChunkGenerator chunkGenerator = featurePlaceContext.chunkGenerator();
		int i = randomSource.nextInt(simpleRandomFeatureConfiguration.features.size());
		PlacedFeature placedFeature = simpleRandomFeatureConfiguration.features.get(i).value();
		return placedFeature.place(worldGenLevel, chunkGenerator, randomSource, blockPos);
	}
}
