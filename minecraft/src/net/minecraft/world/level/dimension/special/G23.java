package net.minecraft.world.level.dimension.special;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.phys.Vec3;

public class G23 extends SpecialDimensionBase {
	public G23(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 0.0F);
	}

	@Override
	public boolean isNaturalDimension() {
		return true;
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G23.Generator(this.level, normalBiomes(this.level.getSeed()), NoneGeneratorSettings.INSTANCE);
	}

	@Override
	public float getTimeOfDay(long l, float f) {
		return NormalDimension.getTimeOfDayI(l, 24000.0);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
		return vec3.multiply((double)(f * 0.94F + 0.06F), (double)(f * 0.94F + 0.06F), (double)(f * 0.91F + 0.09F));
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
			WorldgenRandom worldgenRandom = new WorldgenRandom();
			ChunkPos chunkPos = chunkAccess.getPos();
			worldgenRandom.setBaseChunkSeed(chunkPos.x, chunkPos.z);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int i = 0; i < 16; i += 4) {
				for (int j = 0; j < 256; j += 4) {
					for (int k = 0; k < 16; k += 4) {
						Block block = Registry.BLOCK.getRandom(worldgenRandom);
						BlockState blockState = Util.randomObject(worldgenRandom, block.getStateDefinition().getPossibleStates());
						chunkAccess.setBlockState(mutableBlockPos.set(i, j, k), blockState, false);
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
			return ChunkGeneratorType.T18;
		}
	}
}
