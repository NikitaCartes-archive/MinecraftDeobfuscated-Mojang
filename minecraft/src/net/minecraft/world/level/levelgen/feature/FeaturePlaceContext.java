package net.minecraft.world.level.levelgen.feature;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class FeaturePlaceContext<FC extends FeatureConfiguration> {
	private final Optional<ConfiguredFeature<?, ?>> topFeature;
	private final WorldGenLevel level;
	private final ChunkGenerator chunkGenerator;
	private final RandomSource random;
	private final BlockPos origin;
	private final FC config;

	public FeaturePlaceContext(
		Optional<ConfiguredFeature<?, ?>> optional,
		WorldGenLevel worldGenLevel,
		ChunkGenerator chunkGenerator,
		RandomSource randomSource,
		BlockPos blockPos,
		FC featureConfiguration
	) {
		this.topFeature = optional;
		this.level = worldGenLevel;
		this.chunkGenerator = chunkGenerator;
		this.random = randomSource;
		this.origin = blockPos;
		this.config = featureConfiguration;
	}

	public Optional<ConfiguredFeature<?, ?>> topFeature() {
		return this.topFeature;
	}

	public WorldGenLevel level() {
		return this.level;
	}

	public ChunkGenerator chunkGenerator() {
		return this.chunkGenerator;
	}

	public RandomSource random() {
		return this.random;
	}

	public BlockPos origin() {
		return this.origin;
	}

	public FC config() {
		return this.config;
	}
}
