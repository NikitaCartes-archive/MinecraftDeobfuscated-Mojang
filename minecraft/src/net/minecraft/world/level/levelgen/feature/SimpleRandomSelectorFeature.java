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

	public boolean place(
		WorldGenLevel worldGenLevel,
		ChunkGenerator chunkGenerator,
		Random random,
		BlockPos blockPos,
		SimpleRandomFeatureConfiguration simpleRandomFeatureConfiguration
	) {
		int i = random.nextInt(simpleRandomFeatureConfiguration.features.size());
		ConfiguredFeature<?, ?> configuredFeature = (ConfiguredFeature<?, ?>)((Supplier)simpleRandomFeatureConfiguration.features.get(i)).get();
		return configuredFeature.place(worldGenLevel, chunkGenerator, random, blockPos);
	}
}
