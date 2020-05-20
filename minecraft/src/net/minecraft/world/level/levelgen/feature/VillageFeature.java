package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class VillageFeature extends StructureFeature<JigsawConfiguration> {
	public VillageFeature(Codec<JigsawConfiguration> codec) {
		super(codec);
	}

	@Override
	public StructureFeature.StructureStartFactory<JigsawConfiguration> getStartFactory() {
		return VillageFeature.FeatureStart::new;
	}

	public static class FeatureStart extends BeardedStructureStart<JigsawConfiguration> {
		public FeatureStart(StructureFeature<JigsawConfiguration> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		public void generatePieces(
			ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome, JigsawConfiguration jigsawConfiguration
		) {
			BlockPos blockPos = new BlockPos(i * 16, 0, j * 16);
			VillagePieces.addPieces(chunkGenerator, structureManager, blockPos, this.pieces, this.random, jigsawConfiguration);
			this.calculateBoundingBox();
		}
	}
}
