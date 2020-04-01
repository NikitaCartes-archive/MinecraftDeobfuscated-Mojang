package net.minecraft.world.level.dimension.special;

import com.mojang.math.OctahedralGroup;
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
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.CharFeature;
import net.minecraft.world.level.levelgen.feature.configurations.CharConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.phys.Vec3;

public class G34 extends SpecialDimensionBase {
	public G34(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 1.0F);
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G34.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
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
		private static final BlockState STATE = Blocks.FIRE
			.defaultBlockState()
			.setValue(FireBlock.NORTH, Boolean.valueOf(true))
			.setValue(FireBlock.SOUTH, Boolean.valueOf(true))
			.setValue(FireBlock.EAST, Boolean.valueOf(true))
			.setValue(FireBlock.WEST, Boolean.valueOf(true))
			.setValue(FireBlock.UP, Boolean.valueOf(true));
		final SimpleStateProvider provider = new SimpleStateProvider(STATE);

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
			if (chunkPos.z == 0) {
				this.printChar(chunkAccess, chunkPos.x);
			}
		}

		private void printChar(ChunkAccess chunkAccess, int i) {
			if (i >= 0 && i < " We apologise for the inconvenience.".length()) {
				char c = " We apologise for the inconvenience.".charAt(i);
				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
				CharFeature.place(BlockPos.ZERO, new CharConfiguration(this.provider, c, OctahedralGroup.IDENTITY), blockPos -> {
					chunkAccess.setBlockState(mutableBlockPos.set(2 * blockPos.getX() + 0, 2 * blockPos.getY() + 100, 2 * blockPos.getZ() + 0), STATE, false);
					chunkAccess.setBlockState(mutableBlockPos.set(2 * blockPos.getX() + 1, 2 * blockPos.getY() + 100, 2 * blockPos.getZ() + 1), STATE, false);
					chunkAccess.setBlockState(mutableBlockPos.set(2 * blockPos.getX() + 1, 2 * blockPos.getY() + 100, 2 * blockPos.getZ() + 0), STATE, false);
					chunkAccess.setBlockState(mutableBlockPos.set(2 * blockPos.getX() + 0, 2 * blockPos.getY() + 100, 2 * blockPos.getZ() + 1), STATE, false);
					chunkAccess.setBlockState(mutableBlockPos.set(2 * blockPos.getX() + 0, 2 * blockPos.getY() + 101, 2 * blockPos.getZ() + 0), STATE, false);
					chunkAccess.setBlockState(mutableBlockPos.set(2 * blockPos.getX() + 1, 2 * blockPos.getY() + 101, 2 * blockPos.getZ() + 1), STATE, false);
					chunkAccess.setBlockState(mutableBlockPos.set(2 * blockPos.getX() + 1, 2 * blockPos.getY() + 101, 2 * blockPos.getZ() + 0), STATE, false);
					chunkAccess.setBlockState(mutableBlockPos.set(2 * blockPos.getX() + 0, 2 * blockPos.getY() + 101, 2 * blockPos.getZ() + 1), STATE, false);
				});
			}
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
			return ChunkGeneratorType.T29;
		}
	}
}
