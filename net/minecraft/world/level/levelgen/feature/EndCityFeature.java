/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.EndCityPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public class EndCityFeature
extends StructureFeature<NoneFeatureConfiguration> {
    private static final int RANDOM_SALT = 10387313;

    public EndCityFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec, EndCityFeature::pieceGeneratorSupplier);
    }

    private static int getYPositionForFeature(ChunkPos chunkPos, ChunkGenerator chunkGenerator, LevelHeightAccessor levelHeightAccessor) {
        Random random = new Random(chunkPos.x + chunkPos.z * 10387313);
        Rotation rotation = Rotation.getRandom(random);
        int i = 5;
        int j = 5;
        if (rotation == Rotation.CLOCKWISE_90) {
            i = -5;
        } else if (rotation == Rotation.CLOCKWISE_180) {
            i = -5;
            j = -5;
        } else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
            j = -5;
        }
        int k = chunkPos.getBlockX(7);
        int l = chunkPos.getBlockZ(7);
        int m = chunkGenerator.getFirstOccupiedHeight(k, l, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
        int n = chunkGenerator.getFirstOccupiedHeight(k, l + j, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
        int o = chunkGenerator.getFirstOccupiedHeight(k + i, l, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
        int p = chunkGenerator.getFirstOccupiedHeight(k + i, l + j, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
        return Math.min(Math.min(m, n), Math.min(o, p));
    }

    private static Optional<PieceGenerator<NoneFeatureConfiguration>> pieceGeneratorSupplier(PieceGeneratorSupplier.Context<NoneFeatureConfiguration> context2) {
        int i = EndCityFeature.getYPositionForFeature(context2.chunkPos(), context2.chunkGenerator(), context2.heightAccessor());
        if (i < 60) {
            return Optional.empty();
        }
        BlockPos blockPos = context2.chunkPos().getMiddleBlockPosition(i);
        if (!context2.validBiome().test(context2.chunkGenerator().getNoiseBiome(QuartPos.fromBlock(blockPos.getX()), QuartPos.fromBlock(blockPos.getY()), QuartPos.fromBlock(blockPos.getZ())))) {
            return Optional.empty();
        }
        return Optional.of((structurePiecesBuilder, context) -> {
            Rotation rotation = Rotation.getRandom(context.random());
            ArrayList<StructurePiece> list = Lists.newArrayList();
            EndCityPieces.startHouseTower(context.structureManager(), blockPos, rotation, list, context.random());
            list.forEach(structurePiecesBuilder::addPiece);
        });
    }
}

