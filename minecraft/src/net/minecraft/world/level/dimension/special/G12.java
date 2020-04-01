package net.minecraft.world.level.dimension.special;

import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
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
import net.minecraft.world.level.block.Block;
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

public class G12 extends SpecialDimensionBase {
	private static final boolean[] FACE = new boolean[]{
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		true,
		true,
		false,
		false,
		true,
		true,
		false,
		false,
		true,
		true,
		false,
		false,
		true,
		true,
		false,
		false,
		false,
		false,
		true,
		true,
		false,
		false,
		false,
		false,
		false,
		false,
		true,
		true,
		false,
		false,
		false,
		false,
		false,
		true,
		true,
		true,
		true,
		false,
		false,
		false,
		false,
		true,
		false,
		false,
		true,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false
	};

	public G12(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 1.0F);
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G12.Generator(this.level, fixedBiome(Biomes.JUNGLE), NoneGeneratorSettings.INSTANCE);
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
		private final PerlinSimplexNoise face;
		private final PerlinSimplexNoise background;
		private final WorldgenRandom random = new WorldgenRandom();

		public Generator(LevelAccessor levelAccessor, BiomeSource biomeSource, NoneGeneratorSettings noneGeneratorSettings) {
			super(levelAccessor, biomeSource, noneGeneratorSettings);
			this.face = new PerlinSimplexNoise(this.random, IntStream.rangeClosed(-5, 0));
			this.background = new PerlinSimplexNoise(this.random, IntStream.rangeClosed(-5, 0));
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
			this.placeFace(chunkAccess, 0, 0);
			this.placeFace(chunkAccess, 0, 8);
			this.placeFace(chunkAccess, 8, 0);
			this.placeFace(chunkAccess, 8, 8);
		}

		private void placeFace(ChunkAccess chunkAccess, int i, int j) {
			ChunkPos chunkPos = chunkAccess.getPos();
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			double d = (1.0 + this.face.getValue((double)chunkPos.x, (double)chunkPos.z, false)) * 16.0;
			double e = (1.0 + this.background.getValue((double)chunkPos.x, (double)chunkPos.z, false)) * 16.0;
			this.random.setBaseChunkSeed(2 * chunkPos.x + i, 2 * chunkPos.z + j);
			Block[] blocks = Util.randomObject(this.random, ColoredBlocks.COLORED_BLOCKS);
			BlockState blockState = Util.randomObject(this.random, blocks).defaultBlockState();
			BlockState blockState2 = Util.randomObject(this.random, blocks).defaultBlockState();

			for (int k = 0; k < 8; k++) {
				for (int l = 0; l < 8; l++) {
					boolean bl = G12.FACE[k * 8 + l];
					BlockState blockState3 = bl ? blockState : blockState2;
					double f = bl ? d : e;

					for (int m = 1; (double)m < f; m++) {
						chunkAccess.setBlockState(mutableBlockPos.set(k + i, m, l + j), blockState3, false);
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
			return ChunkGeneratorType.T08;
		}
	}
}
