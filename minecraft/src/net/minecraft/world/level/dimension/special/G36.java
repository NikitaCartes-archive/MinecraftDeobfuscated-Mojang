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

public class G36 extends SpecialDimensionBase {
	public G36(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 0.0F);
	}

	@Override
	public float getTimeOfDay(long l, float f) {
		return 0.0F;
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G36.Generator(this.level, fixedBiome(Biomes.PLAINS), NoneGeneratorSettings.INSTANCE);
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
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			if (chunkPos.x == 0 && chunkPos.z == 0) {
				BlockState blockState = Blocks.LIME_STAINED_GLASS.defaultBlockState();
				BlockState blockState2 = Blocks.RED_STAINED_GLASS.defaultBlockState();
				BlockState blockState3 = Blocks.BLUE_STAINED_GLASS.defaultBlockState();
				chunkAccess.setBlockState(mutableBlockPos.set(0, 0, 0), Blocks.DIAMOND_BLOCK.defaultBlockState(), false);

				for (int i = 1; i <= 4; i++) {
					chunkAccess.setBlockState(mutableBlockPos.set(0, i, 0), blockState, false);
					chunkAccess.setBlockState(mutableBlockPos.set(i, 0, 0), blockState2, false);
					chunkAccess.setBlockState(mutableBlockPos.set(0, 0, i), blockState3, false);
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
			return ChunkGeneratorType.T31;
		}
	}
}
