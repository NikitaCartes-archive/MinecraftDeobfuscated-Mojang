package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.data.BuiltinRegistries;
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
	private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> PILLAGER_OUTPOST = register(
		"pillager_outpost", StructureFeature.PILLAGER_OUTPOST.configured(new JigsawConfiguration(() -> PillagerOutpostPools.START, 7))
	);
	private static final ConfiguredStructureFeature<MineshaftConfiguration, ? extends StructureFeature<MineshaftConfiguration>> MINESHAFT = register(
		"mineshaft", StructureFeature.MINESHAFT.configured(new MineshaftConfiguration(0.004F, MineshaftFeature.Type.NORMAL))
	);
	private static final ConfiguredStructureFeature<MineshaftConfiguration, ? extends StructureFeature<MineshaftConfiguration>> MINESHAFT_MESA = register(
		"mineshaft_mesa", StructureFeature.MINESHAFT.configured(new MineshaftConfiguration(0.004F, MineshaftFeature.Type.MESA))
	);
	private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> WOODLAND_MANSION = register(
		"mansion", StructureFeature.WOODLAND_MANSION.configured(NoneFeatureConfiguration.INSTANCE)
	);
	private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> JUNGLE_TEMPLE = register(
		"jungle_pyramid", StructureFeature.JUNGLE_TEMPLE.configured(NoneFeatureConfiguration.INSTANCE)
	);
	private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> DESERT_PYRAMID = register(
		"desert_pyramid", StructureFeature.DESERT_PYRAMID.configured(NoneFeatureConfiguration.INSTANCE)
	);
	private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> IGLOO = register(
		"igloo", StructureFeature.IGLOO.configured(NoneFeatureConfiguration.INSTANCE)
	);
	private static final ConfiguredStructureFeature<ShipwreckConfiguration, ? extends StructureFeature<ShipwreckConfiguration>> SHIPWRECK = register(
		"shipwreck", StructureFeature.SHIPWRECK.configured(new ShipwreckConfiguration(false))
	);
	private static final ConfiguredStructureFeature<ShipwreckConfiguration, ? extends StructureFeature<ShipwreckConfiguration>> SHIPWRECK_BEACHED = register(
		"shipwreck_beached", StructureFeature.SHIPWRECK.configured(new ShipwreckConfiguration(true))
	);
	private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> SWAMP_HUT = register(
		"swamp_hut", StructureFeature.SWAMP_HUT.configured(NoneFeatureConfiguration.INSTANCE)
	);
	public static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> STRONGHOLD = register(
		"stronghold", StructureFeature.STRONGHOLD.configured(NoneFeatureConfiguration.INSTANCE)
	);
	private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> OCEAN_MONUMENT = register(
		"monument", StructureFeature.OCEAN_MONUMENT.configured(NoneFeatureConfiguration.INSTANCE)
	);
	private static final ConfiguredStructureFeature<OceanRuinConfiguration, ? extends StructureFeature<OceanRuinConfiguration>> OCEAN_RUIN_COLD = register(
		"ocean_ruin_cold", StructureFeature.OCEAN_RUIN.configured(new OceanRuinConfiguration(OceanRuinFeature.Type.COLD, 0.3F, 0.9F))
	);
	private static final ConfiguredStructureFeature<OceanRuinConfiguration, ? extends StructureFeature<OceanRuinConfiguration>> OCEAN_RUIN_WARM = register(
		"ocean_ruin_warm", StructureFeature.OCEAN_RUIN.configured(new OceanRuinConfiguration(OceanRuinFeature.Type.WARM, 0.3F, 0.9F))
	);
	private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> NETHER_BRIDGE = register(
		"fortress", StructureFeature.NETHER_BRIDGE.configured(NoneFeatureConfiguration.INSTANCE)
	);
	private static final ConfiguredStructureFeature<RangeDecoratorConfiguration, ? extends StructureFeature<RangeDecoratorConfiguration>> NETHER_FOSSIL = register(
		"nether_fossil",
		StructureFeature.NETHER_FOSSIL.configured(new RangeDecoratorConfiguration(UniformHeight.of(VerticalAnchor.absolute(32), VerticalAnchor.belowTop(2))))
	);
	private static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> END_CITY = register(
		"end_city", StructureFeature.END_CITY.configured(NoneFeatureConfiguration.INSTANCE)
	);
	private static final ConfiguredStructureFeature<ProbabilityFeatureConfiguration, ? extends StructureFeature<ProbabilityFeatureConfiguration>> BURIED_TREASURE = register(
		"buried_treasure", StructureFeature.BURIED_TREASURE.configured(new ProbabilityFeatureConfiguration(0.01F))
	);
	private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> BASTION_REMNANT = register(
		"bastion_remnant", StructureFeature.BASTION_REMNANT.configured(new JigsawConfiguration(() -> BastionPieces.START, 6))
	);
	private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> VILLAGE_PLAINS = register(
		"village_plains", StructureFeature.VILLAGE.configured(new JigsawConfiguration(() -> PlainVillagePools.START, 6))
	);
	private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> VILLAGE_DESERT = register(
		"village_desert", StructureFeature.VILLAGE.configured(new JigsawConfiguration(() -> DesertVillagePools.START, 6))
	);
	private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> VILLAGE_SAVANNA = register(
		"village_savanna", StructureFeature.VILLAGE.configured(new JigsawConfiguration(() -> SavannaVillagePools.START, 6))
	);
	private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> VILLAGE_SNOWY = register(
		"village_snowy", StructureFeature.VILLAGE.configured(new JigsawConfiguration(() -> SnowyVillagePools.START, 6))
	);
	private static final ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> VILLAGE_TAIGA = register(
		"village_taiga", StructureFeature.VILLAGE.configured(new JigsawConfiguration(() -> TaigaVillagePools.START, 6))
	);
	private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_STANDARD = register(
		"ruined_portal", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.STANDARD))
	);
	private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_DESERT = register(
		"ruined_portal_desert", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.DESERT))
	);
	private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_JUNGLE = register(
		"ruined_portal_jungle", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.JUNGLE))
	);
	private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_SWAMP = register(
		"ruined_portal_swamp", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.SWAMP))
	);
	private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_MOUNTAIN = register(
		"ruined_portal_mountain", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.MOUNTAIN))
	);
	private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_OCEAN = register(
		"ruined_portal_ocean", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.OCEAN))
	);
	private static final ConfiguredStructureFeature<RuinedPortalConfiguration, ? extends StructureFeature<RuinedPortalConfiguration>> RUINED_PORTAL_NETHER = register(
		"ruined_portal_nether", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.NETHER))
	);

	public static ConfiguredStructureFeature<?, ?> bootstrap() {
		return MINESHAFT;
	}

	private static <FC extends FeatureConfiguration, F extends StructureFeature<FC>> ConfiguredStructureFeature<FC, F> register(
		String string, ConfiguredStructureFeature<FC, F> configuredStructureFeature
	) {
		return BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, string, configuredStructureFeature);
	}

	private static void register(
		BiConsumer<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> biConsumer,
		ConfiguredStructureFeature<?, ?> configuredStructureFeature,
		Set<ResourceKey<Biome>> set
	) {
		set.forEach(resourceKey -> biConsumer.accept(configuredStructureFeature, resourceKey));
	}

	private static void register(
		BiConsumer<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> biConsumer,
		ConfiguredStructureFeature<?, ?> configuredStructureFeature,
		ResourceKey<Biome> resourceKey
	) {
		biConsumer.accept(configuredStructureFeature, resourceKey);
	}

	public static void registerStructures(BiConsumer<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> biConsumer) {
		Set<ResourceKey<Biome>> set = ImmutableSet.<ResourceKey<Biome>>builder()
			.add(Biomes.DEEP_FROZEN_OCEAN)
			.add(Biomes.DEEP_COLD_OCEAN)
			.add(Biomes.DEEP_OCEAN)
			.add(Biomes.DEEP_LUKEWARM_OCEAN)
			.add(Biomes.DEEP_WARM_OCEAN)
			.build();
		Set<ResourceKey<Biome>> set2 = ImmutableSet.<ResourceKey<Biome>>builder()
			.add(Biomes.FROZEN_OCEAN)
			.add(Biomes.OCEAN)
			.add(Biomes.COLD_OCEAN)
			.add(Biomes.LUKEWARM_OCEAN)
			.add(Biomes.WARM_OCEAN)
			.addAll(set)
			.build();
		Set<ResourceKey<Biome>> set3 = ImmutableSet.<ResourceKey<Biome>>builder().add(Biomes.BEACH).add(Biomes.SNOWY_BEACH).build();
		Set<ResourceKey<Biome>> set4 = ImmutableSet.<ResourceKey<Biome>>builder().add(Biomes.RIVER).add(Biomes.FROZEN_RIVER).build();
		Set<ResourceKey<Biome>> set5 = ImmutableSet.<ResourceKey<Biome>>builder()
			.add(Biomes.MEADOW)
			.add(Biomes.SNOWCAPPED_PEAKS)
			.add(Biomes.LOFTY_PEAKS)
			.add(Biomes.STONY_PEAKS)
			.add(Biomes.SNOWY_SLOPES)
			.build();
		Set<ResourceKey<Biome>> set6 = ImmutableSet.<ResourceKey<Biome>>builder()
			.add(Biomes.BADLANDS_PLATEAU)
			.add(Biomes.ERODED_BADLANDS)
			.add(Biomes.WOODED_BADLANDS_PLATEAU)
			.build();
		Set<ResourceKey<Biome>> set7 = ImmutableSet.<ResourceKey<Biome>>builder()
			.add(Biomes.MOUNTAINS)
			.add(Biomes.WOODED_MOUNTAINS)
			.add(Biomes.GRAVELLY_MOUNTAINS)
			.build();
		Set<ResourceKey<Biome>> set8 = ImmutableSet.<ResourceKey<Biome>>builder()
			.add(Biomes.TAIGA)
			.add(Biomes.SNOWY_TAIGA)
			.add(Biomes.GIANT_TREE_TAIGA)
			.add(Biomes.GIANT_SPRUCE_TAIGA)
			.build();
		Set<ResourceKey<Biome>> set9 = ImmutableSet.<ResourceKey<Biome>>builder().add(Biomes.BAMBOO_JUNGLE).add(Biomes.JUNGLE).add(Biomes.JUNGLE_EDGE).build();
		Set<ResourceKey<Biome>> set10 = ImmutableSet.<ResourceKey<Biome>>builder()
			.add(Biomes.FOREST)
			.add(Biomes.FLOWER_FOREST)
			.add(Biomes.BIRCH_FOREST)
			.add(Biomes.TALL_BIRCH_FOREST)
			.add(Biomes.DARK_FOREST)
			.add(Biomes.GROVE)
			.build();
		Set<ResourceKey<Biome>> set11 = ImmutableSet.<ResourceKey<Biome>>builder()
			.add(Biomes.NETHER_WASTES)
			.add(Biomes.BASALT_DELTAS)
			.add(Biomes.SOUL_SAND_VALLEY)
			.add(Biomes.CRIMSON_FOREST)
			.add(Biomes.WARPED_FOREST)
			.build();
		register(biConsumer, BURIED_TREASURE, set3);
		register(biConsumer, DESERT_PYRAMID, Biomes.DESERT);
		register(biConsumer, IGLOO, Biomes.SNOWY_TAIGA);
		register(biConsumer, IGLOO, Biomes.SNOWY_TUNDRA);
		register(biConsumer, IGLOO, Biomes.SNOWY_SLOPES);
		register(biConsumer, JUNGLE_TEMPLE, Biomes.BAMBOO_JUNGLE);
		register(biConsumer, JUNGLE_TEMPLE, Biomes.JUNGLE);
		register(biConsumer, MINESHAFT, set2);
		register(biConsumer, MINESHAFT, set4);
		register(biConsumer, MINESHAFT, set3);
		register(biConsumer, MINESHAFT, Biomes.STONE_SHORE);
		register(biConsumer, MINESHAFT, set5);
		register(biConsumer, MINESHAFT, set7);
		register(biConsumer, MINESHAFT, set8);
		register(biConsumer, MINESHAFT, set9);
		register(biConsumer, MINESHAFT, set10);
		register(biConsumer, MINESHAFT, Biomes.MUSHROOM_FIELDS);
		register(biConsumer, MINESHAFT, Biomes.ICE_SPIKES);
		register(biConsumer, MINESHAFT, Biomes.SHATTERED_SAVANNA);
		register(biConsumer, MINESHAFT, Biomes.DESERT);
		register(biConsumer, MINESHAFT, Biomes.SAVANNA);
		register(biConsumer, MINESHAFT, Biomes.SNOWY_TUNDRA);
		register(biConsumer, MINESHAFT, Biomes.PLAINS);
		register(biConsumer, MINESHAFT, Biomes.SUNFLOWER_PLAINS);
		register(biConsumer, MINESHAFT, Biomes.SWAMP);
		register(biConsumer, MINESHAFT, Biomes.SAVANNA_PLATEAU);
		register(biConsumer, MINESHAFT, Biomes.DRIPSTONE_CAVES);
		register(biConsumer, MINESHAFT, Biomes.LUSH_CAVES);
		register(biConsumer, MINESHAFT_MESA, set6);
		register(biConsumer, OCEAN_MONUMENT, set);
		register(biConsumer, OCEAN_RUIN_COLD, Biomes.FROZEN_OCEAN);
		register(biConsumer, OCEAN_RUIN_COLD, Biomes.COLD_OCEAN);
		register(biConsumer, OCEAN_RUIN_COLD, Biomes.OCEAN);
		register(biConsumer, OCEAN_RUIN_COLD, Biomes.DEEP_FROZEN_OCEAN);
		register(biConsumer, OCEAN_RUIN_COLD, Biomes.DEEP_COLD_OCEAN);
		register(biConsumer, OCEAN_RUIN_COLD, Biomes.DEEP_OCEAN);
		register(biConsumer, OCEAN_RUIN_WARM, Biomes.LUKEWARM_OCEAN);
		register(biConsumer, OCEAN_RUIN_WARM, Biomes.WARM_OCEAN);
		register(biConsumer, OCEAN_RUIN_WARM, Biomes.DEEP_LUKEWARM_OCEAN);
		register(biConsumer, OCEAN_RUIN_WARM, Biomes.DEEP_WARM_OCEAN);
		register(biConsumer, PILLAGER_OUTPOST, Biomes.DESERT);
		register(biConsumer, PILLAGER_OUTPOST, Biomes.PLAINS);
		register(biConsumer, PILLAGER_OUTPOST, Biomes.SAVANNA);
		register(biConsumer, PILLAGER_OUTPOST, Biomes.SNOWY_TUNDRA);
		register(biConsumer, PILLAGER_OUTPOST, Biomes.TAIGA);
		register(biConsumer, PILLAGER_OUTPOST, set5);
		register(biConsumer, PILLAGER_OUTPOST, Biomes.GROVE);
		register(biConsumer, RUINED_PORTAL_DESERT, Biomes.DESERT);
		register(biConsumer, RUINED_PORTAL_JUNGLE, set9);
		register(biConsumer, RUINED_PORTAL_OCEAN, set2);
		register(biConsumer, RUINED_PORTAL_SWAMP, Biomes.SWAMP);
		register(biConsumer, RUINED_PORTAL_MOUNTAIN, set6);
		register(biConsumer, RUINED_PORTAL_MOUNTAIN, set7);
		register(biConsumer, RUINED_PORTAL_MOUNTAIN, Biomes.SAVANNA_PLATEAU);
		register(biConsumer, RUINED_PORTAL_MOUNTAIN, Biomes.SHATTERED_SAVANNA);
		register(biConsumer, RUINED_PORTAL_MOUNTAIN, Biomes.STONE_SHORE);
		register(biConsumer, RUINED_PORTAL_MOUNTAIN, set5);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.MUSHROOM_FIELDS);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.ICE_SPIKES);
		register(biConsumer, RUINED_PORTAL_STANDARD, set3);
		register(biConsumer, RUINED_PORTAL_STANDARD, set4);
		register(biConsumer, RUINED_PORTAL_STANDARD, set8);
		register(biConsumer, RUINED_PORTAL_STANDARD, set10);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.DRIPSTONE_CAVES);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.LUSH_CAVES);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.SAVANNA);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.SNOWY_TUNDRA);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.PLAINS);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.SUNFLOWER_PLAINS);
		register(biConsumer, SHIPWRECK_BEACHED, set3);
		register(biConsumer, SHIPWRECK, set2);
		register(biConsumer, SWAMP_HUT, Biomes.SWAMP);
		register(biConsumer, VILLAGE_DESERT, Biomes.DESERT);
		register(biConsumer, VILLAGE_PLAINS, Biomes.PLAINS);
		register(biConsumer, VILLAGE_PLAINS, Biomes.MEADOW);
		register(biConsumer, VILLAGE_SAVANNA, Biomes.SAVANNA);
		register(biConsumer, VILLAGE_SNOWY, Biomes.SNOWY_TUNDRA);
		register(biConsumer, VILLAGE_TAIGA, Biomes.TAIGA);
		register(biConsumer, WOODLAND_MANSION, Biomes.DARK_FOREST);
		register(biConsumer, DESERT_PYRAMID, Biomes.DESERT_HILLS);
		register(biConsumer, JUNGLE_TEMPLE, Biomes.BAMBOO_JUNGLE_HILLS);
		register(biConsumer, JUNGLE_TEMPLE, Biomes.JUNGLE_HILLS);
		register(biConsumer, MINESHAFT, Biomes.BAMBOO_JUNGLE_HILLS);
		register(biConsumer, MINESHAFT, Biomes.BIRCH_FOREST_HILLS);
		register(biConsumer, MINESHAFT, Biomes.DARK_FOREST_HILLS);
		register(biConsumer, MINESHAFT, Biomes.DESERT_HILLS);
		register(biConsumer, MINESHAFT, Biomes.DESERT_LAKES);
		register(biConsumer, MINESHAFT, Biomes.GIANT_SPRUCE_TAIGA_HILLS);
		register(biConsumer, MINESHAFT, Biomes.GIANT_TREE_TAIGA_HILLS);
		register(biConsumer, MINESHAFT, Biomes.JUNGLE_HILLS);
		register(biConsumer, MINESHAFT, Biomes.MODIFIED_GRAVELLY_MOUNTAINS);
		register(biConsumer, MINESHAFT, Biomes.MODIFIED_JUNGLE);
		register(biConsumer, MINESHAFT, Biomes.MODIFIED_JUNGLE_EDGE);
		register(biConsumer, MINESHAFT, Biomes.MOUNTAIN_EDGE);
		register(biConsumer, MINESHAFT, Biomes.MUSHROOM_FIELD_SHORE);
		register(biConsumer, MINESHAFT, Biomes.SHATTERED_SAVANNA_PLATEAU);
		register(biConsumer, MINESHAFT, Biomes.SNOWY_MOUNTAINS);
		register(biConsumer, MINESHAFT, Biomes.SNOWY_TAIGA_HILLS);
		register(biConsumer, MINESHAFT, Biomes.SNOWY_TAIGA_MOUNTAINS);
		register(biConsumer, MINESHAFT, Biomes.SWAMP_HILLS);
		register(biConsumer, MINESHAFT, Biomes.TAIGA_HILLS);
		register(biConsumer, MINESHAFT, Biomes.TAIGA_MOUNTAINS);
		register(biConsumer, MINESHAFT, Biomes.TALL_BIRCH_HILLS);
		register(biConsumer, MINESHAFT, Biomes.WOODED_HILLS);
		register(biConsumer, MINESHAFT_MESA, Biomes.BADLANDS);
		register(biConsumer, MINESHAFT_MESA, Biomes.MODIFIED_BADLANDS_PLATEAU);
		register(biConsumer, MINESHAFT_MESA, Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU);
		register(biConsumer, RUINED_PORTAL_DESERT, Biomes.DESERT_HILLS);
		register(biConsumer, RUINED_PORTAL_DESERT, Biomes.DESERT_LAKES);
		register(biConsumer, RUINED_PORTAL_JUNGLE, Biomes.BAMBOO_JUNGLE_HILLS);
		register(biConsumer, RUINED_PORTAL_JUNGLE, Biomes.JUNGLE_HILLS);
		register(biConsumer, RUINED_PORTAL_JUNGLE, Biomes.MODIFIED_JUNGLE);
		register(biConsumer, RUINED_PORTAL_JUNGLE, Biomes.MODIFIED_JUNGLE_EDGE);
		register(biConsumer, RUINED_PORTAL_MOUNTAIN, Biomes.MODIFIED_BADLANDS_PLATEAU);
		register(biConsumer, RUINED_PORTAL_MOUNTAIN, Biomes.MODIFIED_GRAVELLY_MOUNTAINS);
		register(biConsumer, RUINED_PORTAL_MOUNTAIN, Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU);
		register(biConsumer, RUINED_PORTAL_MOUNTAIN, Biomes.MOUNTAIN_EDGE);
		register(biConsumer, RUINED_PORTAL_MOUNTAIN, Biomes.SHATTERED_SAVANNA_PLATEAU);
		register(biConsumer, RUINED_PORTAL_MOUNTAIN, Biomes.SNOWY_MOUNTAINS);
		register(biConsumer, RUINED_PORTAL_MOUNTAIN, Biomes.SNOWY_TAIGA_MOUNTAINS);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.BADLANDS);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.BIRCH_FOREST_HILLS);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.DARK_FOREST_HILLS);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.GIANT_SPRUCE_TAIGA_HILLS);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.GIANT_TREE_TAIGA_HILLS);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.MUSHROOM_FIELD_SHORE);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.SNOWY_TAIGA_HILLS);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.TAIGA_HILLS);
		register(biConsumer, RUINED_PORTAL_MOUNTAIN, Biomes.TAIGA_MOUNTAINS);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.TALL_BIRCH_HILLS);
		register(biConsumer, RUINED_PORTAL_STANDARD, Biomes.WOODED_HILLS);
		register(biConsumer, RUINED_PORTAL_SWAMP, Biomes.SWAMP_HILLS);
		register(biConsumer, WOODLAND_MANSION, Biomes.DARK_FOREST_HILLS);
		register(biConsumer, NETHER_BRIDGE, set11);
		register(biConsumer, NETHER_FOSSIL, Biomes.SOUL_SAND_VALLEY);
		register(biConsumer, BASTION_REMNANT, Biomes.CRIMSON_FOREST);
		register(biConsumer, BASTION_REMNANT, Biomes.NETHER_WASTES);
		register(biConsumer, BASTION_REMNANT, Biomes.SOUL_SAND_VALLEY);
		register(biConsumer, BASTION_REMNANT, Biomes.WARPED_FOREST);
		register(biConsumer, RUINED_PORTAL_NETHER, set11);
		register(biConsumer, END_CITY, Biomes.END_HIGHLANDS);
		register(biConsumer, END_CITY, Biomes.END_MIDLANDS);
	}
}
