/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.LinkedList;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionPieces;

public class WoodlandMansionStructure
extends Structure {
    public static final Codec<WoodlandMansionStructure> CODEC = WoodlandMansionStructure.simpleCodec(WoodlandMansionStructure::new);

    public WoodlandMansionStructure(Structure.StructureSettings structureSettings) {
        super(structureSettings);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        Rotation rotation = Rotation.getRandom(generationContext.random());
        BlockPos blockPos = this.getLowestYIn5by5BoxOffset7Blocks(generationContext, rotation);
        if (blockPos.getY() < 60) {
            return Optional.empty();
        }
        return Optional.of(new Structure.GenerationStub(blockPos, structurePiecesBuilder -> this.generatePieces((StructurePiecesBuilder)structurePiecesBuilder, generationContext, blockPos, rotation)));
    }

    private void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext, BlockPos blockPos, Rotation rotation) {
        LinkedList<WoodlandMansionPieces.WoodlandMansionPiece> list = Lists.newLinkedList();
        WoodlandMansionPieces.generateMansion(generationContext.structureTemplateManager(), blockPos, rotation, list, generationContext.random());
        list.forEach(structurePiecesBuilder::addPiece);
    }

    @Override
    public void afterPlace(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, PiecesContainer piecesContainer) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int i = worldGenLevel.getMinBuildHeight();
        BoundingBox boundingBox2 = piecesContainer.calculateBoundingBox();
        int j = boundingBox2.minY();
        for (int k = boundingBox.minX(); k <= boundingBox.maxX(); ++k) {
            block1: for (int l = boundingBox.minZ(); l <= boundingBox.maxZ(); ++l) {
                mutableBlockPos.set(k, j, l);
                if (worldGenLevel.isEmptyBlock(mutableBlockPos) || !boundingBox2.isInside(mutableBlockPos) || !piecesContainer.isInsidePiece(mutableBlockPos)) continue;
                for (int m = j - 1; m > i; --m) {
                    mutableBlockPos.setY(m);
                    if (!worldGenLevel.isEmptyBlock(mutableBlockPos) && !worldGenLevel.getBlockState(mutableBlockPos).getMaterial().isLiquid()) continue block1;
                    worldGenLevel.setBlock(mutableBlockPos, Blocks.COBBLESTONE.defaultBlockState(), 2);
                }
            }
        }
    }

    @Override
    public StructureType<?> type() {
        return StructureType.WOODLAND_MANSION;
    }
}

