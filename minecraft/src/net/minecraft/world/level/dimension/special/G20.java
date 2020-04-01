package net.minecraft.world.level.dimension.special;

import java.util.stream.IntStream;
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
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.phys.Vec3;

public class G20 extends SpecialDimensionBase {
	private static final Vec3 PURPLISH = new Vec3((double)s(207.0F), (double)s(104.0F), (double)s(255.0F));

	private static float s(float f) {
		return f / 255.0F;
	}

	public G20(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 1.0F);
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G20.Generator(this.level, fixedBiome(Biomes.JUNGLE), NoneGeneratorSettings.INSTANCE);
	}

	@Override
	public boolean isNaturalDimension() {
		return true;
	}

	@Override
	public float getTimeOfDay(long l, float f) {
		return 0.75F;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
		return PURPLISH;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean isFoggyAt(int i, int j) {
		return true;
	}

	public static class Generator extends ChunkGenerator<NoneGeneratorSettings> {
		private final PerlinSimplexNoise noise = new PerlinSimplexNoise(new WorldgenRandom(1234L), IntStream.rangeClosed(-5, 0));

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
			BlockState blockState = Blocks.BLACK_CONCRETE.defaultBlockState();
			BlockState blockState2 = Blocks.LIME_CONCRETE.defaultBlockState();
			ChunkPos chunkPos = chunkAccess.getPos();
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int i = 0; i < 16; i++) {
				for (int j = 0; j < 16; j++) {
					double d = 64.0
						+ (this.noise.getValue((double)((float)chunkPos.x + (float)i / 16.0F), (double)((float)chunkPos.z + (float)j / 16.0F), false) + 1.0) * 20.0;

					for (int k = 0; (double)k < d; k++) {
						chunkAccess.setBlockState(mutableBlockPos.set(i, k, j), i != 0 && k % 16 != 0 && j != 0 ? blockState : blockState2, false);
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
			return ChunkGeneratorType.T15;
		}
	}
}
