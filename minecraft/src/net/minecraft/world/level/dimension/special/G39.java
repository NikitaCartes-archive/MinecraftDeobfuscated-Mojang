package net.minecraft.world.level.dimension.special;

import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldLevelSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

public class G39 extends NormalDimension {
	public G39(Level level, DimensionType dimensionType) {
		super(level, dimensionType);
	}

	@Override
	public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
		return new G39.Generator(this.level, SpecialDimensionBase.normalBiomes(this.level.getSeed()), ChunkGeneratorType.SURFACE.createSettings());
	}

	public static class Generator extends OverworldLevelSource {
		private final PerlinSimplexNoise noise;

		public Generator(LevelAccessor levelAccessor, BiomeSource biomeSource, OverworldGeneratorSettings overworldGeneratorSettings) {
			super(levelAccessor, biomeSource, overworldGeneratorSettings);
			WorldgenRandom worldgenRandom = new WorldgenRandom(levelAccessor.getSeed());
			this.noise = new PerlinSimplexNoise(worldgenRandom, IntStream.rangeClosed(-4, 1));
		}

		@Override
		public void applyBiomeDecoration(WorldGenRegion worldGenRegion) {
			super.applyBiomeDecoration(worldGenRegion);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			int i = worldGenRegion.getCenterX();
			int j = worldGenRegion.getCenterZ();
			BlockState blockState = Blocks.ZONE.defaultBlockState();

			for (int k = 0; k < 16; k++) {
				for (int l = 0; l < 16; l++) {
					int m = 16 * i + k;
					int n = 16 * j + l;
					int o = worldGenRegion.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, m, n);
					int p = (int)(this.noise.getValue((double)((float)m / 16.0F), (double)((float)n / 16.0F), false) * (double)o / 3.0) - 1;
					if (p > 0) {
						for (int q = -p; q < p; q++) {
							mutableBlockPos.set(m, o + q, n);
							if (worldGenRegion.isEmptyBlock(mutableBlockPos)) {
								worldGenRegion.setBlock(mutableBlockPos, blockState, 4);
							}
						}
					}
				}
			}
		}

		@Override
		public ChunkGeneratorType<?, ?> getType() {
			return ChunkGeneratorType.T34;
		}
	}
}
