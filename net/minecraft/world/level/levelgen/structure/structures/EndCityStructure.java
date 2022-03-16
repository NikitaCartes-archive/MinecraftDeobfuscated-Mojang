/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.EndCityPieces;

public class EndCityStructure
extends Structure {
    public static final Codec<EndCityStructure> CODEC = RecordCodecBuilder.create(instance -> EndCityStructure.codec(instance).apply(instance, EndCityStructure::new));
    private static final int RANDOM_SALT = 10387313;

    public EndCityStructure(HolderSet<Biome> holderSet, Map<MobCategory, StructureSpawnOverride> map, GenerationStep.Decoration decoration, boolean bl) {
        super(holderSet, map, decoration, bl);
    }

    private static int getYPositionForFeature(ChunkPos chunkPos, ChunkGenerator chunkGenerator, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
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
        int m = chunkGenerator.getFirstOccupiedHeight(k, l, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState);
        int n = chunkGenerator.getFirstOccupiedHeight(k, l + j, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState);
        int o = chunkGenerator.getFirstOccupiedHeight(k + i, l, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState);
        int p = chunkGenerator.getFirstOccupiedHeight(k + i, l + j, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState);
        return Math.min(Math.min(m, n), Math.min(o, p));
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        int i = EndCityStructure.getYPositionForFeature(generationContext.chunkPos(), generationContext.chunkGenerator(), generationContext.heightAccessor(), generationContext.randomState());
        if (i < 60) {
            return Optional.empty();
        }
        BlockPos blockPos = generationContext.chunkPos().getMiddleBlockPosition(i);
        return Optional.of(new Structure.GenerationStub(blockPos, structurePiecesBuilder -> this.generatePieces((StructurePiecesBuilder)structurePiecesBuilder, blockPos, generationContext)));
    }

    private void generatePieces(StructurePiecesBuilder structurePiecesBuilder, BlockPos blockPos, Structure.GenerationContext generationContext) {
        Rotation rotation = Rotation.getRandom(generationContext.random());
        ArrayList<StructurePiece> list = Lists.newArrayList();
        EndCityPieces.startHouseTower(generationContext.structureTemplateManager(), blockPos, rotation, list, generationContext.random());
        list.forEach(structurePiecesBuilder::addPiece);
    }

    @Override
    public StructureType<?> type() {
        return StructureType.END_CITY;
    }
}

