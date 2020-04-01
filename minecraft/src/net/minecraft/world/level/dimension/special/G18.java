package net.minecraft.world.level.dimension.special;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

public class G18 extends SpecialDimensionBase {
	public G18(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 1.0F);
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G18.Generator(this.level, fixedBiome(Biomes.PLAINS), NoneGeneratorSettings.INSTANCE);
	}

	@Nullable
	@Override
	public BlockPos getValidSpawnPosition(int i, int j, boolean bl) {
		return NormalDimension.getValidSpawnPositionI(this.level, i, j, bl);
	}

	@Override
	public float getTimeOfDay(long l, float f) {
		return NormalDimension.getTimeOfDayI(l, 12000.0);
	}

	@Override
	public boolean isNaturalDimension() {
		return true;
	}

	@Override
	public boolean mayRespawn() {
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
		private StructureTemplate island;

		public Generator(LevelAccessor levelAccessor, BiomeSource biomeSource, NoneGeneratorSettings noneGeneratorSettings) {
			super(levelAccessor, biomeSource, noneGeneratorSettings);
		}

		@Override
		public void createStructures(BiomeManager biomeManager, ChunkAccess chunkAccess, ChunkGenerator<?> chunkGenerator, StructureManager structureManager) {
			if (this.island == null) {
				this.island = structureManager.getOrCreate(new ResourceLocation("content"));
			}

			super.createStructures(biomeManager, chunkAccess, chunkGenerator, structureManager);
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
		}

		@Override
		public void applyCarvers(BiomeManager biomeManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
		}

		@Override
		public void applyBiomeDecoration(WorldGenRegion worldGenRegion) {
			int i = worldGenRegion.getCenterX();
			int j = worldGenRegion.getCenterZ();
			if (i % 32 == 0 && j % 32 == 0) {
				BlockPos blockPos = new BlockPos(16 * i + 8, 64, 16 * j + 8);
				this.island.placeInWorld(worldGenRegion, blockPos, blockPos, new StructurePlaceSettings(), 4);
			}
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
			return ChunkGeneratorType.T13;
		}
	}
}
