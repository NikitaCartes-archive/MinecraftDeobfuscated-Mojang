/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.data.loot.packs.UpdateOneTwentyBuiltInLootTables;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.structures.DesertPyramidPiece;

public class DesertPyramidStructure
extends SinglePieceStructure {
    public static final Codec<DesertPyramidStructure> CODEC = DesertPyramidStructure.simpleCodec(DesertPyramidStructure::new);

    public DesertPyramidStructure(Structure.StructureSettings structureSettings) {
        super(DesertPyramidPiece::new, 21, 21, structureSettings);
    }

    @Override
    public void afterPlace(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, PiecesContainer piecesContainer) {
        if (!worldGenLevel.enabledFeatures().contains(FeatureFlags.UPDATE_1_20)) {
            return;
        }
        SortedArraySet set = SortedArraySet.create(Vec3i::compareTo);
        for (StructurePiece structurePiece : piecesContainer.pieces()) {
            if (!(structurePiece instanceof DesertPyramidPiece)) continue;
            DesertPyramidPiece desertPyramidPiece = (DesertPyramidPiece)structurePiece;
            set.addAll(desertPyramidPiece.getPotentialSuspiciousSandWorldPositions());
        }
        ObjectArrayList objectArrayList = new ObjectArrayList(set.stream().toList());
        Util.shuffle(objectArrayList, randomSource);
        int i = Math.min(set.size(), randomSource.nextInt(5, 8));
        for (BlockPos blockPos : objectArrayList) {
            if (i > 0) {
                --i;
                worldGenLevel.setBlock(blockPos, Blocks.SUSPICIOUS_SAND.defaultBlockState(), 2);
                worldGenLevel.getBlockEntity(blockPos, BlockEntityType.SUSPICIOUS_SAND).ifPresent(suspiciousSandBlockEntity -> suspiciousSandBlockEntity.setLootTable(UpdateOneTwentyBuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY, blockPos.asLong()));
                continue;
            }
            worldGenLevel.setBlock(blockPos, Blocks.SAND.defaultBlockState(), 2);
        }
    }

    @Override
    public StructureType<?> type() {
        return StructureType.DESERT_PYRAMID;
    }
}

