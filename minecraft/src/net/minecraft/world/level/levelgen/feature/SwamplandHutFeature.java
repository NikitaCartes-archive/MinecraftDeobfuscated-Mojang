package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.SwamplandHutPiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class SwamplandHutFeature extends StructureFeature<NoneFeatureConfiguration> {
	private static final List<Biome.SpawnerData> SWAMPHUT_ENEMIES = Lists.<Biome.SpawnerData>newArrayList(new Biome.SpawnerData(EntityType.WITCH, 1, 1, 1));
	private static final List<Biome.SpawnerData> SWAMPHUT_ANIMALS = Lists.<Biome.SpawnerData>newArrayList(new Biome.SpawnerData(EntityType.CAT, 1, 1, 1));

	public SwamplandHutFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return SwamplandHutFeature.FeatureStart::new;
	}

	@Override
	public List<Biome.SpawnerData> getSpecialEnemies() {
		return SWAMPHUT_ENEMIES;
	}

	@Override
	public List<Biome.SpawnerData> getSpecialAnimals() {
		return SWAMPHUT_ANIMALS;
	}

	public static class FeatureStart extends StructureStart<NoneFeatureConfiguration> {
		public FeatureStart(StructureFeature<NoneFeatureConfiguration> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		public void generatePieces(
			ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome, NoneFeatureConfiguration noneFeatureConfiguration
		) {
			SwamplandHutPiece swamplandHutPiece = new SwamplandHutPiece(this.random, i * 16, j * 16);
			this.pieces.add(swamplandHutPiece);
			this.calculateBoundingBox();
		}
	}
}
