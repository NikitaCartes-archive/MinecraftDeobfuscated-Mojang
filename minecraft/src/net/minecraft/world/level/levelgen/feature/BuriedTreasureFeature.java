package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BuriedTreasurePieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class BuriedTreasureFeature extends StructureFeature<ProbabilityFeatureConfiguration> {
	private static final int RANDOM_SALT = 10387320;

	public BuriedTreasureFeature(Codec<ProbabilityFeatureConfiguration> codec) {
		super(codec, PieceGeneratorSupplier.simple(BuriedTreasureFeature::checkLocation, BuriedTreasureFeature::generatePieces));
	}

	private static boolean checkLocation(PieceGeneratorSupplier.Context<ProbabilityFeatureConfiguration> context) {
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
		worldgenRandom.setLargeFeatureWithSalt(context.seed(), context.chunkPos().x, context.chunkPos().z, 10387320);
		return worldgenRandom.nextFloat() < ((ProbabilityFeatureConfiguration)context.config()).probability
			&& context.validBiomeOnTop(Heightmap.Types.OCEAN_FLOOR_WG);
	}

	private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, PieceGenerator.Context<ProbabilityFeatureConfiguration> context) {
		BlockPos blockPos = new BlockPos(context.chunkPos().getBlockX(9), 90, context.chunkPos().getBlockZ(9));
		structurePiecesBuilder.addPiece(new BuriedTreasurePieces.BuriedTreasurePiece(blockPos));
	}

	@Override
	public BlockPos getLocatePos(ChunkPos chunkPos) {
		return new BlockPos(chunkPos.getBlockX(9), 0, chunkPos.getBlockZ(9));
	}
}
