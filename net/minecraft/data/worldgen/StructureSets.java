/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.Structures;
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
    public static final Holder<StructureSet> VILLAGES = StructureSets.register(BuiltinStructureSets.VILLAGES, new StructureSet(List.of(StructureSet.entry(Structures.VILLAGE_PLAINS), StructureSet.entry(Structures.VILLAGE_DESERT), StructureSet.entry(Structures.VILLAGE_SAVANNA), StructureSet.entry(Structures.VILLAGE_SNOWY), StructureSet.entry(Structures.VILLAGE_TAIGA)), (StructurePlacement)new RandomSpreadStructurePlacement(34, 8, RandomSpreadType.LINEAR, 10387312)));
    public static final Holder<StructureSet> DESERT_PYRAMIDS = StructureSets.register(BuiltinStructureSets.DESERT_PYRAMIDS, Structures.DESERT_PYRAMID, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357617));
    public static final Holder<StructureSet> IGLOOS = StructureSets.register(BuiltinStructureSets.IGLOOS, Structures.IGLOO, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357618));
    public static final Holder<StructureSet> JUNGLE_TEMPLES = StructureSets.register(BuiltinStructureSets.JUNGLE_TEMPLES, Structures.JUNGLE_TEMPLE, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357619));
    public static final Holder<StructureSet> SWAMP_HUTS = StructureSets.register(BuiltinStructureSets.SWAMP_HUTS, Structures.SWAMP_HUT, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357620));
    public static final Holder<StructureSet> PILLAGER_OUTPOSTS = StructureSets.register(BuiltinStructureSets.PILLAGER_OUTPOSTS, Structures.PILLAGER_OUTPOST, new RandomSpreadStructurePlacement(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_1, 0.2f, 165745296, Optional.of(new StructurePlacement.ExclusionZone(VILLAGES, 10)), 32, 8, RandomSpreadType.LINEAR));
    public static final Holder<StructureSet> ANCIENT_CITIES = StructureSets.register(BuiltinStructureSets.ANCIENT_CITIES, Structures.ANCIENT_CITY, new RandomSpreadStructurePlacement(24, 8, RandomSpreadType.LINEAR, 20083232));
    public static final Holder<StructureSet> OCEAN_MONUMENTS = StructureSets.register(BuiltinStructureSets.OCEAN_MONUMENTS, Structures.OCEAN_MONUMENT, new RandomSpreadStructurePlacement(32, 5, RandomSpreadType.TRIANGULAR, 10387313));
    public static final Holder<StructureSet> WOODLAND_MANSIONS = StructureSets.register(BuiltinStructureSets.WOODLAND_MANSIONS, Structures.WOODLAND_MANSION, new RandomSpreadStructurePlacement(80, 20, RandomSpreadType.TRIANGULAR, 10387319));
    public static final Holder<StructureSet> BURIED_TREASURES = StructureSets.register(BuiltinStructureSets.BURIED_TREASURES, Structures.BURIED_TREASURE, new RandomSpreadStructurePlacement(new Vec3i(9, 0, 9), StructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_2, 0.01f, 0, Optional.empty(), 1, 0, RandomSpreadType.LINEAR));
    public static final Holder<StructureSet> MINESHAFTS = StructureSets.register(BuiltinStructureSets.MINESHAFTS, new StructureSet(List.of(StructureSet.entry(Structures.MINESHAFT), StructureSet.entry(Structures.MINESHAFT_MESA)), (StructurePlacement)new RandomSpreadStructurePlacement(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_3, 0.004f, 0, Optional.empty(), 1, 0, RandomSpreadType.LINEAR)));
    public static final Holder<StructureSet> RUINED_PORTALS = StructureSets.register(BuiltinStructureSets.RUINED_PORTALS, new StructureSet(List.of(StructureSet.entry(Structures.RUINED_PORTAL_STANDARD), StructureSet.entry(Structures.RUINED_PORTAL_DESERT), StructureSet.entry(Structures.RUINED_PORTAL_JUNGLE), StructureSet.entry(Structures.RUINED_PORTAL_SWAMP), StructureSet.entry(Structures.RUINED_PORTAL_MOUNTAIN), StructureSet.entry(Structures.RUINED_PORTAL_OCEAN), StructureSet.entry(Structures.RUINED_PORTAL_NETHER)), (StructurePlacement)new RandomSpreadStructurePlacement(40, 15, RandomSpreadType.LINEAR, 34222645)));
    public static final Holder<StructureSet> SHIPWRECKS = StructureSets.register(BuiltinStructureSets.SHIPWRECKS, new StructureSet(List.of(StructureSet.entry(Structures.SHIPWRECK), StructureSet.entry(Structures.SHIPWRECK_BEACHED)), (StructurePlacement)new RandomSpreadStructurePlacement(24, 4, RandomSpreadType.LINEAR, 165745295)));
    public static final Holder<StructureSet> OCEAN_RUINS = StructureSets.register(BuiltinStructureSets.OCEAN_RUINS, new StructureSet(List.of(StructureSet.entry(Structures.OCEAN_RUIN_COLD), StructureSet.entry(Structures.OCEAN_RUIN_WARM)), (StructurePlacement)new RandomSpreadStructurePlacement(20, 8, RandomSpreadType.LINEAR, 14357621)));
    public static final Holder<StructureSet> NETHER_COMPLEXES = StructureSets.register(BuiltinStructureSets.NETHER_COMPLEXES, new StructureSet(List.of(StructureSet.entry(Structures.FORTRESS, 2), StructureSet.entry(Structures.BASTION_REMNANT, 3)), (StructurePlacement)new RandomSpreadStructurePlacement(27, 4, RandomSpreadType.LINEAR, 30084232)));
    public static final Holder<StructureSet> NETHER_FOSSILS = StructureSets.register(BuiltinStructureSets.NETHER_FOSSILS, Structures.NETHER_FOSSIL, new RandomSpreadStructurePlacement(2, 1, RandomSpreadType.LINEAR, 14357921));
    public static final Holder<StructureSet> END_CITIES = StructureSets.register(BuiltinStructureSets.END_CITIES, Structures.END_CITY, new RandomSpreadStructurePlacement(20, 11, RandomSpreadType.TRIANGULAR, 10387313));
    public static final Holder<StructureSet> STRONGHOLDS = StructureSets.register(BuiltinStructureSets.STRONGHOLDS, Structures.STRONGHOLD, new ConcentricRingsStructurePlacement(32, 3, 128, BuiltinRegistries.BIOME.getOrCreateTag(BiomeTags.STRONGHOLD_BIASED_TO)));

    public static Holder<StructureSet> bootstrap(Registry<StructureSet> registry) {
        return (Holder)registry.holders().iterator().next();
    }

    public static Holder<StructureSet> register(ResourceKey<StructureSet> resourceKey, StructureSet structureSet) {
        return BuiltinRegistries.register(BuiltinRegistries.STRUCTURE_SETS, resourceKey, structureSet);
    }

    public static Holder<StructureSet> register(ResourceKey<StructureSet> resourceKey, Holder<Structure> holder, StructurePlacement structurePlacement) {
        return StructureSets.register(resourceKey, new StructureSet(holder, structurePlacement));
    }
}

