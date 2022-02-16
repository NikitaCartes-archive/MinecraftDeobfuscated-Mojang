/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class BiomeTags {
    public static final TagKey<Biome> IS_DEEP_OCEAN = BiomeTags.create("is_deep_ocean");
    public static final TagKey<Biome> IS_OCEAN = BiomeTags.create("is_ocean");
    public static final TagKey<Biome> IS_BEACH = BiomeTags.create("is_beach");
    public static final TagKey<Biome> IS_RIVER = BiomeTags.create("is_river");
    public static final TagKey<Biome> IS_MOUNTAIN = BiomeTags.create("is_mountain");
    public static final TagKey<Biome> IS_BADLANDS = BiomeTags.create("is_badlands");
    public static final TagKey<Biome> IS_HILL = BiomeTags.create("is_hill");
    public static final TagKey<Biome> IS_TAIGA = BiomeTags.create("is_taiga");
    public static final TagKey<Biome> IS_JUNGLE = BiomeTags.create("is_jungle");
    public static final TagKey<Biome> IS_FOREST = BiomeTags.create("is_forest");
    public static final TagKey<Biome> IS_NETHER = BiomeTags.create("is_nether");
    public static final TagKey<Biome> HAS_BURIED_TREASURE = BiomeTags.create("has_structure/buried_treasure");
    public static final TagKey<Biome> HAS_DESERT_PYRAMID = BiomeTags.create("has_structure/desert_pyramid");
    public static final TagKey<Biome> HAS_IGLOO = BiomeTags.create("has_structure/igloo");
    public static final TagKey<Biome> HAS_JUNGLE_TEMPLE = BiomeTags.create("has_structure/jungle_temple");
    public static final TagKey<Biome> HAS_MINESHAFT = BiomeTags.create("has_structure/mineshaft");
    public static final TagKey<Biome> HAS_MINESHAFT_MESA = BiomeTags.create("has_structure/mineshaft_mesa");
    public static final TagKey<Biome> HAS_OCEAN_MONUMENT = BiomeTags.create("has_structure/ocean_monument");
    public static final TagKey<Biome> HAS_OCEAN_RUIN_COLD = BiomeTags.create("has_structure/ocean_ruin_cold");
    public static final TagKey<Biome> HAS_OCEAN_RUIN_WARM = BiomeTags.create("has_structure/ocean_ruin_warm");
    public static final TagKey<Biome> HAS_PILLAGER_OUTPOST = BiomeTags.create("has_structure/pillager_outpost");
    public static final TagKey<Biome> HAS_RUINED_PORTAL_DESERT = BiomeTags.create("has_structure/ruined_portal_desert");
    public static final TagKey<Biome> HAS_RUINED_PORTAL_JUNGLE = BiomeTags.create("has_structure/ruined_portal_jungle");
    public static final TagKey<Biome> HAS_RUINED_PORTAL_OCEAN = BiomeTags.create("has_structure/ruined_portal_ocean");
    public static final TagKey<Biome> HAS_RUINED_PORTAL_SWAMP = BiomeTags.create("has_structure/ruined_portal_swamp");
    public static final TagKey<Biome> HAS_RUINED_PORTAL_MOUNTAIN = BiomeTags.create("has_structure/ruined_portal_mountain");
    public static final TagKey<Biome> HAS_RUINED_PORTAL_STANDARD = BiomeTags.create("has_structure/ruined_portal_standard");
    public static final TagKey<Biome> HAS_SHIPWRECK_BEACHED = BiomeTags.create("has_structure/shipwreck_beached");
    public static final TagKey<Biome> HAS_SHIPWRECK = BiomeTags.create("has_structure/shipwreck");
    public static final TagKey<Biome> HAS_SWAMP_HUT = BiomeTags.create("has_structure/swamp_hut");
    public static final TagKey<Biome> HAS_VILLAGE_DESERT = BiomeTags.create("has_structure/village_desert");
    public static final TagKey<Biome> HAS_VILLAGE_PLAINS = BiomeTags.create("has_structure/village_plains");
    public static final TagKey<Biome> HAS_VILLAGE_SAVANNA = BiomeTags.create("has_structure/village_savanna");
    public static final TagKey<Biome> HAS_VILLAGE_SNOWY = BiomeTags.create("has_structure/village_snowy");
    public static final TagKey<Biome> HAS_VILLAGE_TAIGA = BiomeTags.create("has_structure/village_taiga");
    public static final TagKey<Biome> HAS_WOODLAND_MANSION = BiomeTags.create("has_structure/woodland_mansion");
    public static final TagKey<Biome> HAS_STRONGHOLD = BiomeTags.create("has_structure/stronghold");
    public static final TagKey<Biome> HAS_NETHER_FORTRESS = BiomeTags.create("has_structure/nether_fortress");
    public static final TagKey<Biome> HAS_NETHER_FOSSIL = BiomeTags.create("has_structure/nether_fossil");
    public static final TagKey<Biome> HAS_BASTION_REMNANT = BiomeTags.create("has_structure/bastion_remnant");
    public static final TagKey<Biome> HAS_RUINED_PORTAL_NETHER = BiomeTags.create("has_structure/ruined_portal_nether");
    public static final TagKey<Biome> HAS_END_CITY = BiomeTags.create("has_structure/end_city");

    private BiomeTags() {
    }

    private static TagKey<Biome> create(String string) {
        return TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(string));
    }
}

