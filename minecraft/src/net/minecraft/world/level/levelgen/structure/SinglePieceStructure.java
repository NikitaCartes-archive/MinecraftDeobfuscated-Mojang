package net.minecraft.world.level.levelgen.structure;

import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public abstract class SinglePieceStructure extends Structure {
	private final SinglePieceStructure.PieceConstructor constructor;
	private int width;
	private int depth;

	protected SinglePieceStructure(
		SinglePieceStructure.PieceConstructor pieceConstructor,
		int i,
		int j,
		HolderSet<Biome> holderSet,
		Map<MobCategory, StructureSpawnOverride> map,
		GenerationStep.Decoration decoration,
		boolean bl
	) {
		super(holderSet, map, decoration, bl);
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
