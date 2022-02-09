/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public interface StructurePlacementType<SP extends StructurePlacement> {
    public static final StructurePlacementType<RandomSpreadStructurePlacement> RANDOM_SPREAD = StructurePlacementType.register("random_spread", RandomSpreadStructurePlacement.CODEC);
    public static final StructurePlacementType<ConcentricRingsStructurePlacement> CONCENTRIC_RINGS = StructurePlacementType.register("concentric_rings", ConcentricRingsStructurePlacement.CODEC);

    public Codec<SP> codec();

    private static <SP extends StructurePlacement> StructurePlacementType<SP> register(String string, Codec<SP> codec) {
        return Registry.register(Registry.STRUCTURE_PLACEMENT_TYPE, string, () -> codec);
    }
}

