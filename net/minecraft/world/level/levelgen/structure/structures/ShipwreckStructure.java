/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.ShipwreckPieces;

public class ShipwreckStructure
extends Structure {
    public static final Codec<ShipwreckStructure> CODEC = RecordCodecBuilder.create(instance -> ShipwreckStructure.codec(instance).and(((MapCodec)Codec.BOOL.fieldOf("is_beached")).forGetter(shipwreckStructure -> shipwreckStructure.isBeached)).apply((Applicative<ShipwreckStructure, ?>)instance, ShipwreckStructure::new));
    public final boolean isBeached;

    public ShipwreckStructure(HolderSet<Biome> holderSet, Map<MobCategory, StructureSpawnOverride> map, GenerationStep.Decoration decoration, boolean bl, boolean bl2) {
        super(holderSet, map, decoration, bl);
        this.isBeached = bl2;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        Heightmap.Types types = this.isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG;
        return ShipwreckStructure.onTopOfChunkCenter(generationContext, types, structurePiecesBuilder -> this.generatePieces((StructurePiecesBuilder)structurePiecesBuilder, generationContext));
    }

    private void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
        Rotation rotation = Rotation.getRandom(generationContext.random());
        BlockPos blockPos = new BlockPos(generationContext.chunkPos().getMinBlockX(), 90, generationContext.chunkPos().getMinBlockZ());
        ShipwreckPieces.addPieces(generationContext.structureTemplateManager(), blockPos, rotation, structurePiecesBuilder, generationContext.random(), this.isBeached);
    }

    @Override
    public StructureType<?> type() {
        return StructureType.SHIPWRECK;
    }
}

