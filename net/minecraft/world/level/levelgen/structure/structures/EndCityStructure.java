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
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.EndCityPieces;

public class EndCityStructure
extends Structure {
    public static final Codec<EndCityStructure> CODEC = RecordCodecBuilder.create(instance -> EndCityStructure.codec(instance).apply(instance, EndCityStructure::new));

    public EndCityStructure(HolderSet<Biome> holderSet, Map<MobCategory, StructureSpawnOverride> map, GenerationStep.Decoration decoration, boolean bl) {
        super(holderSet, map, decoration, bl);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        Rotation rotation = Rotation.getRandom(generationContext.random());
        BlockPos blockPos = this.getLowestYIn5by5BoxOffset7Blocks(generationContext, rotation);
        if (blockPos.getY() < 60) {
            return Optional.empty();
        }
        return Optional.of(new Structure.GenerationStub(blockPos, structurePiecesBuilder -> this.generatePieces((StructurePiecesBuilder)structurePiecesBuilder, blockPos, rotation, generationContext)));
    }

    private void generatePieces(StructurePiecesBuilder structurePiecesBuilder, BlockPos blockPos, Rotation rotation, Structure.GenerationContext generationContext) {
        ArrayList<StructurePiece> list = Lists.newArrayList();
        EndCityPieces.startHouseTower(generationContext.structureTemplateManager(), blockPos, rotation, list, generationContext.random());
        list.forEach(structurePiecesBuilder::addPiece);
    }

    @Override
    public StructureType<?> type() {
        return StructureType.END_CITY;
    }
}

