package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.MultiJigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class BastionFeature extends StructureFeature<MultiJigsawConfiguration> {
	public BastionFeature(Codec<MultiJigsawConfiguration> codec) {
		super(codec);
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
		MultiJigsawConfiguration multiJigsawConfiguration
	) {
		return worldgenRandom.nextInt(6) >= 2;
	}

	@Override
	public StructureFeature.StructureStartFactory<MultiJigsawConfiguration> getStartFactory() {
		return BastionFeature.FeatureStart::new;
	}

	public static class FeatureStart extends BeardedStructureStart<MultiJigsawConfiguration> {
		public FeatureStart(StructureFeature<MultiJigsawConfiguration> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		public void generatePieces(
			ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome, MultiJigsawConfiguration multiJigsawConfiguration
		) {
			BlockPos blockPos = new BlockPos(i * 16, 33, j * 16);
			BastionPieces.addPieces(chunkGenerator, structureManager, blockPos, this.pieces, this.random, multiJigsawConfiguration);
			this.calculateBoundingBox();
		}
	}
}
