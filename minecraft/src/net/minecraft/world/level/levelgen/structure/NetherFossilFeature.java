package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.RandomScatteredFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class NetherFossilFeature extends RandomScatteredFeature<NoneFeatureConfiguration> {
	public NetherFossilFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	@Override
	protected int getRandomSalt() {
		return 14357921;
	}

	@Override
	public StructureFeature.StructureStartFactory getStartFactory() {
		return NetherFossilFeature.FeatureStart::new;
	}

	@Override
	public String getFeatureName() {
		return "Nether_Fossil";
	}

	@Override
	protected int getSpacing(ChunkGenerator<?> chunkGenerator) {
		return 2;
	}

	@Override
	protected int getSeparation(ChunkGenerator<?> chunkGenerator) {
		return 1;
	}

	@Override
	public int getLookupRange() {
		return 3;
	}

	public static class FeatureStart extends StructureStart {
		public FeatureStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		@Override
		public void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
			int k = i * 16;
			int l = j * 16;
			int m = chunkGenerator.getSeaLevel() + this.random.nextInt(126 - chunkGenerator.getSeaLevel());
			NetherFossilPieces.addPieces(structureManager, this.pieces, this.random, new BlockPos(k + this.random.nextInt(16), m, l + this.random.nextInt(16)));
			this.calculateBoundingBox();
		}
	}
}
