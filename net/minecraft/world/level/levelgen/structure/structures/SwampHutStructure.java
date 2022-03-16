/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.SwampHutPiece;

public class SwampHutStructure
extends Structure {
    public static final Codec<SwampHutStructure> CODEC = RecordCodecBuilder.create(instance -> SwampHutStructure.codec(instance).apply(instance, SwampHutStructure::new));

    public SwampHutStructure(HolderSet<Biome> holderSet, Map<MobCategory, StructureSpawnOverride> map, GenerationStep.Decoration decoration, boolean bl) {
        super(holderSet, map, decoration, bl);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        return SwampHutStructure.onTopOfChunkCenter(generationContext, Heightmap.Types.WORLD_SURFACE_WG, structurePiecesBuilder -> SwampHutStructure.generatePieces(structurePiecesBuilder, generationContext));
    }

    private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
        structurePiecesBuilder.addPiece(new SwampHutPiece(generationContext.random(), generationContext.chunkPos().getMinBlockX(), generationContext.chunkPos().getMinBlockZ()));
    }

    @Override
    public StructureType<?> type() {
        return StructureType.SWAMP_HUT;
    }
}

