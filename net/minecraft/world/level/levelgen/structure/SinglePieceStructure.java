/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure;

import java.util.Optional;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public abstract class SinglePieceStructure
extends Structure {
    private final PieceConstructor constructor;
    private final int width;
    private final int depth;

    protected SinglePieceStructure(PieceConstructor pieceConstructor, int i, int j, Structure.StructureSettings structureSettings) {
        super(structureSettings);
        this.constructor = pieceConstructor;
        this.width = i;
        this.depth = j;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        if (SinglePieceStructure.getLowestY(generationContext, this.width, this.depth) < generationContext.chunkGenerator().getSeaLevel()) {
            return Optional.empty();
        }
        return SinglePieceStructure.onTopOfChunkCenter(generationContext, Heightmap.Types.WORLD_SURFACE_WG, structurePiecesBuilder -> this.generatePieces((StructurePiecesBuilder)structurePiecesBuilder, generationContext));
    }

    private void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
        ChunkPos chunkPos = generationContext.chunkPos();
        structurePiecesBuilder.addPiece(this.constructor.construct(generationContext.random(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ()));
    }

    @FunctionalInterface
    protected static interface PieceConstructor {
        public StructurePiece construct(WorldgenRandom var1, int var2, int var3);
    }
}

