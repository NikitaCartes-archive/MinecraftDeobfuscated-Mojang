package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureFeature;
import net.minecraft.world.level.levelgen.structure.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class StrongholdFeature extends NoiseAffectingStructureFeature<NoneFeatureConfiguration> {
	public StrongholdFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec, StrongholdFeature::generatePieces);
	}

	protected boolean isFeatureChunk(
		ChunkGenerator chunkGenerator,
		BiomeSource biomeSource,
		long l,
		WorldgenRandom worldgenRandom,
		ChunkPos chunkPos,
		ChunkPos chunkPos2,
		NoneFeatureConfiguration noneFeatureConfiguration,
		LevelHeightAccessor levelHeightAccessor
	) {
		return chunkGenerator.hasStronghold(chunkPos);
	}

	private static void generatePieces(
		StructurePiecesBuilder structurePiecesBuilder, NoneFeatureConfiguration noneFeatureConfiguration, PieceGenerator.Context context
	) {
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
