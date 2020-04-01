package net.minecraft.world.level.dimension.special;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;

public class G03 extends SpecialDimensionBase {
	public G03(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 1.5F);
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G03.Generator(this.level, fixedBiome(Biomes.BETWEEN), NoneGeneratorSettings.INSTANCE);
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

	public static class BetweenBiome extends Biome {
		public BetweenBiome() {
			super(
				new Biome.BiomeBuilder()
					.surfaceBuilder(SurfaceBuilder.NOPE, SurfaceBuilder.CONFIG_STONE)
					.precipitation(Biome.Precipitation.NONE)
					.biomeCategory(Biome.BiomeCategory.NONE)
					.depth(0.1F)
					.scale(0.2F)
					.temperature(0.5F)
					.downfall(0.5F)
					.specialEffects(
						new BiomeSpecialEffects.Builder()
							.waterColor(4159204)
							.waterFogColor(329011)
							.fogColor(12638463)
							.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
							.build()
					)
					.parent(null)
			);
			this.addStructureStart(Feature.SHIP.configured(FeatureConfiguration.NONE));
			this.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Feature.SHIP.configured(FeatureConfiguration.NONE));
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
		public void applyCarvers(BiomeManager biomeManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
		}

		@Override
		public int getSpawnHeight() {
			return 0;
		}

		@Override
		public void fillFromNoise(LevelAccessor levelAccessor, ChunkAccess chunkAccess) {
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
			return ChunkGeneratorType.T03;
		}
	}

	public static class ShipFeature extends StructureFeature<NoneFeatureConfiguration> {
		public ShipFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, Function<Random, ? extends NoneFeatureConfiguration> function2) {
			super(function, function2);
		}

		@Override
		public boolean isFeatureChunk(BiomeManager biomeManager, ChunkGenerator<?> chunkGenerator, Random random, int i, int j, Biome biome) {
			return (i & 1) == 0 && (j & 1) == 0;
		}

		@Override
		public StructureFeature.StructureStartFactory getStartFactory() {
			return G03.ShipStart::new;
		}

		@Override
		public String getFeatureName() {
			return "Ship";
		}

		@Override
		public int getLookupRange() {
			return 0;
		}
	}

	public static class ShipPiece extends TemplateStructurePiece {
		private static final ResourceLocation END_SHIP = new ResourceLocation("end_city/ship");

		public ShipPiece(StructureManager structureManager, BlockPos blockPos) {
			super(StructurePieceType.FLEET_PIECE, 0);
			this.templatePosition = blockPos;
			this.configure(structureManager);
		}

		public ShipPiece(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.FLEET_PIECE, compoundTag);
			this.configure(structureManager);
		}

		private void configure(StructureManager structureManager) {
			StructureTemplate structureTemplate = structureManager.getOrCreate(END_SHIP);
			StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings().addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR).setIgnoreEntities(true);
			this.setup(structureTemplate, this.templatePosition, structurePlaceSettings);
		}

		@Override
		protected void handleDataMarker(String string, BlockPos blockPos, LevelAccessor levelAccessor, Random random, BoundingBox boundingBox) {
			if (string.startsWith("Chest")) {
				BlockPos blockPos2 = blockPos.below();
				if (boundingBox.isInside(blockPos2)) {
					RandomizableContainerBlockEntity.setLootTable(levelAccessor, random, blockPos2, BuiltInLootTables.FLEET_ORDERS);
				}
			}
		}
	}

	static class ShipStart extends StructureStart {
		public ShipStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		@Override
		public void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
			this.pieces.add(new G03.ShipPiece(structureManager, new BlockPos(i * 16 + 9, 50, j * 16 + 9)));
			this.pieces.add(new G03.ShipPiece(structureManager, new BlockPos(i * 16 + 9, 100, j * 16 + 9)));
			this.pieces.add(new G03.ShipPiece(structureManager, new BlockPos(i * 16 + 9, 150, j * 16 + 9)));
			this.calculateBoundingBox();
		}
	}
}
