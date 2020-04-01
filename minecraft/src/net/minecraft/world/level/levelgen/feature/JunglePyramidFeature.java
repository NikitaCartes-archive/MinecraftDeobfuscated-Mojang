package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.JunglePyramidPiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class JunglePyramidFeature extends RandomScatteredFeature<NoneFeatureConfiguration> {
	public JunglePyramidFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, Function<Random, ? extends NoneFeatureConfiguration> function2) {
		super(function, function2);
	}

	@Override
	public String getFeatureName() {
		return "Jungle_Pyramid";
	}

	@Override
	public int getLookupRange() {
		return 3;
	}

	@Override
	public StructureFeature.StructureStartFactory getStartFactory() {
		return JunglePyramidFeature.FeatureStart::new;
	}

	@Override
	protected int getRandomSalt() {
		return 14357619;
	}

	public static class FeatureStart extends StructureStart {
		public FeatureStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		@Override
		public void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
			JunglePyramidPiece junglePyramidPiece = new JunglePyramidPiece(this.random, i * 16, j * 16);
			this.pieces.add(junglePyramidPiece);
			this.calculateBoundingBox();
		}
	}
}
