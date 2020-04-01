package net.minecraft.world.level.dimension.special;

import java.util.BitSet;
import java.util.function.IntPredicate;
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
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.phys.Vec3;

public class G32 extends SpecialDimensionBase {
	private static final BitSet TUNNNEL_N = new BitSet();
	private static final BitSet TUNNNEL_S = new BitSet();
	private static final BitSet TUNNNEL_W = new BitSet();
	private static final BitSet TUNNNEL_E = new BitSet();
	private static final BitSet TUNNNEL_U = new BitSet();
	private static final BitSet TUNNNEL_D = new BitSet();
	private static final BitSet CHAMBER = new BitSet();

	private static int sectionPosToInt(int i, int j, int k) {
		return i << 8 | j << 4 | k;
	}

	public G32(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 0.0F);
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G32.Generator(this.level, fixedBiome(Biomes.THE_END), NoneGeneratorSettings.INSTANCE);
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

	static {
		for (int i = 6; i < 10; i++) {
			for (int j = 6; j < 10; j++) {
				for (int k = 0; k <= 8; k++) {
					TUNNNEL_N.set(sectionPosToInt(i, j, k));
					TUNNNEL_S.set(sectionPosToInt(i, j, 15 - k));
					TUNNNEL_W.set(sectionPosToInt(k, j, i));
					TUNNNEL_E.set(sectionPosToInt(15 - k, j, i));
					TUNNNEL_U.set(sectionPosToInt(i, 15 - k, j));
					TUNNNEL_D.set(sectionPosToInt(i, k, j));
				}
			}
		}

		for (int i = 5; i < 11; i++) {
			for (int j = 5; j < 11; j++) {
				for (int k = 5; k < 11; k++) {
					CHAMBER.set(sectionPosToInt(i, j, k));
				}
			}
		}
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

		private static IntPredicate appendSide(WorldgenRandom worldgenRandom, int i, int j, int k, IntPredicate intPredicate, BitSet bitSet) {
			if (j < 0) {
				return intPredicate;
			} else {
				worldgenRandom.setBaseChunkSeed(i, k);
				worldgenRandom.setBaseChunkSeed(worldgenRandom.nextInt(), j);
				return worldgenRandom.nextBoolean() ? intPredicate.or(bitSet::get) : intPredicate;
			}
		}

		@Override
		public void fillFromNoise(LevelAccessor levelAccessor, ChunkAccess chunkAccess) {
			ChunkPos chunkPos = chunkAccess.getPos();

			for (int i = 0; i < 16; i++) {
				WorldgenRandom worldgenRandom = new WorldgenRandom();
				int j = 2 * chunkPos.x;
				int k = 2 * i;
				int l = 2 * chunkPos.z;
				IntPredicate intPredicate = ix -> false;
				intPredicate = appendSide(worldgenRandom, j + 1, k, l, intPredicate, G32.TUNNNEL_E);
				intPredicate = appendSide(worldgenRandom, j - 1, k, l, intPredicate, G32.TUNNNEL_W);
				intPredicate = appendSide(worldgenRandom, j, k, l + 1, intPredicate, G32.TUNNNEL_S);
				intPredicate = appendSide(worldgenRandom, j, k, l - 1, intPredicate, G32.TUNNNEL_N);
				intPredicate = appendSide(worldgenRandom, j, k + 1, l, intPredicate, G32.TUNNNEL_U);
				intPredicate = appendSide(worldgenRandom, j, k - 1, l, intPredicate, G32.TUNNNEL_D);
				if (intPredicate.test(G32.sectionPosToInt(8, 8, 8))) {
					intPredicate = intPredicate.or(G32.CHAMBER::get);
				}

				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

				for (int m = 0; m < 16; m++) {
					for (int n = 0; n < 16; n++) {
						for (int o = 0; o < 16; o++) {
							if (!intPredicate.test(G32.sectionPosToInt(m, n, o))) {
								int p = 16 * i + n;
								chunkAccess.setBlockState(mutableBlockPos.set(m, p, o), Blocks.SEA_LANTERN.defaultBlockState(), false);
							}
						}
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
			return ChunkGeneratorType.T28;
		}
	}
}
