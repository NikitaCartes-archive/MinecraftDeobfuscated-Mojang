/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public interface StructureSets {
    public static void bootstrap(BootstapContext<StructureSet> bootstapContext) {
        HolderGetter<Structure> holderGetter = bootstapContext.lookup(Registry.STRUCTURE_REGISTRY);
        HolderGetter<Biome> holderGetter2 = bootstapContext.lookup(Registry.BIOME_REGISTRY);
        Holder.Reference<StructureSet> reference = bootstapContext.register(BuiltinStructureSets.VILLAGES, new StructureSet(List.of(StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.VILLAGE_PLAINS)), StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.VILLAGE_DESERT)), StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.VILLAGE_SAVANNA)), StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.VILLAGE_SNOWY)), StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.VILLAGE_TAIGA))), (StructurePlacement)new RandomSpreadStructurePlacement(34, 8, RandomSpreadType.LINEAR, 10387312)));
        bootstapContext.register(BuiltinStructureSets.DESERT_PYRAMIDS, new StructureSet(holderGetter.getOrThrow(BuiltinStructures.DESERT_PYRAMID), (StructurePlacement)new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357617)));
        bootstapContext.register(BuiltinStructureSets.IGLOOS, new StructureSet(holderGetter.getOrThrow(BuiltinStructures.IGLOO), (StructurePlacement)new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357618)));
        bootstapContext.register(BuiltinStructureSets.JUNGLE_TEMPLES, new StructureSet(holderGetter.getOrThrow(BuiltinStructures.JUNGLE_TEMPLE), (StructurePlacement)new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357619)));
        bootstapContext.register(BuiltinStructureSets.SWAMP_HUTS, new StructureSet(holderGetter.getOrThrow(BuiltinStructures.SWAMP_HUT), (StructurePlacement)new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357620)));
        bootstapContext.register(BuiltinStructureSets.PILLAGER_OUTPOSTS, new StructureSet(holderGetter.getOrThrow(BuiltinStructures.PILLAGER_OUTPOST), (StructurePlacement)new RandomSpreadStructurePlacement(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_1, 0.2f, 165745296, Optional.of(new StructurePlacement.ExclusionZone(reference, 10)), 32, 8, RandomSpreadType.LINEAR)));
        bootstapContext.register(BuiltinStructureSets.ANCIENT_CITIES, new StructureSet(holderGetter.getOrThrow(BuiltinStructures.ANCIENT_CITY), (StructurePlacement)new RandomSpreadStructurePlacement(24, 8, RandomSpreadType.LINEAR, 20083232)));
        bootstapContext.register(BuiltinStructureSets.OCEAN_MONUMENTS, new StructureSet(holderGetter.getOrThrow(BuiltinStructures.OCEAN_MONUMENT), (StructurePlacement)new RandomSpreadStructurePlacement(32, 5, RandomSpreadType.TRIANGULAR, 10387313)));
        bootstapContext.register(BuiltinStructureSets.WOODLAND_MANSIONS, new StructureSet(holderGetter.getOrThrow(BuiltinStructures.WOODLAND_MANSION), (StructurePlacement)new RandomSpreadStructurePlacement(80, 20, RandomSpreadType.TRIANGULAR, 10387319)));
        bootstapContext.register(BuiltinStructureSets.BURIED_TREASURES, new StructureSet(holderGetter.getOrThrow(BuiltinStructures.BURIED_TREASURE), (StructurePlacement)new RandomSpreadStructurePlacement(new Vec3i(9, 0, 9), StructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_2, 0.01f, 0, Optional.empty(), 1, 0, RandomSpreadType.LINEAR)));
        bootstapContext.register(BuiltinStructureSets.MINESHAFTS, new StructureSet(List.of(StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.MINESHAFT)), StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.MINESHAFT_MESA))), (StructurePlacement)new RandomSpreadStructurePlacement(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_3, 0.004f, 0, Optional.empty(), 1, 0, RandomSpreadType.LINEAR)));
        bootstapContext.register(BuiltinStructureSets.RUINED_PORTALS, new StructureSet(List.of(StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.RUINED_PORTAL_STANDARD)), StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.RUINED_PORTAL_DESERT)), StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.RUINED_PORTAL_JUNGLE)), StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.RUINED_PORTAL_SWAMP)), StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.RUINED_PORTAL_MOUNTAIN)), StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.RUINED_PORTAL_OCEAN)), StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.RUINED_PORTAL_NETHER))), (StructurePlacement)new RandomSpreadStructurePlacement(40, 15, RandomSpreadType.LINEAR, 34222645)));
        bootstapContext.register(BuiltinStructureSets.SHIPWRECKS, new StructureSet(List.of(StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.SHIPWRECK)), StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.SHIPWRECK_BEACHED))), (StructurePlacement)new RandomSpreadStructurePlacement(24, 4, RandomSpreadType.LINEAR, 165745295)));
        bootstapContext.register(BuiltinStructureSets.OCEAN_RUINS, new StructureSet(List.of(StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.OCEAN_RUIN_COLD)), StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.OCEAN_RUIN_WARM))), (StructurePlacement)new RandomSpreadStructurePlacement(20, 8, RandomSpreadType.LINEAR, 14357621)));
        bootstapContext.register(BuiltinStructureSets.NETHER_COMPLEXES, new StructureSet(List.of(StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.FORTRESS), 2), StructureSet.entry(holderGetter.getOrThrow(BuiltinStructures.BASTION_REMNANT), 3)), (StructurePlacement)new RandomSpreadStructurePlacement(27, 4, RandomSpreadType.LINEAR, 30084232)));
        bootstapContext.register(BuiltinStructureSets.NETHER_FOSSILS, new StructureSet(holderGetter.getOrThrow(BuiltinStructures.NETHER_FOSSIL), (StructurePlacement)new RandomSpreadStructurePlacement(2, 1, RandomSpreadType.LINEAR, 14357921)));
        bootstapContext.register(BuiltinStructureSets.END_CITIES, new StructureSet(holderGetter.getOrThrow(BuiltinStructures.END_CITY), (StructurePlacement)new RandomSpreadStructurePlacement(20, 11, RandomSpreadType.TRIANGULAR, 10387313)));
        bootstapContext.register(BuiltinStructureSets.STRONGHOLDS, new StructureSet(holderGetter.getOrThrow(BuiltinStructures.STRONGHOLD), (StructurePlacement)new ConcentricRingsStructurePlacement(32, 3, 128, holderGetter2.getOrThrow(BiomeTags.STRONGHOLD_BIASED_TO))));
    }
}

