package net.minecraft.world.level.dimension.special;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class G05 extends SpecialDimensionBase {
	public G05(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 1.0F);
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G05.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
	}

	@Override
	public float getTimeOfDay(long l, float f) {
		return 12000.0F;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
		return vec3;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean isFoggyAt(int i, int j) {
		return false;
	}

	public static class Generator extends ChunkGenerator<NoneGeneratorSettings> {
		public Generator(LevelAccessor levelAccessor, BiomeSource biomeSource, NoneGeneratorSettings noneGeneratorSettings) {
			super(levelAccessor, biomeSource, noneGeneratorSettings);
		}

		@Override
		public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
		}

		@Override
		public int getSpawnHeight() {
			return 30;
		}

		@Override
		public void fillFromNoise(LevelAccessor levelAccessor, ChunkAccess chunkAccess) {
			ChunkPos chunkPos = chunkAccess.getPos();
			if (chunkPos.x >= 0 && chunkPos.z >= 0) {
				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

				for (int i = 0; i < 16; i++) {
					for (int j = 0; j < 256; j++) {
						for (int k = 0; k < 16; k++) {
							int l = chunkPos.getMinBlockX() + i;
							int m = chunkPos.getMinBlockZ() + k;
							if (isInSponge(l, j, m)) {
								chunkAccess.setBlockState(mutableBlockPos.set(l, j, m), Blocks.SPONGE.defaultBlockState(), false);
							}
						}
					}
				}
			}
		}

		private static boolean isInSponge(int i, int j, int k) {
			while (i != 0 || j != 0 || k != 0) {
				int l = i % 3 == 1 ? 1 : 0;
				int m = j % 3 == 1 ? 1 : 0;
				int n = k % 3 == 1 ? 1 : 0;
				if (l + m + n >= 2) {
					return false;
				}

				i /= 3;
				j /= 3;
				k /= 3;
			}

			return true;
		}

		@Override
		public void applyCarvers(BiomeManager biomeManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
		}

		@Override
		public void applyBiomeDecoration(WorldGenRegion worldGenRegion) {
		}

		@Override
		public int getBaseHeight(int i, int j, Heightmap.Types types) {
			return 0;
		}

		@Override
		public BlockGetter getBaseColumn(int i, int j) {
			return EmptyBlockGetter.INSTANCE;
		}

		@Override
		public ChunkGeneratorType<?, ?> getType() {
			return ChunkGeneratorType.T05;
		}
	}
}
