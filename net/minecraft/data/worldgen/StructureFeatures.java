/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.BastionPieces;
import net.minecraft.data.worldgen.DesertVillagePools;
import net.minecraft.data.worldgen.PillagerOutpostPools;
import net.minecraft.data.worldgen.PlainVillagePools;
import net.minecraft.data.worldgen.SavannaVillagePools;
import net.minecraft.data.worldgen.SnowyVillagePools;
import net.minecraft.data.worldgen.TaigaVillagePools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.RuinedPortalFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;

public class StructureFeatures {
    private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> PILLAGER_OUTPOST = StructureFeatures.register("pillager_outpost", StructureFeature.PILLAGER_OUTPOST.configured(new JigsawConfiguration(() -> PillagerOutpostPools.START, 7)));
    private static final ConfiguredStructureFeature<MineshaftConfiguration, ? extends StructureFeature<MineshaftConfiguration>> MINESHAFT = StructureFeatures.register("mineshaft", StructureFeature.MINESHAFT.configured(new MineshaftConfiguration(0.004f, MineshaftFeature.Type.NORMAL)));
    private static final ConfiguredStructureFeature<MineshaftConfiguration, ? extends StructureFeature<MineshaftConfiguration>> MINESHAFT_MESA = StructureFeatures.register("mineshaft_mesa", StructureFeature.MINESHAFT.configured(new MineshaftConfiguration(0.004f, MineshaftFeature.Type.MESA)));
    private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> WOODLAND_MANSION = StructureFeatures.register("mansion", StructureFeature.WOODLAND_MANSION.configured(NoneFeatureConfiguration.INSTANCE));
    private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> JUNGLE_TEMPLE = StructureFeatures.register("jungle_pyramid", StructureFeature.JUNGLE_TEMPLE.configured(NoneFeatureConfiguration.INSTANCE));
    private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> DESERT_PYRAMID = StructureFeatures.register("desert_pyramid", StructureFeature.DESERT_PYRAMID.configured(NoneFeatureConfiguration.INSTANCE));
    private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> IGLOO = StructureFeatures.register("igloo", StructureFeature.IGLOO.configured(NoneFeatureConfiguration.INSTANCE));
    private static final ConfiguredStructureFeature<ShipwreckConfiguration, ? extends StructureFeature<ShipwreckConfiguration>> SHIPWRECK = StructureFeatures.register("shipwreck", StructureFeature.SHIPWRECK.configured(new ShipwreckConfiguration(false)));
    private static final ConfiguredStructureFeature<ShipwreckConfiguration, ? extends StructureFeature<ShipwreckConfiguration>> SHIPWRECK_BEACHED = StructureFeatures.register("shipwreck_beached", StructureFeature.SHIPWRECK.configured(new ShipwreckConfiguration(true)));
    private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> SWAMP_HUT = StructureFeatures.register("swamp_hut", StructureFeature.SWAMP_HUT.configured(NoneFeatureConfiguration.INSTANCE));
    public static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> STRONGHOLD = StructureFeatures.register("stronghold", StructureFeature.STRONGHOLD.configured(NoneFeatureConfiguration.INSTANCE));
    private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> OCEAN_MONUMENT = StructureFeatures.register("monument", StructureFeature.OCEAN_MONUMENT.configured(NoneFeatureConfiguration.INSTANCE));
    private static final ConfiguredStructureFeature<OceanRuinConfiguration, ? extends StructureFeature<OceanRuinConfiguration>> OCEAN_RUIN_COLD = StructureFeatures.register("ocean_ruin_cold", StructureFeature.OCEAN_RUIN.configured(new OceanRuinConfiguration(OceanRuinFeature.Type.COLD, 0.3f, 0.9f)));
    private static final ConfiguredStructureFeature<OceanRuinConfiguration, ? extends StructureFeature<OceanRuinConfiguration>> OCEAN_RUIN_WARM = StructureFeatures.register("ocean_ruin_warm", StructureFeature.OCEAN_RUIN.configured(new OceanRuinConfiguration(OceanRuinFeature.Type.WARM, 0.3f, 0.9f)));
    private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> NETHER_BRIDGE = StructureFeatures.register("fortress", StructureFeature.NETHER_BRIDGE.configured(NoneFeatureConfiguration.INSTANCE));
    private static final ConfiguredStructureFeature<RangeDecoratorConfiguration, ? extends StructureFeature<RangeDecoratorConfiguration>> NETHER_FOSSIL = StructureFeatures.register("nether_fossil", StructureFeature.NETHER_FOSSIL.configured(new RangeDecoratorConfiguration(UniformHeight.of(VerticalAnchor.absolute(32), VerticalAnchor.belowTop(2)))));
    private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> END_CITY = StructureFeatures.register("end_city", StructureFeature.END_CITY.configured(NoneFeatureConfiguration.INSTANCE));
    private static final ConfiguredStructureFeature<ProbabilityFeatureConfiguration, ? extends StructureFeature<ProbabilityFeatureConfiguration>> BURIED_TREASURE = StructureFeatures.register("buried_treasure", StructureFeature.BURIED_TREASURE.configured(new ProbabilityFeatureConfiguration(0.01f)));
    private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> BASTION_REMNANT = StructureFeatures.register("bastion_remnant", StructureFeature.BASTION_REMNANT.configured(new JigsawConfiguration(() -> BastionPieces.START, 6)));
    private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> VILLAGE_PLAINS = StructureFeatures.register("village_plains", StructureFeature.VILLAGE.configured(new JigsawConfiguration(() -> PlainVillagePools.START, 6)));
    private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> VILLAGE_DESERT = StructureFeatures.register("village_desert", StructureFeature.VILLAGE.configured(new JigsawConfiguration(() -> DesertVillagePools.START, 6)));
    private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> VILLAGE_SAVANNA = StructureFeatures.register("village_savanna", StructureFeature.VILLAGE.configured(new JigsawConfiguration(() -> SavannaVillagePools.START, 6)));
    private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> VILLAGE_SNOWY = StructureFeatures.register("village_snowy", StructureFeature.VILLAGE.configured(new JigsawConfiguration(() -> SnowyVillagePools.START, 6)));
    private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> VILLAGE_TAIGA = StructureFeatures.register("village_taiga", StructureFeature.VILLAGE.configured(new JigsawConfiguration(() -> TaigaVillagePools.START, 6)));
    private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_STANDARD = StructureFeatures.register("ruined_portal", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.STANDARD)));
    private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_DESERT = StructureFeatures.register("ruined_portal_desert", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.DESERT)));
    private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_JUNGLE = StructureFeatures.register("ruined_portal_jungle", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.JUNGLE)));
    private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_SWAMP = StructureFeatures.register("ruined_portal_swamp", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.SWAMP)));
    private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_MOUNTAIN = StructureFeatures.register("ruined_portal_mountain", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.MOUNTAIN)));
    private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_OCEAN = StructureFeatures.register("ruined_portal_ocean", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.OCEAN)));
    private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_NETHER = StructureFeatures.register("ruined_portal_nether", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.NETHER)));

    public static ConfiguredStructureFeature<?, ?> bootstrap() {
        return MINESHAFT;
    }

    private static <FC extends FeatureConfiguration, F extends StructureFeature<FC>> ConfiguredStructureFeature<FC, F> register(String string, ConfiguredStructureFeature<FC, F> configuredStructureFeature) {
        return BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, string, configuredStructureFeature);
    }

    private static void register(BiConsumer<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> biConsumer, ConfiguredStructureFeature<?, ?> configuredStructureFeature, Set<ResourceKey<Biome>> set) {
        set.forEach(resourceKey -> biConsumer.accept(configuredStructureFeature, (ResourceKey<Biome>)resourceKey));
    }

    private static void register(BiConsumer<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> biConsumer, ConfiguredStructureFeature<?, ?> configuredStructureFeature, ResourceKey<Biome> resourceKey) {
        biConsumer.accept(configuredStructureFeature, resourceKey);
    }

    public static void registerStructures(BiConsumer<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> biConsumer) {
        ImmutableCollection set = ((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)ImmutableSet.builder().add(Biomes.DEEP_FROZEN_OCEAN)).add(Biomes.DEEP_COLD_OCEAN)).add(Biomes.DEEP_OCEAN)).add(Biomes.DEEP_LUKEWARM_OCEAN)).build();
        ImmutableCollection set2 = ((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)ImmutableSet.builder().add(Biomes.FROZEN_OCEAN)).add(Biomes.OCEAN)).add(Biomes.COLD_OCEAN)).add(Biomes.LUKEWARM_OCEAN)).add(Biomes.WARM_OCEAN)).addAll((Iterable)set)).build();
        ImmutableCollection set3 = ((ImmutableSet.Builder)((ImmutableSet.Builder)ImmutableSet.builder().add(Biomes.BEACH)).add(Biomes.SNOWY_BEACH)).build();
        ImmutableCollection set4 = ((ImmutableSet.Builder)((ImmutableSet.Builder)ImmutableSet.builder().add(Biomes.RIVER)).add(Biomes.FROZEN_RIVER)).build();
        ImmutableCollection set5 = ((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)ImmutableSet.builder().add(Biomes.MEADOW)).add(Biomes.FROZEN_PEAKS)).add(Biomes.JAGGED_PEAKS)).add(Biomes.STONY_PEAKS)).add(Biomes.SNOWY_SLOPES)).build();
        ImmutableCollection set6 = ((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)ImmutableSet.builder().add(Biomes.BADLANDS)).add(Biomes.ERODED_BADLANDS)).add(Biomes.WOODED_BADLANDS)).build();
        ImmutableCollection set7 = ((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)ImmutableSet.builder().add(Biomes.WINDSWEPT_HILLS)).add(Biomes.WINDSWEPT_FOREST)).add(Biomes.WINDSWEPT_GRAVELLY_HILLS)).build();
        ImmutableCollection set8 = ((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)ImmutableSet.builder().add(Biomes.TAIGA)).add(Biomes.SNOWY_TAIGA)).add(Biomes.OLD_GROWTH_PINE_TAIGA)).add(Biomes.OLD_GROWTH_SPRUCE_TAIGA)).build();
        ImmutableCollection set9 = ((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)ImmutableSet.builder().add(Biomes.BAMBOO_JUNGLE)).add(Biomes.JUNGLE)).add(Biomes.SPARSE_JUNGLE)).build();
        ImmutableCollection set10 = ((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)ImmutableSet.builder().add(Biomes.FOREST)).add(Biomes.FLOWER_FOREST)).add(Biomes.BIRCH_FOREST)).add(Biomes.OLD_GROWTH_BIRCH_FOREST)).add(Biomes.DARK_FOREST)).add(Biomes.GROVE)).build();
        ImmutableCollection set11 = ((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)ImmutableSet.builder().add(Biomes.NETHER_WASTES)).add(Biomes.BASALT_DELTAS)).add(Biomes.SOUL_SAND_VALLEY)).add(Biomes.CRIMSON_FOREST)).add(Biomes.WARPED_FOREST)).build();
        StructureFeatures.register(biConsumer, BURIED_TREASURE, (Set<ResourceKey<Biome>>)((Object)set3));
        StructureFeatures.register(biConsumer, DESERT_PYRAMID, Biomes.DESERT);
        StructureFeatures.register(biConsumer, IGLOO, Biomes.SNOWY_TAIGA);
        StructureFeatures.register(biConsumer, IGLOO, Biomes.SNOWY_PLAINS);
        StructureFeatures.register(biConsumer, IGLOO, Biomes.SNOWY_SLOPES);
        StructureFeatures.register(biConsumer, JUNGLE_TEMPLE, Biomes.BAMBOO_JUNGLE);
        StructureFeatures.register(biConsumer, JUNGLE_TEMPLE, Biomes.JUNGLE);
        StructureFeatures.register(biConsumer, MINESHAFT, (Set<ResourceKey<Biome>>)((Object)set2));
        StructureFeatures.register(biConsumer, MINESHAFT, (Set<ResourceKey<Biome>>)((Object)set4));
        StructureFeatures.register(biConsumer, MINESHAFT, (Set<ResourceKey<Biome>>)((Object)set3));
        StructureFeatures.register(biConsumer, MINESHAFT, Biomes.STONY_SHORE);
        StructureFeatures.register(biConsumer, MINESHAFT, (Set<ResourceKey<Biome>>)((Object)set5));
        StructureFeatures.register(biConsumer, MINESHAFT, (Set<ResourceKey<Biome>>)((Object)set7));
        StructureFeatures.register(biConsumer, MINESHAFT, (Set<ResourceKey<Biome>>)((Object)set8));
        StructureFeatures.register(biConsumer, MINESHAFT, (Set<ResourceKey<Biome>>)((Object)set9));
        StructureFeatures.register(biConsumer, MINESHAFT, (Set<ResourceKey<Biome>>)((Object)set10));
        StructureFeatures.register(biConsumer, MINESHAFT, Biomes.MUSHROOM_FIELDS);
        StructureFeatures.register(biConsumer, MINESHAFT, Biomes.ICE_SPIKES);
        StructureFeatures.register(biConsumer, MINESHAFT, Biomes.WINDSWEPT_SAVANNA);
        StructureFeatures.register(biConsumer, MINESHAFT, Biomes.DESERT);
        StructureFeatures.register(biConsumer, MINESHAFT, Biomes.SAVANNA);
        StructureFeatures.register(biConsumer, MINESHAFT, Biomes.SNOWY_PLAINS);
        StructureFeatures.register(biConsumer, MINESHAFT, Biomes.PLAINS);
        StructureFeatures.register(biConsumer, MINESHAFT, Biomes.SUNFLOWER_PLAINS);
        StructureFeatures.register(biConsumer, MINESHAFT, Biomes.SWAMP);
        StructureFeatures.register(biConsumer, MINESHAFT, Biomes.SAVANNA_PLATEAU);
        StructureFeatures.register(biConsumer, MINESHAFT, Biomes.DRIPSTONE_CAVES);
        StructureFeatures.register(biConsumer, MINESHAFT, Biomes.LUSH_CAVES);
        StructureFeatures.register(biConsumer, MINESHAFT_MESA, (Set<ResourceKey<Biome>>)((Object)set6));
        StructureFeatures.register(biConsumer, OCEAN_MONUMENT, (Set<ResourceKey<Biome>>)((Object)set));
        StructureFeatures.register(biConsumer, OCEAN_RUIN_COLD, Biomes.FROZEN_OCEAN);
        StructureFeatures.register(biConsumer, OCEAN_RUIN_COLD, Biomes.COLD_OCEAN);
        StructureFeatures.register(biConsumer, OCEAN_RUIN_COLD, Biomes.OCEAN);
        StructureFeatures.register(biConsumer, OCEAN_RUIN_COLD, Biomes.DEEP_FROZEN_OCEAN);
        StructureFeatures.register(biConsumer, OCEAN_RUIN_COLD, Biomes.DEEP_COLD_OCEAN);
        StructureFeatures.register(biConsumer, OCEAN_RUIN_COLD, Biomes.DEEP_OCEAN);
        StructureFeatures.register(biConsumer, OCEAN_RUIN_WARM, Biomes.LUKEWARM_OCEAN);
        StructureFeatures.register(biConsumer, OCEAN_RUIN_WARM, Biomes.WARM_OCEAN);
        StructureFeatures.register(biConsumer, OCEAN_RUIN_WARM, Biomes.DEEP_LUKEWARM_OCEAN);
        StructureFeatures.register(biConsumer, PILLAGER_OUTPOST, Biomes.DESERT);
        StructureFeatures.register(biConsumer, PILLAGER_OUTPOST, Biomes.PLAINS);
        StructureFeatures.register(biConsumer, PILLAGER_OUTPOST, Biomes.SAVANNA);
        StructureFeatures.register(biConsumer, PILLAGER_OUTPOST, Biomes.SNOWY_PLAINS);
        StructureFeatures.register(biConsumer, PILLAGER_OUTPOST, Biomes.TAIGA);
        StructureFeatures.register(biConsumer, PILLAGER_OUTPOST, (Set<ResourceKey<Biome>>)((Object)set5));
        StructureFeatures.register(biConsumer, PILLAGER_OUTPOST, Biomes.GROVE);
        StructureFeatures.register(biConsumer, RUINED_PORTAL_DESERT, Biomes.DESERT);
        StructureFeatures.register(biConsumer, RUINED_PORTAL_JUNGLE, (Set<ResourceKey<Biome>>)((Object)set9));
        StructureFeatures.register(biConsumer, RUINED_PORTAL_OCEAN, (Set<ResourceKey<Biome>>)((Object)set2));
        StructureFeatures.register(biConsumer, RUINED_PORTAL_SWAMP, Biomes.SWAMP);
        StructureFeatures.register(biConsumer, RUINED_PORTAL_MOUNTAIN, (Set<ResourceKey<Biome>>)((Object)set6));
        StructureFeatures.register(biConsumer, RUINED_PORTAL_MOUNTAIN, (Set<ResourceKey<Biome>>)((Object)set7));
        StructureFeatures.register(biConsumer, RUINED_PORTAL_MOUNTAIN, Biomes.SAVANNA_PLATEAU);
        StructureFeatures.register(biConsumer, RUINED_PORTAL_MOUNTAIN, Biomes.WINDSWEPT_SAVANNA);
        StructureFeatures.register(biConsumer, RUINED_PORTAL_MOUNTAIN, Biomes.STONY_SHORE);
        StructureFeatures.register(biConsumer, RUINED_PORTAL_MOUNTAIN, (Set<ResourceKey<Biome>>)((Object)set5));
        StructureFeatures.register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.MUSHROOM_FIELDS);
        StructureFeatures.register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.ICE_SPIKES);
        StructureFeatures.register(biConsumer, RUINED_PORTAL_STANDARD, (Set<ResourceKey<Biome>>)((Object)set3));
        StructureFeatures.register(biConsumer, RUINED_PORTAL_STANDARD, (Set<ResourceKey<Biome>>)((Object)set4));
        StructureFeatures.register(biConsumer, RUINED_PORTAL_STANDARD, (Set<ResourceKey<Biome>>)((Object)set8));
        StructureFeatures.register(biConsumer, RUINED_PORTAL_STANDARD, (Set<ResourceKey<Biome>>)((Object)set10));
        StructureFeatures.register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.DRIPSTONE_CAVES);
        StructureFeatures.register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.LUSH_CAVES);
        StructureFeatures.register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.SAVANNA);
        StructureFeatures.register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.SNOWY_PLAINS);
        StructureFeatures.register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.PLAINS);
        StructureFeatures.register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.SUNFLOWER_PLAINS);
        StructureFeatures.register(biConsumer, SHIPWRECK_BEACHED, (Set<ResourceKey<Biome>>)((Object)set3));
        StructureFeatures.register(biConsumer, SHIPWRECK, (Set<ResourceKey<Biome>>)((Object)set2));
        StructureFeatures.register(biConsumer, SWAMP_HUT, Biomes.SWAMP);
        StructureFeatures.register(biConsumer, VILLAGE_DESERT, Biomes.DESERT);
        StructureFeatures.register(biConsumer, VILLAGE_PLAINS, Biomes.PLAINS);
        StructureFeatures.register(biConsumer, VILLAGE_PLAINS, Biomes.MEADOW);
        StructureFeatures.register(biConsumer, VILLAGE_SAVANNA, Biomes.SAVANNA);
        StructureFeatures.register(biConsumer, VILLAGE_SNOWY, Biomes.SNOWY_PLAINS);
        StructureFeatures.register(biConsumer, VILLAGE_TAIGA, Biomes.TAIGA);
        StructureFeatures.register(biConsumer, WOODLAND_MANSION, Biomes.DARK_FOREST);
        StructureFeatures.register(biConsumer, NETHER_BRIDGE, (Set<ResourceKey<Biome>>)((Object)set11));
        StructureFeatures.register(biConsumer, NETHER_FOSSIL, Biomes.SOUL_SAND_VALLEY);
        StructureFeatures.register(biConsumer, BASTION_REMNANT, Biomes.CRIMSON_FOREST);
        StructureFeatures.register(biConsumer, BASTION_REMNANT, Biomes.NETHER_WASTES);
        StructureFeatures.register(biConsumer, BASTION_REMNANT, Biomes.SOUL_SAND_VALLEY);
        StructureFeatures.register(biConsumer, BASTION_REMNANT, Biomes.WARPED_FOREST);
        StructureFeatures.register(biConsumer, RUINED_PORTAL_NETHER, (Set<ResourceKey<Biome>>)((Object)set11));
        StructureFeatures.register(biConsumer, END_CITY, Biomes.END_HIGHLANDS);
        StructureFeatures.register(biConsumer, END_CITY, Biomes.END_MIDLANDS);
    }
}

