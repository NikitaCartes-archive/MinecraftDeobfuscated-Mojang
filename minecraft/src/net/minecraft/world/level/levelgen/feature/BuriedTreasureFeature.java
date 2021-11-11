package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BuriedTreasurePieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class BuriedTreasureFeature extends StructureFeature<ProbabilityFeatureConfiguration> {
	private static final int RANDOM_SALT = 10387320;

	public BuriedTreasureFeature(Codec<ProbabilityFeatureConfiguration> codec) {
		super(codec, BuriedTreasureFeature::generatePieces);
	}

	protected boolean isFeatureChunk(
		ChunkGenerator chunkGenerator,
		BiomeSource biomeSource,
		long l,
		ChunkPos chunkPos,
		ProbabilityFeatureConfiguration probabilityFeatureConfiguration,
		LevelHeightAccessor levelHeightAccessor
	) {
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
		worldgenRandom.setLargeFeatureWithSalt(l, chunkPos.x, chunkPos.z, 10387320);
		return worldgenRandom.nextFloat() < probabilityFeatureConfiguration.probability;
	}

	private static void generatePieces(
		StructurePiecesBuilder structurePiecesBuilder, ProbabilityFeatureConfiguration probabilityFeatureConfiguration, PieceGenerator.Context context
	) {
		if (context.validBiomeOnTop(Heightmap.Types.OCEAN_FLOOR_WG)) {
			BlockPos blockPos = new BlockPos(context.chunkPos().getBlockX(9), 90, context.chunkPos().getBlockZ(9));
			structurePiecesBuilder.addPiece(new BuriedTreasurePieces.BuriedTreasurePiece(blockPos));
		}
	}

	@Override
	public BlockPos getLocatePos(ChunkPos chunkPos) {
		return new BlockPos(chunkPos.getBlockX(9), 0, chunkPos.getBlockZ(9));
	}
}
