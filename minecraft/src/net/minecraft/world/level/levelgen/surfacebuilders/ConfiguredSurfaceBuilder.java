package net.minecraft.world.level.levelgen.surfacebuilders;

import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ConfiguredSurfaceBuilder<SC extends SurfaceBuilderConfiguration> {
	public final SurfaceBuilder<SC> surfaceBuilder;
	public final SC config;

	public ConfiguredSurfaceBuilder(SurfaceBuilder<SC> surfaceBuilder, SC surfaceBuilderConfiguration) {
		this.surfaceBuilder = surfaceBuilder;
		this.config = surfaceBuilderConfiguration;
	}

	public void apply(
		Random random, ChunkAccess chunkAccess, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, long m
	) {
		this.surfaceBuilder.apply(random, chunkAccess, biome, i, j, k, d, blockState, blockState2, l, m, this.config);
	}

	public void initNoise(long l) {
		this.surfaceBuilder.initNoise(l);
	}

	public SC getSurfaceBuilderConfiguration() {
		return this.config;
	}
}
