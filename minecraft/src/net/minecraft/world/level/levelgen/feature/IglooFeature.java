package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.IglooPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class IglooFeature extends RandomScatteredFeature<NoneFeatureConfiguration> {
	public IglooFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	@Override
	public String getFeatureName() {
		return "Igloo";
	}

	@Override
	public int getLookupRange() {
		return 3;
	}

	@Override
	public StructureFeature.StructureStartFactory getStartFactory() {
		return IglooFeature.FeatureStart::new;
	}

	@Override
	protected int getRandomSalt(ChunkGeneratorSettings chunkGeneratorSettings) {
		return 14357618;
	}

	public static class FeatureStart extends StructureStart {
		public FeatureStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		@Override
		public void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
			NoneFeatureConfiguration noneFeatureConfiguration = chunkGenerator.getStructureConfiguration(biome, Feature.IGLOO);
			int k = i * 16;
			int l = j * 16;
			BlockPos blockPos = new BlockPos(k, 90, l);
			Rotation rotation = Rotation.getRandom(this.random);
			IglooPieces.addPieces(structureManager, blockPos, rotation, this.pieces, this.random, noneFeatureConfiguration);
			this.calculateBoundingBox();
		}
	}
}
