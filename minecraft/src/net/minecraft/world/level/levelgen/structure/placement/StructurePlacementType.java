package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;

public interface StructurePlacementType<SP extends StructurePlacement> {
	StructurePlacementType<RandomSpreadStructurePlacement> RANDOM_SPREAD = register("random_spread", RandomSpreadStructurePlacement.CODEC);
	StructurePlacementType<ConcentricRingsStructurePlacement> CONCENTRIC_RINGS = register("concentric_rings", ConcentricRingsStructurePlacement.CODEC);

	Codec<SP> codec();

	private static <SP extends StructurePlacement> StructurePlacementType<SP> register(String string, Codec<SP> codec) {
		return Registry.register(Registry.STRUCTURE_PLACEMENT_TYPE, string, () -> codec);
	}
}
