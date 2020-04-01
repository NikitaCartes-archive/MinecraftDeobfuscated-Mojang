package net.minecraft.world.level.dimension.special;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;
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

public class G38 extends NormalDimension {
	public G38(Level level, DimensionType dimensionType) {
		super(level, dimensionType);
	}

	@Override
	public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
		return new G38.Generator(this.level, SpecialDimensionBase.normalBiomes(this.level.getSeed()), ChunkGeneratorType.SURFACE.createSettings());
	}

	public static class Generator extends OverworldLevelSource {
		public Generator(LevelAccessor levelAccessor, BiomeSource biomeSource, OverworldGeneratorSettings overworldGeneratorSettings) {
			super(levelAccessor, biomeSource, overworldGeneratorSettings);
		}

		@Override
		public void applyBiomeDecoration(WorldGenRegion worldGenRegion) {
			super.applyBiomeDecoration(worldGenRegion);
			int i = worldGenRegion.getCenterX();
			int j = worldGenRegion.getCenterZ();
			WorldgenRandom worldgenRandom = new WorldgenRandom();
			worldgenRandom.setBaseChunkSeed(i, j);
			int k = i * i + j * j;
			int l = Math.min(Mth.floor(Math.sqrt((double)k) / 3.0 + 1.0), 16);
			int m = worldgenRandom.nextInt(Math.min(k / 2 + 1, 32768));
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();

			for (int n = 0; n < m; n++) {
				int o = 16 * i + worldgenRandom.nextInt(16);
				int p = 16 * j + worldgenRandom.nextInt(16);
				int q = worldGenRegion.getHeight(Heightmap.Types.MOTION_BLOCKING, o, p);
				int r = worldgenRandom.nextInt(q + 5);
				mutableBlockPos.set(o, r, p);
				mutableBlockPos2.setWithOffset(mutableBlockPos, worldgenRandom.nextInt(l), worldgenRandom.nextInt(l), worldgenRandom.nextInt(l));
				BlockState blockState = worldGenRegion.getBlockState(mutableBlockPos);
				worldGenRegion.setBlock(mutableBlockPos, worldGenRegion.getBlockState(mutableBlockPos2), 4);
				worldGenRegion.setBlock(mutableBlockPos2, blockState, 4);
			}
		}

		@Override
		public ChunkGeneratorType<?, ?> getType() {
			return ChunkGeneratorType.T33;
		}
	}
}
