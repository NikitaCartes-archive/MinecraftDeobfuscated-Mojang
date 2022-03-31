/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.structures.DesertPyramidPiece;

public class DesertPyramidStructure
extends SinglePieceStructure {
    public static final Codec<DesertPyramidStructure> CODEC = DesertPyramidStructure.simpleCodec(DesertPyramidStructure::new);

    public DesertPyramidStructure(Structure.StructureSettings structureSettings) {
        super(DesertPyramidPiece::new, 21, 21, structureSettings);
    }

    @Override
    public StructureType<?> type() {
        return StructureType.DESERT_PYRAMID;
    }
}

