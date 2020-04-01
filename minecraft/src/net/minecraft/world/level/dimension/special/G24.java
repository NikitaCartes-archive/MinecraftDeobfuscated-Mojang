package net.minecraft.world.level.dimension.special;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.phys.Vec3;

public class G24 extends SpecialDimensionBase {
	public G24(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 0.0F);
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G24.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
	}

	@Override
	public float getTimeOfDay(long l, float f) {
		return 0.5F;
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
		private final List<Block> blocks = (List<Block>)Registry.BLOCK
			.stream()
			.filter(block -> !block.isUnstable() && !block.isEntityBlock())
			.collect(ImmutableList.toImmutableList());

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
			float f = worldgenRandom.nextFloat() / 10.0F;
			float g = worldgenRandom.nextFloat() / 10.0F;
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int i = 0; i < 16; i++) {
				for (int j = 0; j < 16; j++) {
					int k = i - 8;
					int l = j - 8;
					float h = Mth.sqrt((float)(k * k + l * l));
					if (h <= 9.0F && h >= 6.0F) {
						if ((int)h == 7) {
							chunkAccess.setBlockState(mutableBlockPos.set(i, 128, j), Blocks.SMOOTH_QUARTZ.defaultBlockState(), false);
						} else {
							chunkAccess.setBlockState(mutableBlockPos.set(i, 128, j), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), false);
						}
					}
				}
			}

			int i = 10 + worldgenRandom.nextInt(75);

			for (int jx = -i; jx < i; jx++) {
				Block block = Util.randomObject(worldgenRandom, this.blocks);
				BlockState blockState = Util.randomObject(worldgenRandom, block.getStateDefinition().getPossibleStates());
				int m = 8 + (int)(5.0F * Mth.sin(f * (float)jx));
				int n = 8 + (int)(5.0F * Mth.sin(g * (float)jx));
				chunkAccess.setBlockState(mutableBlockPos.set(m, 128 + jx, n), blockState, false);
				chunkAccess.setBlockState(mutableBlockPos.set(m, 128 - jx, n), blockState, false);
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
			return ChunkGeneratorType.T19;
		}
	}
}
