package net.minecraft.world.level.dimension.special;

import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldLevelSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.ShapeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

public class G40 extends NormalDimension {
	public G40(Level level, DimensionType dimensionType) {
		super(level, dimensionType);
	}

	@Override
	public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
		return new G40.Generator(this.level, SpecialDimensionBase.normalBiomes(this.level.getSeed()), ChunkGeneratorType.SURFACE.createSettings());
	}

	public static class Generator extends OverworldLevelSource {
		public static final SimpleStateProvider STATE_PROVIDER = new SimpleStateProvider(Blocks.AIR.defaultBlockState());
		private final PerlinSimplexNoise noise;

		public Generator(LevelAccessor levelAccessor, BiomeSource biomeSource, OverworldGeneratorSettings overworldGeneratorSettings) {
			super(levelAccessor, biomeSource, overworldGeneratorSettings);
			WorldgenRandom worldgenRandom = new WorldgenRandom(levelAccessor.getSeed());
			this.noise = new PerlinSimplexNoise(worldgenRandom, IntStream.rangeClosed(-4, 1));
		}

		@Override
		public void applyBiomeDecoration(WorldGenRegion worldGenRegion) {
			super.applyBiomeDecoration(worldGenRegion);
			WorldgenRandom worldgenRandom = new WorldgenRandom();
			int i = worldGenRegion.getCenterX();
			int j = worldGenRegion.getCenterZ();
			worldgenRandom.setBaseChunkSeed(i, j);
			int k = worldgenRandom.nextInt(4);

			for (int l = 0; l < k; l++) {
				int m = 16 * i + worldgenRandom.nextInt(16);
				int n = 16 * j + worldgenRandom.nextInt(16);
				int o = worldGenRegion.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, m, n);
				int p = Mth.ceil((double)o * (1.0 + worldgenRandom.nextGaussian() / 2.0));
				ShapeConfiguration.Metric metric = Util.randomObject(worldgenRandom, ShapeConfiguration.Metric.values());
				float f = 2.0F + worldgenRandom.nextFloat() * 5.0F;
				float g = Math.min(f + worldgenRandom.nextFloat() * 10.0F, 15.0F);
				Feature.SHAPE.place(worldGenRegion, this, worldgenRandom, new BlockPos(m, p, n), new ShapeConfiguration(STATE_PROVIDER, metric, f, g));
			}
		}

		@Override
		public ChunkGeneratorType<?, ?> getType() {
			return ChunkGeneratorType.T35;
		}
	}
}
