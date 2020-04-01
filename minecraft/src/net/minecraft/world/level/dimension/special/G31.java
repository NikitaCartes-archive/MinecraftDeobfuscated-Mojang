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
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class G31 extends SpecialDimensionBase {
	public G31(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 0.0F);
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G31.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
	}

	@Override
	public float getTimeOfDay(long l, float f) {
		return NormalDimension.getTimeOfDayI(l, 24000.0);
	}

	@Override
	public boolean isNaturalDimension() {
		return true;
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
			BlockState blockState = Blocks.GRASS_BLOCK.defaultBlockState();
			BlockState blockState2 = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
			ChunkPos chunkPos = chunkAccess.getPos();
			int i = chunkPos.getMinBlockX();
			int j = chunkPos.getMinBlockZ();
			int k = 0;
			int l;
			if (chunkPos.x >= 0) {
				l = 0;
			} else if (chunkPos.z >= 0) {
				l = -1;
			} else {
				l = 1;
			}

			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int m = 0; m < 16; m++) {
				for (int n = 0; n < 16; n++) {
					int o = Math.max(Math.abs(i + m - l), Math.abs(j + n - 0));
					if (o % 2 == 0) {
						chunkAccess.setBlockState(mutableBlockPos.set(m, 50, n), blockState2, false);
						chunkAccess.setBlockState(mutableBlockPos.move(0, 1, 0), blockState2, false);
						chunkAccess.setBlockState(mutableBlockPos.move(0, 1, 0), blockState2, false);
					} else {
						chunkAccess.setBlockState(mutableBlockPos.set(m, 50, n), blockState, false);
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
			return ChunkGeneratorType.T26;
		}
	}
}
