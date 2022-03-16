/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.BuriedTreasurePieces;

public class BuriedTreasureStructure
extends Structure {
    public static final Codec<BuriedTreasureStructure> CODEC = RecordCodecBuilder.create(instance -> BuriedTreasureStructure.codec(instance).apply(instance, BuriedTreasureStructure::new));

    public BuriedTreasureStructure(HolderSet<Biome> holderSet, Map<MobCategory, StructureSpawnOverride> map, GenerationStep.Decoration decoration, boolean bl) {
        super(holderSet, map, decoration, bl);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        return BuriedTreasureStructure.onTopOfChunkCenter(generationContext, Heightmap.Types.OCEAN_FLOOR_WG, structurePiecesBuilder -> BuriedTreasureStructure.generatePieces(structurePiecesBuilder, generationContext));
    }

    private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
        BlockPos blockPos = new BlockPos(generationContext.chunkPos().getBlockX(9), 90, generationContext.chunkPos().getBlockZ(9));
        structurePiecesBuilder.addPiece(new BuriedTreasurePieces.BuriedTreasurePiece(blockPos));
    }

    @Override
    public StructureType<?> type() {
        return StructureType.BURIED_TREASURE;
    }
}

