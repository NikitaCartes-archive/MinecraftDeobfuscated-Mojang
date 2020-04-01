package net.minecraft.world.level.dimension.special;

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
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

public class G19 extends SpecialDimensionBase {
	public G19(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 1.0F);
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G19.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
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
		private StructureTemplate center;
		private StructureTemplate side;
		private StructureTemplate legs;

		public Generator(LevelAccessor levelAccessor, BiomeSource biomeSource, NoneGeneratorSettings noneGeneratorSettings) {
			super(levelAccessor, biomeSource, noneGeneratorSettings);
		}

		@Override
		public void createStructures(BiomeManager biomeManager, ChunkAccess chunkAccess, ChunkGenerator<?> chunkGenerator, StructureManager structureManager) {
			if (this.center == null) {
				this.center = structureManager.getOrCreate(new ResourceLocation("b_center"));
			}

			if (this.side == null) {
				this.side = structureManager.getOrCreate(new ResourceLocation("b_side"));
			}

			if (this.legs == null) {
				this.legs = structureManager.getOrCreate(new ResourceLocation("b_legs"));
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
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(16 * i + 5, 0, 16 * j + 5);
			StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings();
			int k = 32;

			for (int l = 0; l < 32; l++) {
				this.legs.placeInWorld(worldGenRegion, mutableBlockPos, mutableBlockPos, structurePlaceSettings, 20);
				mutableBlockPos.move(0, 4, 0);
			}

			this.center.placeInWorld(worldGenRegion, mutableBlockPos, mutableBlockPos, structurePlaceSettings, 20);
			WorldgenRandom worldgenRandom = new WorldgenRandom();
			worldgenRandom.setBaseChunkSeed(2 * i, 2 * j - 1);
			if (worldgenRandom.nextBoolean()) {
				mutableBlockPos.set(16 * i + 10, 128, 16 * j + 4);
				this.side.placeInWorld(worldGenRegion, mutableBlockPos, mutableBlockPos, new StructurePlaceSettings().setRotation(Rotation.CLOCKWISE_180), 20);
			}

			worldgenRandom.setBaseChunkSeed(2 * i, 2 * j + 1);
			if (worldgenRandom.nextBoolean()) {
				mutableBlockPos.set(16 * i + 5, 128, 16 * j + 11);
				this.side.placeInWorld(worldGenRegion, mutableBlockPos, mutableBlockPos, new StructurePlaceSettings(), 20);
			}

			worldgenRandom.setBaseChunkSeed(2 * i - 1, 2 * j);
			if (worldgenRandom.nextBoolean()) {
				mutableBlockPos.set(16 * i + 4, 128, 16 * j + 5);
				this.side.placeInWorld(worldGenRegion, mutableBlockPos, mutableBlockPos, new StructurePlaceSettings().setRotation(Rotation.CLOCKWISE_90), 20);
			}

			worldgenRandom.setBaseChunkSeed(2 * i + 1, 2 * j);
			if (worldgenRandom.nextBoolean()) {
				mutableBlockPos.set(16 * i + 11, 128, 16 * j + 10);
				this.side.placeInWorld(worldGenRegion, mutableBlockPos, mutableBlockPos, new StructurePlaceSettings().setRotation(Rotation.COUNTERCLOCKWISE_90), 20);
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
			return ChunkGeneratorType.T14;
		}
	}
}
