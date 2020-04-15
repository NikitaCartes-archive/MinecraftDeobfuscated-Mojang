package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class VillageFeature extends StructureFeature<JigsawConfiguration> {
	public VillageFeature(Function<Dynamic<?>, ? extends JigsawConfiguration> function) {
		super(function);
	}

	@Override
	protected int getSpacing(DimensionType dimensionType, ChunkGeneratorSettings chunkGeneratorSettings) {
		return chunkGeneratorSettings.getVillagesSpacing();
	}

	@Override
	protected int getSeparation(DimensionType dimensionType, ChunkGeneratorSettings chunkGeneratorSettings) {
		return chunkGeneratorSettings.getVillagesSeparation();
	}

	@Override
	protected int getRandomSalt(ChunkGeneratorSettings chunkGeneratorSettings) {
		return 10387312;
	}

	@Override
	public StructureFeature.StructureStartFactory getStartFactory() {
		return VillageFeature.FeatureStart::new;
	}

	@Override
	public String getFeatureName() {
		return "Village";
	}

	@Override
	public int getLookupRange() {
		return 8;
	}

	public static class FeatureStart extends BeardedStructureStart {
		public FeatureStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		@Override
		public void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
			JigsawConfiguration jigsawConfiguration = chunkGenerator.getStructureConfiguration(biome, Feature.VILLAGE);
			BlockPos blockPos = new BlockPos(i * 16, 0, j * 16);
			VillagePieces.addPieces(chunkGenerator, structureManager, blockPos, this.pieces, this.random, jigsawConfiguration);
			this.calculateBoundingBox();
		}
	}
}
