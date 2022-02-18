/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

public interface BuiltinStructures {
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> PILLAGER_OUTPOST = BuiltinStructures.createKey("pillager_outpost");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> MINESHAFT = BuiltinStructures.createKey("mineshaft");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> MINESHAFT_MESA = BuiltinStructures.createKey("mineshaft_mesa");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> WOODLAND_MANSION = BuiltinStructures.createKey("mansion");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> JUNGLE_TEMPLE = BuiltinStructures.createKey("jungle_pyramid");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> DESERT_PYRAMID = BuiltinStructures.createKey("desert_pyramid");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> IGLOO = BuiltinStructures.createKey("igloo");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> SHIPWRECK = BuiltinStructures.createKey("shipwreck");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> SHIPWRECK_BEACHED = BuiltinStructures.createKey("shipwreck_beached");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> SWAMP_HUT = BuiltinStructures.createKey("swamp_hut");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> STRONGHOLD = BuiltinStructures.createKey("stronghold");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> OCEAN_MONUMENT = BuiltinStructures.createKey("monument");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> OCEAN_RUIN_COLD = BuiltinStructures.createKey("ocean_ruin_cold");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> OCEAN_RUIN_WARM = BuiltinStructures.createKey("ocean_ruin_warm");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> FORTRESS = BuiltinStructures.createKey("fortress");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> NETHER_FOSSIL = BuiltinStructures.createKey("nether_fossil");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> END_CITY = BuiltinStructures.createKey("end_city");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> BURIED_TREASURE = BuiltinStructures.createKey("buried_treasure");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> BASTION_REMNANT = BuiltinStructures.createKey("bastion_remnant");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> VILLAGE_PLAINS = BuiltinStructures.createKey("village_plains");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> VILLAGE_DESERT = BuiltinStructures.createKey("village_desert");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> VILLAGE_SAVANNA = BuiltinStructures.createKey("village_savanna");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> VILLAGE_SNOWY = BuiltinStructures.createKey("village_snowy");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> VILLAGE_TAIGA = BuiltinStructures.createKey("village_taiga");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> RUINED_PORTAL_STANDARD = BuiltinStructures.createKey("ruined_portal");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> RUINED_PORTAL_DESERT = BuiltinStructures.createKey("ruined_portal_desert");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> RUINED_PORTAL_JUNGLE = BuiltinStructures.createKey("ruined_portal_jungle");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> RUINED_PORTAL_SWAMP = BuiltinStructures.createKey("ruined_portal_swamp");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> RUINED_PORTAL_MOUNTAIN = BuiltinStructures.createKey("ruined_portal_mountain");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> RUINED_PORTAL_OCEAN = BuiltinStructures.createKey("ruined_portal_ocean");
    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> RUINED_PORTAL_NETHER = BuiltinStructures.createKey("ruined_portal_nether");

    private static ResourceKey<ConfiguredStructureFeature<?, ?>> createKey(String string) {
        return ResourceKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, new ResourceLocation(string));
    }
}

