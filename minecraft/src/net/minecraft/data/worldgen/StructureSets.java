package net.minecraft.data.worldgen;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public interface StructureSets {
	Holder<StructureSet> VILLAGES = register(
		BuiltinStructureSets.VILLAGES,
		new StructureSet(
			List.of(
				StructureSet.entry(Structures.VILLAGE_PLAINS),
				StructureSet.entry(Structures.VILLAGE_DESERT),
				StructureSet.entry(Structures.VILLAGE_SAVANNA),
				StructureSet.entry(Structures.VILLAGE_SNOWY),
				StructureSet.entry(Structures.VILLAGE_TAIGA)
			),
			new RandomSpreadStructurePlacement(34, 8, RandomSpreadType.LINEAR, 10387312)
		)
	);
	Holder<StructureSet> DESERT_PYRAMIDS = register(
		BuiltinStructureSets.DESERT_PYRAMIDS, Structures.DESERT_PYRAMID, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357617)
	);
	Holder<StructureSet> IGLOOS = register(
		BuiltinStructureSets.IGLOOS, Structures.IGLOO, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357618)
	);
	Holder<StructureSet> JUNGLE_TEMPLES = register(
		BuiltinStructureSets.JUNGLE_TEMPLES, Structures.JUNGLE_TEMPLE, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357619)
	);
	Holder<StructureSet> SWAMP_HUTS = register(
		BuiltinStructureSets.SWAMP_HUTS, Structures.SWAMP_HUT, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357620)
	);
	Holder<StructureSet> PILLAGER_OUTPOSTS = register(
		BuiltinStructureSets.PILLAGER_OUTPOSTS,
		Structures.PILLAGER_OUTPOST,
		new RandomSpreadStructurePlacement(
			Vec3i.ZERO,
			StructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_1,
			0.2F,
			165745296,
			Optional.of(new StructurePlacement.ExclusionZone(VILLAGES, 10)),
			32,
			8,
			RandomSpreadType.LINEAR
		)
	);
	Holder<StructureSet> OCEAN_MONUMENTS = register(
		BuiltinStructureSets.OCEAN_MONUMENTS, Structures.OCEAN_MONUMENT, new RandomSpreadStructurePlacement(32, 5, RandomSpreadType.TRIANGULAR, 10387313)
	);
	Holder<StructureSet> WOODLAND_MANSIONS = register(
		BuiltinStructureSets.WOODLAND_MANSIONS, Structures.WOODLAND_MANSION, new RandomSpreadStructurePlacement(80, 20, RandomSpreadType.TRIANGULAR, 10387319)
	);
	Holder<StructureSet> BURIED_TREASURES = register(
		BuiltinStructureSets.BURIED_TREASURES,
		Structures.BURIED_TREASURE,
		new RandomSpreadStructurePlacement(
			new Vec3i(9, 0, 9), StructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_2, 0.01F, 0, Optional.empty(), 1, 0, RandomSpreadType.LINEAR
		)
	);
	Holder<StructureSet> MINESHAFTS = register(
		BuiltinStructureSets.MINESHAFTS,
		new StructureSet(
			List.of(StructureSet.entry(Structures.MINESHAFT), StructureSet.entry(Structures.MINESHAFT_MESA)),
			new RandomSpreadStructurePlacement(
				Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_3, 0.004F, 0, Optional.empty(), 1, 0, RandomSpreadType.LINEAR
			)
		)
	);
	Holder<StructureSet> RUINED_PORTALS = register(
		BuiltinStructureSets.RUINED_PORTALS,
		new StructureSet(
			List.of(
				StructureSet.entry(Structures.RUINED_PORTAL_STANDARD),
				StructureSet.entry(Structures.RUINED_PORTAL_DESERT),
				StructureSet.entry(Structures.RUINED_PORTAL_JUNGLE),
				StructureSet.entry(Structures.RUINED_PORTAL_SWAMP),
				StructureSet.entry(Structures.RUINED_PORTAL_MOUNTAIN),
				StructureSet.entry(Structures.RUINED_PORTAL_OCEAN),
				StructureSet.entry(Structures.RUINED_PORTAL_NETHER)
			),
			new RandomSpreadStructurePlacement(40, 15, RandomSpreadType.LINEAR, 34222645)
		)
	);
	Holder<StructureSet> SHIPWRECKS = register(
		BuiltinStructureSets.SHIPWRECKS,
		new StructureSet(
			List.of(StructureSet.entry(Structures.SHIPWRECK), StructureSet.entry(Structures.SHIPWRECK_BEACHED)),
			new RandomSpreadStructurePlacement(24, 4, RandomSpreadType.LINEAR, 165745295)
		)
	);
	Holder<StructureSet> OCEAN_RUINS = register(
		BuiltinStructureSets.OCEAN_RUINS,
		new StructureSet(
			List.of(StructureSet.entry(Structures.OCEAN_RUIN_COLD), StructureSet.entry(Structures.OCEAN_RUIN_WARM)),
			new RandomSpreadStructurePlacement(20, 8, RandomSpreadType.LINEAR, 14357621)
		)
	);
	Holder<StructureSet> NETHER_COMPLEXES = register(
		BuiltinStructureSets.NETHER_COMPLEXES,
		new StructureSet(
			List.of(StructureSet.entry(Structures.FORTRESS, 2), StructureSet.entry(Structures.BASTION_REMNANT, 3)),
			new RandomSpreadStructurePlacement(27, 4, RandomSpreadType.LINEAR, 30084232)
		)
	);
	Holder<StructureSet> NETHER_FOSSILS = register(
		BuiltinStructureSets.NETHER_FOSSILS, Structures.NETHER_FOSSIL, new RandomSpreadStructurePlacement(2, 1, RandomSpreadType.LINEAR, 14357921)
	);
	Holder<StructureSet> END_CITIES = register(
		BuiltinStructureSets.END_CITIES, Structures.END_CITY, new RandomSpreadStructurePlacement(20, 11, RandomSpreadType.TRIANGULAR, 10387313)
	);
	Holder<StructureSet> STRONGHOLDS = register(
		BuiltinStructureSets.STRONGHOLDS,
		Structures.STRONGHOLD,
		new ConcentricRingsStructurePlacement(32, 3, 128, BuiltinRegistries.BIOME.getOrCreateTag(BiomeTags.STRONGHOLD_BIASED_TO))
	);

	static Holder<StructureSet> bootstrap() {
		return (Holder<StructureSet>)BuiltinRegistries.STRUCTURE_SETS.holders().iterator().next();
	}

	static Holder<StructureSet> register(ResourceKey<StructureSet> resourceKey, StructureSet structureSet) {
		return BuiltinRegistries.register(BuiltinRegistries.STRUCTURE_SETS, resourceKey, structureSet);
	}

	static Holder<StructureSet> register(ResourceKey<StructureSet> resourceKey, Holder<Structure> holder, StructurePlacement structurePlacement) {
		return register(resourceKey, new StructureSet(holder, structurePlacement));
	}
}
