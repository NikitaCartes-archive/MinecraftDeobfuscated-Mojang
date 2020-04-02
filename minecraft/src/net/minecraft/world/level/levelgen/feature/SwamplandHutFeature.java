package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.SwamplandHutPiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class SwamplandHutFeature extends RandomScatteredFeature<NoneFeatureConfiguration> {
	private static final List<Biome.SpawnerData> SWAMPHUT_ENEMIES = Lists.<Biome.SpawnerData>newArrayList(new Biome.SpawnerData(EntityType.WITCH, 1, 1, 1));
	private static final List<Biome.SpawnerData> SWAMPHUT_ANIMALS = Lists.<Biome.SpawnerData>newArrayList(new Biome.SpawnerData(EntityType.CAT, 1, 1, 1));

	public SwamplandHutFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	@Override
	public String getFeatureName() {
		return "Swamp_Hut";
	}

	@Override
	public int getLookupRange() {
		return 3;
	}

	@Override
	public StructureFeature.StructureStartFactory getStartFactory() {
		return SwamplandHutFeature.FeatureStart::new;
	}

	@Override
	protected int getRandomSalt() {
		return 14357620;
	}

	@Override
	public List<Biome.SpawnerData> getSpecialEnemies() {
		return SWAMPHUT_ENEMIES;
	}

	@Override
	public List<Biome.SpawnerData> getSpecialAnimals() {
		return SWAMPHUT_ANIMALS;
	}

	public boolean isSwamphut(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, BlockPos blockPos) {
		StructureStart structureStart = this.getStructureAt(levelAccessor, structureFeatureManager, blockPos, true);
		if (structureStart.isValid() && structureStart instanceof SwamplandHutFeature.FeatureStart) {
			StructurePiece structurePiece = (StructurePiece)structureStart.getPieces().get(0);
			return structurePiece instanceof SwamplandHutPiece;
		} else {
			return false;
		}
	}

	public static class FeatureStart extends StructureStart {
		public FeatureStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		@Override
		public void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
			SwamplandHutPiece swamplandHutPiece = new SwamplandHutPiece(this.random, i * 16, j * 16);
			this.pieces.add(swamplandHutPiece);
			this.calculateBoundingBox();
		}
	}
}
