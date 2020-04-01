package net.minecraft.world.level.dimension.special;

import java.util.Random;
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

public class G02 extends SpecialDimensionBase {
	private static final BlockState X = Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState();
	private static final BlockState O = Blocks.AIR.defaultBlockState();
	private static final BlockState[] LEFT = new BlockState[]{
		X,
		X,
		O,
		O,
		O,
		O,
		O,
		O,
		X,
		X,
		X,
		O,
		O,
		O,
		O,
		O,
		O,
		X,
		X,
		X,
		O,
		O,
		O,
		O,
		O,
		O,
		X,
		X,
		X,
		O,
		O,
		O,
		O,
		O,
		O,
		X,
		X,
		X,
		O,
		O,
		O,
		O,
		O,
		O,
		X,
		X,
		X,
		O,
		O,
		O,
		O,
		O,
		O,
		X,
		X,
		X,
		O,
		O,
		O,
		O,
		O,
		O,
		X,
		X
	};
	private static final BlockState[] RIGHT = new BlockState[]{
		O,
		O,
		O,
		O,
		O,
		O,
		X,
		X,
		O,
		O,
		O,
		O,
		O,
		X,
		X,
		X,
		O,
		O,
		O,
		O,
		X,
		X,
		X,
		O,
		O,
		O,
		O,
		X,
		X,
		X,
		O,
		O,
		O,
		O,
		X,
		X,
		X,
		O,
		O,
		O,
		O,
		X,
		X,
		X,
		O,
		O,
		O,
		O,
		X,
		X,
		X,
		O,
		O,
		O,
		O,
		O,
		X,
		X,
		O,
		O,
		O,
		O,
		O,
		O
	};

	public G02(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 1.0F);
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G02.Generator(this.level, fixedBiome(Biomes.JUNGLE), NoneGeneratorSettings.INSTANCE);
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
			BlockState blockState = Blocks.BLUE_CONCRETE.defaultBlockState();
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int i = 0; i < 16; i++) {
				for (int j = 0; j < 16; j++) {
					for (int k = 0; k < 16; k++) {
						chunkAccess.setBlockState(mutableBlockPos.set(i, j, k), blockState, false);
					}
				}
			}

			Random random = new Random((long)(chunkPos.x << 16 + chunkPos.z));
			this.placeSlash(chunkAccess, 0, 0, random.nextBoolean() ? G02.LEFT : G02.RIGHT);
			this.placeSlash(chunkAccess, 0, 8, random.nextBoolean() ? G02.LEFT : G02.RIGHT);
			this.placeSlash(chunkAccess, 8, 0, random.nextBoolean() ? G02.LEFT : G02.RIGHT);
			this.placeSlash(chunkAccess, 8, 8, random.nextBoolean() ? G02.LEFT : G02.RIGHT);
		}

		private void placeSlash(ChunkAccess chunkAccess, int i, int j, BlockState[] blockStates) {
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int k = 0; k < 8; k++) {
				for (int l = 0; l < 8; l++) {
					BlockState blockState = blockStates[k * 8 + l];

					for (int m = 16; m < 32; m++) {
						chunkAccess.setBlockState(mutableBlockPos.set(k + i, m, l + j), blockState, false);
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
			return ChunkGeneratorType.T02;
		}
	}
}
