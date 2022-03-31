/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.structures.JungleTemplePiece;

public class JungleTempleStructure
extends SinglePieceStructure {
    public static final Codec<JungleTempleStructure> CODEC = JungleTempleStructure.simpleCodec(JungleTempleStructure::new);

    public JungleTempleStructure(Structure.StructureSettings structureSettings) {
        super(JungleTemplePiece::new, 12, 15, structureSettings);
    }

    @Override
    public StructureType<?> type() {
        return StructureType.JUNGLE_TEMPLE;
    }
}

