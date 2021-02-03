package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;

public class SimpleRandomSelectorFeature extends Feature<SimpleRandomFeatureConfiguration> {
	public SimpleRandomSelectorFeature(Codec<SimpleRandomFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<SimpleRandomFeatureConfiguration> featurePlaceContext) {
		Random random = featurePlaceContext.random();
		SimpleRandomFeatureConfiguration simpleRandomFeatureConfiguration = featurePlaceContext.config();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		ChunkGenerator chunkGenerator = featurePlaceContext.chunkGenerator();
		int i = random.nextInt(simpleRandomFeatureConfiguration.features.size());
		ConfiguredFeature<?, ?> configuredFeature = (ConfiguredFeature<?, ?>)((Supplier)simpleRandomFeatureConfiguration.features.get(i)).get();
		return configuredFeature.place(worldGenLevel, chunkGenerator, random, blockPos);
	}
}
