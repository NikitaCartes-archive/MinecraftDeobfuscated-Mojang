package net.minecraft.world.level.levelgen.feature;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class FeaturePlaceContext<FC extends FeatureConfiguration> {
	private final WorldGenLevel level;
	private final ChunkGenerator chunkGenerator;
	private final Random random;
	private final BlockPos origin;
	private final FC config;

	public FeaturePlaceContext(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, FC featureConfiguration) {
		this.level = worldGenLevel;
		this.chunkGenerator = chunkGenerator;
		this.random = random;
		this.origin = blockPos;
		this.config = featureConfiguration;
	}

	public WorldGenLevel level() {
		return this.level;
	}

	public ChunkGenerator chunkGenerator() {
		return this.chunkGenerator;
	}

	public Random random() {
		return this.random;
	}

	public BlockPos origin() {
		return this.origin;
	}

	public FC config() {
		return this.config;
	}
}
