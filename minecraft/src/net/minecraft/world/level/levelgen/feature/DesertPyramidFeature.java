package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.DesertPyramidPiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class DesertPyramidFeature extends RandomScatteredFeature<NoneFeatureConfiguration> {
	public DesertPyramidFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	@Override
	public String getFeatureName() {
		return "Desert_Pyramid";
	}

	@Override
	public int getLookupRange() {
		return 3;
	}

	@Override
	public StructureFeature.StructureStartFactory getStartFactory() {
		return DesertPyramidFeature.FeatureStart::new;
	}

	@Override
	protected int getRandomSalt() {
		return 14357617;
	}

	public static class FeatureStart extends StructureStart {
		public FeatureStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		@Override
		public void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
			DesertPyramidPiece desertPyramidPiece = new DesertPyramidPiece(this.random, i * 16, j * 16);
			this.pieces.add(desertPyramidPiece);
			this.calculateBoundingBox();
		}
	}
}
