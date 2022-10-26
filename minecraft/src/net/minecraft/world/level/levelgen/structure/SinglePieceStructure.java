package net.minecraft.world.level.levelgen.structure;

import java.util.Optional;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public abstract class SinglePieceStructure extends Structure {
	private final SinglePieceStructure.PieceConstructor constructor;
	private final int width;
	private final int depth;

	protected SinglePieceStructure(SinglePieceStructure.PieceConstructor pieceConstructor, int i, int j, Structure.StructureSettings structureSettings) {
		super(structureSettings);
		this.constructor = pieceConstructor;
		this.width = i;
		this.depth = j;
	}

	@Override
	public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
		return getLowestY(generationContext, this.width, this.depth) < generationContext.chunkGenerator().getSeaLevel()
			? Optional.empty()
			: onTopOfChunkCenter(
				generationContext, Heightmap.Types.WORLD_SURFACE_WG, structurePiecesBuilder -> this.generatePieces(structurePiecesBuilder, generationContext)
			);
	}

	private void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
		ChunkPos chunkPos = generationContext.chunkPos();
		structurePiecesBuilder.addPiece(this.constructor.construct(generationContext.random(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ()));
	}

	@FunctionalInterface
	protected interface PieceConstructor {
		StructurePiece construct(WorldgenRandom worldgenRandom, int i, int j);
	}
}
