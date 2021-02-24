package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.SwamplandHutPiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class SwamplandHutFeature extends StructureFeature<NoneFeatureConfiguration> {
	private static final List<MobSpawnSettings.SpawnerData> SWAMPHUT_ENEMIES = ImmutableList.of(new MobSpawnSettings.SpawnerData(EntityType.WITCH, 1, 1, 1));
	private static final List<MobSpawnSettings.SpawnerData> SWAMPHUT_ANIMALS = ImmutableList.of(new MobSpawnSettings.SpawnerData(EntityType.CAT, 1, 1, 1));

	public SwamplandHutFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return SwamplandHutFeature.FeatureStart::new;
	}

	@Override
	public List<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
		return SWAMPHUT_ENEMIES;
	}

	@Override
	public List<MobSpawnSettings.SpawnerData> getSpecialAnimals() {
		return SWAMPHUT_ANIMALS;
	}

	public static class FeatureStart extends StructureStart<NoneFeatureConfiguration> {
		public FeatureStart(StructureFeature<NoneFeatureConfiguration> structureFeature, ChunkPos chunkPos, BoundingBox boundingBox, int i, long l) {
			super(structureFeature, chunkPos, boundingBox, i, l);
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
			SwamplandHutPiece swamplandHutPiece = new SwamplandHutPiece(this.random, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
			this.pieces.add(swamplandHutPiece);
			this.calculateBoundingBox();
		}
	}
}
