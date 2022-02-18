/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public interface StructureSets {
    public static final Holder<StructureSet> VILLAGES = StructureSets.register(BuiltinStructureSets.VILLAGES, new StructureSet(List.of(StructureSet.entry(StructureFeatures.VILLAGE_PLAINS), StructureSet.entry(StructureFeatures.VILLAGE_DESERT), StructureSet.entry(StructureFeatures.VILLAGE_SAVANNA), StructureSet.entry(StructureFeatures.VILLAGE_SNOWY), StructureSet.entry(StructureFeatures.VILLAGE_TAIGA)), (StructurePlacement)new RandomSpreadStructurePlacement(34, 8, RandomSpreadType.LINEAR, 10387312)));
    public static final Holder<StructureSet> DESERT_PYRAMIDS = StructureSets.register(BuiltinStructureSets.DESERT_PYRAMIDS, StructureFeatures.DESERT_PYRAMID, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357617));
    public static final Holder<StructureSet> IGLOOS = StructureSets.register(BuiltinStructureSets.IGLOOS, StructureFeatures.IGLOO, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357618));
    public static final Holder<StructureSet> JUNGLE_TEMPLES = StructureSets.register(BuiltinStructureSets.JUNGLE_TEMPLES, StructureFeatures.JUNGLE_TEMPLE, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357619));
    public static final Holder<StructureSet> SWAMP_HUTS = StructureSets.register(BuiltinStructureSets.SWAMP_HUTS, StructureFeatures.SWAMP_HUT, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357620));
    public static final Holder<StructureSet> PILLAGER_OUTPOSTS = StructureSets.register(BuiltinStructureSets.PILLAGER_OUTPOSTS, StructureFeatures.PILLAGER_OUTPOST, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 165745296));
    public static final Holder<StructureSet> OCEAN_MONUMENTS = StructureSets.register(BuiltinStructureSets.OCEAN_MONUMENTS, StructureFeatures.OCEAN_MONUMENT, new RandomSpreadStructurePlacement(32, 5, RandomSpreadType.TRIANGULAR, 10387313));
    public static final Holder<StructureSet> WOODLAND_MANSIONS = StructureSets.register(BuiltinStructureSets.WOODLAND_MANSIONS, StructureFeatures.WOODLAND_MANSION, new RandomSpreadStructurePlacement(80, 20, RandomSpreadType.TRIANGULAR, 10387319));
    public static final Holder<StructureSet> BURIED_TREASURES = StructureSets.register(BuiltinStructureSets.BURIED_TREASURES, StructureFeatures.BURIED_TREASURE, new RandomSpreadStructurePlacement(1, 0, RandomSpreadType.LINEAR, 0, new Vec3i(9, 0, 9)));
    public static final Holder<StructureSet> MINESHAFTS = StructureSets.register(BuiltinStructureSets.MINESHAFTS, new StructureSet(List.of(StructureSet.entry(StructureFeatures.MINESHAFT), StructureSet.entry(StructureFeatures.MINESHAFT_MESA)), (StructurePlacement)new RandomSpreadStructurePlacement(1, 0, RandomSpreadType.LINEAR, 0)));
    public static final Holder<StructureSet> RUINED_PORTALS = StructureSets.register(BuiltinStructureSets.RUINED_PORTALS, new StructureSet(List.of(StructureSet.entry(StructureFeatures.RUINED_PORTAL_STANDARD), StructureSet.entry(StructureFeatures.RUINED_PORTAL_DESERT), StructureSet.entry(StructureFeatures.RUINED_PORTAL_JUNGLE), StructureSet.entry(StructureFeatures.RUINED_PORTAL_SWAMP), StructureSet.entry(StructureFeatures.RUINED_PORTAL_MOUNTAIN), StructureSet.entry(StructureFeatures.RUINED_PORTAL_OCEAN), StructureSet.entry(StructureFeatures.RUINED_PORTAL_NETHER)), (StructurePlacement)new RandomSpreadStructurePlacement(40, 15, RandomSpreadType.LINEAR, 34222645)));
    public static final Holder<StructureSet> SHIPWRECKS = StructureSets.register(BuiltinStructureSets.SHIPWRECKS, new StructureSet(List.of(StructureSet.entry(StructureFeatures.SHIPWRECK), StructureSet.entry(StructureFeatures.SHIPWRECK_BEACHED)), (StructurePlacement)new RandomSpreadStructurePlacement(24, 4, RandomSpreadType.LINEAR, 165745295)));
    public static final Holder<StructureSet> OCEAN_RUINS = StructureSets.register(BuiltinStructureSets.OCEAN_RUINS, new StructureSet(List.of(StructureSet.entry(StructureFeatures.OCEAN_RUIN_COLD), StructureSet.entry(StructureFeatures.OCEAN_RUIN_WARM)), (StructurePlacement)new RandomSpreadStructurePlacement(20, 8, RandomSpreadType.LINEAR, 14357621)));
    public static final Holder<StructureSet> NETHER_COMPLEXES = StructureSets.register(BuiltinStructureSets.NETHER_COMPLEXES, new StructureSet(List.of(StructureSet.entry(StructureFeatures.FORTRESS, 2), StructureSet.entry(StructureFeatures.BASTION_REMNANT, 3)), (StructurePlacement)new RandomSpreadStructurePlacement(27, 4, RandomSpreadType.LINEAR, 30084232)));
    public static final Holder<StructureSet> NETHER_FOSSILS = StructureSets.register(BuiltinStructureSets.NETHER_FOSSILS, StructureFeatures.NETHER_FOSSIL, new RandomSpreadStructurePlacement(2, 1, RandomSpreadType.LINEAR, 14357921));
    public static final Holder<StructureSet> END_CITIES = StructureSets.register(BuiltinStructureSets.END_CITIES, StructureFeatures.END_CITY, new RandomSpreadStructurePlacement(20, 11, RandomSpreadType.TRIANGULAR, 10387313));
    public static final Holder<StructureSet> STRONGHOLDS = StructureSets.register(BuiltinStructureSets.STRONGHOLDS, StructureFeatures.STRONGHOLD, new ConcentricRingsStructurePlacement(32, 3, 128));

    public static Holder<StructureSet> bootstrap() {
        return (Holder)BuiltinRegistries.STRUCTURE_SETS.holders().iterator().next();
    }

    public static Holder<StructureSet> register(ResourceKey<StructureSet> resourceKey, StructureSet structureSet) {
        return BuiltinRegistries.register(BuiltinRegistries.STRUCTURE_SETS, resourceKey, structureSet);
    }

    public static Holder<StructureSet> register(ResourceKey<StructureSet> resourceKey, Holder<ConfiguredStructureFeature<?, ?>> holder, StructurePlacement structurePlacement) {
        return StructureSets.register(resourceKey, new StructureSet(holder, structurePlacement));
    }
}

