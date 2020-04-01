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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class G27 extends SpecialDimensionBase {
	public G27(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 0.0F);
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G27.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
	}

	@Override
	public float getTimeOfDay(long l, float f) {
		return 0.0F;
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
		public void applyBiomeDecoration(WorldGenRegion worldGenRegion) {
		}

		@Override
		public void applyCarvers(BiomeManager biomeManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
		}

		@Override
		public int getSpawnHeight() {
			return 100;
		}

		@Override
		public void fillFromNoise(LevelAccessor levelAccessor, ChunkAccess chunkAccess) {
			ChunkPos chunkPos = chunkAccess.getPos();
			this.fillPattern(chunkAccess, chunkPos, 0, 0);
			this.fillPattern(chunkAccess, chunkPos, 1, 0);
			this.fillPattern(chunkAccess, chunkPos, 0, 1);
			this.fillPattern(chunkAccess, chunkPos, 1, 1);
		}

		private void fillPattern(ChunkAccess chunkAccess, ChunkPos chunkPos, int i, int j) {
			int k = 8388607;
			long l = ((long)chunkPos.x * 2L + (long)i & 8388607L) << 31 | ((long)chunkPos.z * 2L + (long)j & 8388607L) << 8;
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			BlockState blockState = Blocks.WHITE_CONCRETE.defaultBlockState();
			BlockState blockState2 = Blocks.BLACK_CONCRETE.defaultBlockState();

			for (int m = 0; m < 256; m++) {
				long n = l | (long)m;

				for (int o = 0; o < 7; o++) {
					for (int p = 0; p < 7; p++) {
						boolean bl = (n & 1L) != 0L;
						n >>= 1;
						chunkAccess.setBlockState(mutableBlockPos.set(o + i * 8, m, p + j * 8), bl ? blockState : blockState2, false);
					}
				}
			}
		}

		@Override
		public int getBaseHeight(int i, int j, Heightmap.Types types) {
			return 100;
		}

		@Override
		public BlockGetter getBaseColumn(int i, int j) {
			return EmptyBlockGetter.INSTANCE;
		}

		@Override
		public ChunkGeneratorType<?, ?> getType() {
			return ChunkGeneratorType.T22;
		}
	}
}
