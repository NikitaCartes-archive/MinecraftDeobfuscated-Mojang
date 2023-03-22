package net.minecraft.world.level.levelgen.structure;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public interface BuiltinStructureSets {
	ResourceKey<StructureSet> VILLAGES = register("villages");
	ResourceKey<StructureSet> DESERT_PYRAMIDS = register("desert_pyramids");
	ResourceKey<StructureSet> IGLOOS = register("igloos");
	ResourceKey<StructureSet> JUNGLE_TEMPLES = register("jungle_temples");
	ResourceKey<StructureSet> SWAMP_HUTS = register("swamp_huts");
	ResourceKey<StructureSet> PILLAGER_OUTPOSTS = register("pillager_outposts");
	ResourceKey<StructureSet> OCEAN_MONUMENTS = register("ocean_monuments");
	ResourceKey<StructureSet> WOODLAND_MANSIONS = register("woodland_mansions");
	ResourceKey<StructureSet> BURIED_TREASURES = register("buried_treasures");
	ResourceKey<StructureSet> MINESHAFTS = register("mineshafts");
	ResourceKey<StructureSet> RUINED_PORTALS = register("ruined_portals");
	ResourceKey<StructureSet> SHIPWRECKS = register("shipwrecks");
	ResourceKey<StructureSet> OCEAN_RUINS = register("ocean_ruins");
	ResourceKey<StructureSet> NETHER_COMPLEXES = register("nether_complexes");
	ResourceKey<StructureSet> NETHER_FOSSILS = register("nether_fossils");
	ResourceKey<StructureSet> END_CITIES = register("end_cities");
	ResourceKey<StructureSet> ANCIENT_CITIES = register("ancient_cities");
	ResourceKey<StructureSet> STRONGHOLDS = register("strongholds");
	ResourceKey<StructureSet> TRAIL_RUINS = register("trail_ruins");

	private static ResourceKey<StructureSet> register(String string) {
		return ResourceKey.create(Registries.STRUCTURE_SET, new ResourceLocation(string));
	}
}
