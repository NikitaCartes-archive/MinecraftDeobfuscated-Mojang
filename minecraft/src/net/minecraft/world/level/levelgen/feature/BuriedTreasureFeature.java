package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
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
		ChunkPos chunkPos,
		Biome biome,
		ChunkPos chunkPos2,
		ProbabilityFeatureConfiguration probabilityFeatureConfiguration,
		LevelHeightAccessor levelHeightAccessor
	) {
		worldgenRandom.setLargeFeatureWithSalt(l, chunkPos.x, chunkPos.z, 10387320);
		return worldgenRandom.nextFloat() < probabilityFeatureConfiguration.probability;
	}

	@Override
	public StructureFeature.StructureStartFactory<ProbabilityFeatureConfiguration> getStartFactory() {
		return BuriedTreasureFeature.BuriedTreasureStart::new;
	}

	public static class BuriedTreasureStart extends StructureStart<ProbabilityFeatureConfiguration> {
		public BuriedTreasureStart(StructureFeature<ProbabilityFeatureConfiguration> structureFeature, ChunkPos chunkPos, BoundingBox boundingBox, int i, long l) {
			super(structureFeature, chunkPos, boundingBox, i, l);
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			ChunkPos chunkPos,
			Biome biome,
			ProbabilityFeatureConfiguration probabilityFeatureConfiguration,
			LevelHeightAccessor levelHeightAccessor
		) {
			BlockPos blockPos = new BlockPos(chunkPos.getBlockX(9), 90, chunkPos.getBlockZ(9));
			this.pieces.add(new BuriedTreasurePieces.BuriedTreasurePiece(blockPos));
			this.calculateBoundingBox();
		}

		@Override
		public BlockPos getLocatePos() {
			ChunkPos chunkPos = this.getChunkPos();
			return new BlockPos(chunkPos.getBlockX(9), 0, chunkPos.getBlockZ(9));
		}
	}
}
