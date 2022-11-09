/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public interface BuiltinStructureSets {
    public static final ResourceKey<StructureSet> VILLAGES = BuiltinStructureSets.register("villages");
    public static final ResourceKey<StructureSet> DESERT_PYRAMIDS = BuiltinStructureSets.register("desert_pyramids");
    public static final ResourceKey<StructureSet> IGLOOS = BuiltinStructureSets.register("igloos");
    public static final ResourceKey<StructureSet> JUNGLE_TEMPLES = BuiltinStructureSets.register("jungle_temples");
    public static final ResourceKey<StructureSet> SWAMP_HUTS = BuiltinStructureSets.register("swamp_huts");
    public static final ResourceKey<StructureSet> PILLAGER_OUTPOSTS = BuiltinStructureSets.register("pillager_outposts");
    public static final ResourceKey<StructureSet> OCEAN_MONUMENTS = BuiltinStructureSets.register("ocean_monuments");
    public static final ResourceKey<StructureSet> WOODLAND_MANSIONS = BuiltinStructureSets.register("woodland_mansions");
    public static final ResourceKey<StructureSet> BURIED_TREASURES = BuiltinStructureSets.register("buried_treasures");
    public static final ResourceKey<StructureSet> MINESHAFTS = BuiltinStructureSets.register("mineshafts");
    public static final ResourceKey<StructureSet> RUINED_PORTALS = BuiltinStructureSets.register("ruined_portals");
    public static final ResourceKey<StructureSet> SHIPWRECKS = BuiltinStructureSets.register("shipwrecks");
    public static final ResourceKey<StructureSet> OCEAN_RUINS = BuiltinStructureSets.register("ocean_ruins");
    public static final ResourceKey<StructureSet> NETHER_COMPLEXES = BuiltinStructureSets.register("nether_complexes");
    public static final ResourceKey<StructureSet> NETHER_FOSSILS = BuiltinStructureSets.register("nether_fossils");
    public static final ResourceKey<StructureSet> END_CITIES = BuiltinStructureSets.register("end_cities");
    public static final ResourceKey<StructureSet> ANCIENT_CITIES = BuiltinStructureSets.register("ancient_cities");
    public static final ResourceKey<StructureSet> STRONGHOLDS = BuiltinStructureSets.register("strongholds");

    private static ResourceKey<StructureSet> register(String string) {
        return ResourceKey.create(Registries.STRUCTURE_SET, new ResourceLocation(string));
    }
}

