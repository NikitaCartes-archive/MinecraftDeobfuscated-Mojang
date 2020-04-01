package net.minecraft.world.level.dimension.special;

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
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldLevelSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class G37 extends NormalDimension {
	public G37(Level level, DimensionType dimensionType) {
		super(level, dimensionType);
	}

	@Override
	public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
		return new G37.Generator(this.level, SpecialDimensionBase.normalBiomes(this.level.getSeed()), ChunkGeneratorType.SURFACE.createSettings());
	}

	public static class Generator extends OverworldLevelSource {
		public Generator(LevelAccessor levelAccessor, BiomeSource biomeSource, OverworldGeneratorSettings overworldGeneratorSettings) {
			super(levelAccessor, biomeSource, overworldGeneratorSettings);
		}

		@Override
		public void applyBiomeDecoration(WorldGenRegion worldGenRegion) {
			super.applyBiomeDecoration(worldGenRegion);
			WorldgenRandom worldgenRandom = new WorldgenRandom();
			int i = worldGenRegion.getCenterX();
			int j = worldGenRegion.getCenterZ();
			worldgenRandom.setBaseChunkSeed(i, j);
			if (worldgenRandom.nextInt(10) == 0) {
				BlockState blockState = Blocks.AIR.defaultBlockState();
				BlockState blockState2 = Blocks.OBSIDIAN.defaultBlockState();
				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
				int k = 1 + worldgenRandom.nextInt(10);
				int l = k * k + 1;
				int m = k + 3;
				int n = m * m + 1;

				for (int o = -m; o <= m; o++) {
					for (int p = -m; p <= m; p++) {
						int q = o * o + p * p;
						if (q <= l) {
							for (int r = 0; r < 256; r++) {
								worldGenRegion.setBlock(mutableBlockPos.set(16 * i + o, r, 16 * j + p), blockState2, 4);
							}
						} else if (q <= n) {
							for (int r = 0; r < 256; r++) {
								worldGenRegion.setBlock(mutableBlockPos.set(16 * i + o, r, 16 * j + p), blockState, 4);
							}
						}
					}
				}
			}
		}

		@Override
		public ChunkGeneratorType<?, ?> getType() {
			return ChunkGeneratorType.T32;
		}
	}
}
