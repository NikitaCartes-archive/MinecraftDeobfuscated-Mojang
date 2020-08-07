package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
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
	private static final List<MobSpawnSettings.SpawnerData> MONUMENT_ENEMIES = ImmutableList.of(new MobSpawnSettings.SpawnerData(EntityType.GUARDIAN, 1, 2, 4));

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
		int i,
		int j,
		Biome biome,
		ChunkPos chunkPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		for (Biome biome2 : biomeSource.getBiomesWithin(i * 16 + 9, chunkGenerator.getSeaLevel(), j * 16 + 9, 16)) {
			if (!biome2.getGenerationSettings().isValidStart(this)) {
				return false;
			}
		}

		for (Biome biome3 : biomeSource.getBiomesWithin(i * 16 + 9, chunkGenerator.getSeaLevel(), j * 16 + 9, 29)) {
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
	public List<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
		return MONUMENT_ENEMIES;
	}

	public static class OceanMonumentStart extends StructureStart<NoneFeatureConfiguration> {
		private boolean isCreated;

		public OceanMonumentStart(StructureFeature<NoneFeatureConfiguration> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			int i,
			int j,
			Biome biome,
			NoneFeatureConfiguration noneFeatureConfiguration
		) {
			this.generatePieces(i, j);
		}

		private void generatePieces(int i, int j) {
			int k = i * 16 - 29;
			int l = j * 16 - 29;
			Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(this.random);
			this.pieces.add(new OceanMonumentPieces.MonumentBuilding(this.random, k, l, direction));
			this.calculateBoundingBox();
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
				this.generatePieces(this.getChunkX(), this.getChunkZ());
			}

			super.placeInChunk(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos);
		}
	}
}
