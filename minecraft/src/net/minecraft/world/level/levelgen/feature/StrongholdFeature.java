package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureFeature;
import net.minecraft.world.level.levelgen.structure.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class StrongholdFeature extends NoiseAffectingStructureFeature<NoneFeatureConfiguration> {
	public StrongholdFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec, PieceGeneratorSupplier.simple(StrongholdFeature::checkLocation, StrongholdFeature::generatePieces));
	}

	private static boolean checkLocation(PieceGeneratorSupplier.Context<NoneFeatureConfiguration> context) {
		return context.chunkGenerator().hasStronghold(context.chunkPos());
	}

	private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, PieceGenerator.Context<NoneFeatureConfiguration> context) {
		int i = 0;

		StrongholdPieces.StartPiece startPiece;
		do {
			structurePiecesBuilder.clear();
			context.random().setLargeFeatureSeed(context.seed() + (long)(i++), context.chunkPos().x, context.chunkPos().z);
			StrongholdPieces.resetPieces();
			startPiece = new StrongholdPieces.StartPiece(context.random(), context.chunkPos().getBlockX(2), context.chunkPos().getBlockZ(2));
			structurePiecesBuilder.addPiece(startPiece);
			startPiece.addChildren(startPiece, structurePiecesBuilder, context.random());
			List<StructurePiece> list = startPiece.pendingChildren;

			while (!list.isEmpty()) {
				int j = context.random().nextInt(list.size());
				StructurePiece structurePiece = (StructurePiece)list.remove(j);
				structurePiece.addChildren(startPiece, structurePiecesBuilder, context.random());
			}

			structurePiecesBuilder.moveBelowSeaLevel(context.chunkGenerator().getSeaLevel(), context.chunkGenerator().getMinY(), context.random(), 10);
		} while (structurePiecesBuilder.isEmpty() || startPiece.portalRoomPiece == null);
	}
}
