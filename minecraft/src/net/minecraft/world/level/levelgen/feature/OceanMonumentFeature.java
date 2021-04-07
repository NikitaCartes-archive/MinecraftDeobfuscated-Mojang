package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.OceanMonumentPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class OceanMonumentFeature extends StructureFeature<NoneFeatureConfiguration> {
	private static final WeightedRandomList<MobSpawnSettings.SpawnerData> MONUMENT_ENEMIES = WeightedRandomList.create(
		new MobSpawnSettings.SpawnerData(EntityType.GUARDIAN, 1, 2, 4)
	);

	public OceanMonumentFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	protected boolean linearSeparation() {
		return false;
	}

	protected boolean isFeatureChunk(
		ChunkGenerator chunkGenerator,
		BiomeSource biomeSource,
		long l,
		WorldgenRandom worldgenRandom,
		ChunkPos chunkPos,
		Biome biome,
		ChunkPos chunkPos2,
		NoneFeatureConfiguration noneFeatureConfiguration,
		LevelHeightAccessor levelHeightAccessor
	) {
		int i = chunkPos.getBlockX(9);
		int j = chunkPos.getBlockZ(9);

		for (Biome biome2 : biomeSource.getBiomesWithin(i, chunkGenerator.getSeaLevel(), j, 16)) {
			if (!biome2.getGenerationSettings().isValidStart(this)) {
				return false;
			}
		}

		for (Biome biome3 : biomeSource.getBiomesWithin(i, chunkGenerator.getSeaLevel(), j, 29)) {
			if (biome3.getBiomeCategory() != Biome.BiomeCategory.OCEAN && biome3.getBiomeCategory() != Biome.BiomeCategory.RIVER) {
				return false;
			}
		}

		return true;
	}

	@Override
	public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return OceanMonumentFeature.OceanMonumentStart::new;
	}

	@Override
	public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
		return MONUMENT_ENEMIES;
	}

	public static class OceanMonumentStart extends StructureStart<NoneFeatureConfiguration> {
		private boolean isCreated;

		public OceanMonumentStart(StructureFeature<NoneFeatureConfiguration> structureFeature, ChunkPos chunkPos, int i, long l) {
			super(structureFeature, chunkPos, i, l);
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			ChunkPos chunkPos,
			Biome biome,
			NoneFeatureConfiguration noneFeatureConfiguration,
			LevelHeightAccessor levelHeightAccessor
		) {
			this.generatePieces(chunkPos);
		}

		private void generatePieces(ChunkPos chunkPos) {
			int i = chunkPos.getMinBlockX() - 29;
			int j = chunkPos.getMinBlockZ() - 29;
			Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(this.random);
			this.addPiece(new OceanMonumentPieces.MonumentBuilding(this.random, i, j, direction));
			this.isCreated = true;
		}

		@Override
		public void placeInChunk(
			WorldGenLevel worldGenLevel,
			StructureFeatureManager structureFeatureManager,
			ChunkGenerator chunkGenerator,
			Random random,
			BoundingBox boundingBox,
			ChunkPos chunkPos
		) {
			if (!this.isCreated) {
				this.pieces.clear();
				this.generatePieces(this.getChunkPos());
			}

			super.placeInChunk(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos);
		}
	}
}
