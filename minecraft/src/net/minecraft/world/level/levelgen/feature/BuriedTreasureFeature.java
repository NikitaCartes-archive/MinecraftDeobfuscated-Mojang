package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuriedTreasurePieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class BuriedTreasureFeature extends StructureFeature<ProbabilityFeatureConfiguration> {
	public BuriedTreasureFeature(Codec<ProbabilityFeatureConfiguration> codec) {
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
		ProbabilityFeatureConfiguration probabilityFeatureConfiguration
	) {
		worldgenRandom.setLargeFeatureWithSalt(l, i, j, 10387320);
		return worldgenRandom.nextFloat() < probabilityFeatureConfiguration.probability;
	}

	@Override
	public StructureFeature.StructureStartFactory<ProbabilityFeatureConfiguration> getStartFactory() {
		return BuriedTreasureFeature.BuriedTreasureStart::new;
	}

	public static class BuriedTreasureStart extends StructureStart<ProbabilityFeatureConfiguration> {
		public BuriedTreasureStart(StructureFeature<ProbabilityFeatureConfiguration> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			int i,
			int j,
			Biome biome,
			ProbabilityFeatureConfiguration probabilityFeatureConfiguration
		) {
			int k = i * 16;
			int l = j * 16;
			BlockPos blockPos = new BlockPos(k + 9, 90, l + 9);
			this.pieces.add(new BuriedTreasurePieces.BuriedTreasurePiece(blockPos));
			this.calculateBoundingBox();
		}

		@Override
		public BlockPos getLocatePos() {
			return new BlockPos((this.getChunkX() << 4) + 9, 0, (this.getChunkZ() << 4) + 9);
		}
	}
}
