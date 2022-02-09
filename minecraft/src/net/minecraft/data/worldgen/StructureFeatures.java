package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.core.Holder;
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
import net.minecraft.world.level.levelgen.feature.configurations.RangeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;

public class StructureFeatures {
	private static final Holder<ConfiguredStructureFeature<JigsawConfiguration, ?>> PILLAGER_OUTPOST = register(
		"pillager_outpost", StructureFeature.PILLAGER_OUTPOST.configured(new JigsawConfiguration(PillagerOutpostPools.START, 7))
	);
	private static final Holder<ConfiguredStructureFeature<MineshaftConfiguration, ?>> MINESHAFT = register(
		"mineshaft", StructureFeature.MINESHAFT.configured(new MineshaftConfiguration(0.004F, MineshaftFeature.Type.NORMAL))
	);
	private static final Holder<ConfiguredStructureFeature<MineshaftConfiguration, ?>> MINESHAFT_MESA = register(
		"mineshaft_mesa", StructureFeature.MINESHAFT.configured(new MineshaftConfiguration(0.004F, MineshaftFeature.Type.MESA))
	);
	private static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> WOODLAND_MANSION = register(
		"mansion", StructureFeature.WOODLAND_MANSION.configured(NoneFeatureConfiguration.INSTANCE)
	);
	private static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> JUNGLE_TEMPLE = register(
		"jungle_pyramid", StructureFeature.JUNGLE_TEMPLE.configured(NoneFeatureConfiguration.INSTANCE)
	);
	private static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> DESERT_PYRAMID = register(
		"desert_pyramid", StructureFeature.DESERT_PYRAMID.configured(NoneFeatureConfiguration.INSTANCE)
	);
	private static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> IGLOO = register(
		"igloo", StructureFeature.IGLOO.configured(NoneFeatureConfiguration.INSTANCE)
	);
	private static final Holder<ConfiguredStructureFeature<ShipwreckConfiguration, ?>> SHIPWRECK = register(
		"shipwreck", StructureFeature.SHIPWRECK.configured(new ShipwreckConfiguration(false))
	);
	private static final Holder<ConfiguredStructureFeature<ShipwreckConfiguration, ?>> SHIPWRECK_BEACHED = register(
		"shipwreck_beached", StructureFeature.SHIPWRECK.configured(new ShipwreckConfiguration(true))
	);
	private static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> SWAMP_HUT = register(
		"swamp_hut", StructureFeature.SWAMP_HUT.configured(NoneFeatureConfiguration.INSTANCE)
	);
	public static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> STRONGHOLD = register(
		"stronghold", StructureFeature.STRONGHOLD.configured(NoneFeatureConfiguration.INSTANCE)
	);
	private static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> OCEAN_MONUMENT = register(
		"monument", StructureFeature.OCEAN_MONUMENT.configured(NoneFeatureConfiguration.INSTANCE)
	);
	private static final Holder<ConfiguredStructureFeature<OceanRuinConfiguration, ?>> OCEAN_RUIN_COLD = register(
		"ocean_ruin_cold", StructureFeature.OCEAN_RUIN.configured(new OceanRuinConfiguration(OceanRuinFeature.Type.COLD, 0.3F, 0.9F))
	);
	private static final Holder<ConfiguredStructureFeature<OceanRuinConfiguration, ?>> OCEAN_RUIN_WARM = register(
		"ocean_ruin_warm", StructureFeature.OCEAN_RUIN.configured(new OceanRuinConfiguration(OceanRuinFeature.Type.WARM, 0.3F, 0.9F))
	);
	private static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> NETHER_BRIDGE = register(
		"fortress", StructureFeature.NETHER_BRIDGE.configured(NoneFeatureConfiguration.INSTANCE)
	);
	private static final Holder<ConfiguredStructureFeature<RangeConfiguration, ?>> NETHER_FOSSIL = register(
		"nether_fossil", StructureFeature.NETHER_FOSSIL.configured(new RangeConfiguration(UniformHeight.of(VerticalAnchor.absolute(32), VerticalAnchor.belowTop(2))))
	);
	private static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> END_CITY = register(
		"end_city", StructureFeature.END_CITY.configured(NoneFeatureConfiguration.INSTANCE)
	);
	private static final Holder<ConfiguredStructureFeature<ProbabilityFeatureConfiguration, ?>> BURIED_TREASURE = register(
		"buried_treasure", StructureFeature.BURIED_TREASURE.configured(new ProbabilityFeatureConfiguration(0.01F))
	);
	private static final Holder<ConfiguredStructureFeature<JigsawConfiguration, ?>> BASTION_REMNANT = register(
		"bastion_remnant", StructureFeature.BASTION_REMNANT.configured(new JigsawConfiguration(BastionPieces.START, 6))
	);
	private static final Holder<ConfiguredStructureFeature<JigsawConfiguration, ?>> VILLAGE_PLAINS = register(
		"village_plains", StructureFeature.VILLAGE.configured(new JigsawConfiguration(PlainVillagePools.START, 6))
	);
	private static final Holder<ConfiguredStructureFeature<JigsawConfiguration, ?>> VILLAGE_DESERT = register(
		"village_desert", StructureFeature.VILLAGE.configured(new JigsawConfiguration(DesertVillagePools.START, 6))
	);
	private static final Holder<ConfiguredStructureFeature<JigsawConfiguration, ?>> VILLAGE_SAVANNA = register(
		"village_savanna", StructureFeature.VILLAGE.configured(new JigsawConfiguration(SavannaVillagePools.START, 6))
	);
	private static final Holder<ConfiguredStructureFeature<JigsawConfiguration, ?>> VILLAGE_SNOWY = register(
		"village_snowy", StructureFeature.VILLAGE.configured(new JigsawConfiguration(SnowyVillagePools.START, 6))
	);
	private static final Holder<ConfiguredStructureFeature<JigsawConfiguration, ?>> VILLAGE_TAIGA = register(
		"village_taiga", StructureFeature.VILLAGE.configured(new JigsawConfiguration(TaigaVillagePools.START, 6))
	);
	private static final Holder<ConfiguredStructureFeature<RuinedPortalConfiguration, ?>> RUINED_PORTAL_STANDARD = register(
		"ruined_portal", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.STANDARD))
	);
	private static final Holder<ConfiguredStructureFeature<RuinedPortalConfiguration, ?>> RUINED_PORTAL_DESERT = register(
		"ruined_portal_desert", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.DESERT))
	);
	private static final Holder<ConfiguredStructureFeature<RuinedPortalConfiguration, ?>> RUINED_PORTAL_JUNGLE = register(
		"ruined_portal_jungle", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.JUNGLE))
	);
	private static final Holder<ConfiguredStructureFeature<RuinedPortalConfiguration, ?>> RUINED_PORTAL_SWAMP = register(
		"ruined_portal_swamp", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.SWAMP))
	);
	private static final Holder<ConfiguredStructureFeature<RuinedPortalConfiguration, ?>> RUINED_PORTAL_MOUNTAIN = register(
		"ruined_portal_mountain", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.MOUNTAIN))
	);
	private static final Holder<ConfiguredStructureFeature<RuinedPortalConfiguration, ?>> RUINED_PORTAL_OCEAN = register(
		"ruined_portal_ocean", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.OCEAN))
	);
	private static final Holder<ConfiguredStructureFeature<RuinedPortalConfiguration, ?>> RUINED_PORTAL_NETHER = register(
		"ruined_portal_nether", StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.NETHER))
	);

	public static Holder<? extends ConfiguredStructureFeature<?, ?>> bootstrap() {
		return MINESHAFT;
	}

	private static <FC extends FeatureConfiguration, F extends StructureFeature<FC>> Holder<ConfiguredStructureFeature<FC, ?>> register(
		String string, ConfiguredStructureFeature<FC, F> configuredStructureFeature
	) {
		return BuiltinRegistries.registerExact(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, string, configuredStructureFeature);
	}

	private static void register(
		StructureFeatures.StructureConfigConsumer structureConfigConsumer, Holder<? extends ConfiguredStructureFeature<?, ?>> holder, Set<ResourceKey<Biome>> set
	) {
		set.forEach(resourceKey -> register(structureConfigConsumer, holder, resourceKey));
	}

	private static void register(
		StructureFeatures.StructureConfigConsumer structureConfigConsumer, Holder<? extends ConfiguredStructureFeature<?, ?>> holder, ResourceKey<Biome> resourceKey
	) {
		Holder<ConfiguredStructureFeature<?, ?>> holder2 = Holder.hackyErase(holder);
		structureConfigConsumer.accept(holder2.value().feature, (ResourceKey<ConfiguredStructureFeature<?, ?>>)holder2.unwrapKey().orElseThrow(), resourceKey);
	}

	public static void registerStructures(StructureFeatures.StructureConfigConsumer structureConfigConsumer) {
		Set<ResourceKey<Biome>> set = ImmutableSet.<ResourceKey<Biome>>builder()
			.add(Biomes.DEEP_FROZEN_OCEAN)
			.add(Biomes.DEEP_COLD_OCEAN)
			.add(Biomes.DEEP_OCEAN)
			.add(Biomes.DEEP_LUKEWARM_OCEAN)
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
			.add(Biomes.FROZEN_PEAKS)
			.add(Biomes.JAGGED_PEAKS)
			.add(Biomes.STONY_PEAKS)
			.add(Biomes.SNOWY_SLOPES)
			.build();
		Set<ResourceKey<Biome>> set6 = ImmutableSet.<ResourceKey<Biome>>builder()
			.add(Biomes.BADLANDS)
			.add(Biomes.ERODED_BADLANDS)
			.add(Biomes.WOODED_BADLANDS)
			.build();
		Set<ResourceKey<Biome>> set7 = ImmutableSet.<ResourceKey<Biome>>builder()
			.add(Biomes.WINDSWEPT_HILLS)
			.add(Biomes.WINDSWEPT_FOREST)
			.add(Biomes.WINDSWEPT_GRAVELLY_HILLS)
			.build();
		Set<ResourceKey<Biome>> set8 = ImmutableSet.<ResourceKey<Biome>>builder()
			.add(Biomes.TAIGA)
			.add(Biomes.SNOWY_TAIGA)
			.add(Biomes.OLD_GROWTH_PINE_TAIGA)
			.add(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
			.build();
		Set<ResourceKey<Biome>> set9 = ImmutableSet.<ResourceKey<Biome>>builder().add(Biomes.BAMBOO_JUNGLE).add(Biomes.JUNGLE).add(Biomes.SPARSE_JUNGLE).build();
		Set<ResourceKey<Biome>> set10 = ImmutableSet.<ResourceKey<Biome>>builder()
			.add(Biomes.FOREST)
			.add(Biomes.FLOWER_FOREST)
			.add(Biomes.BIRCH_FOREST)
			.add(Biomes.OLD_GROWTH_BIRCH_FOREST)
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
		Set<ResourceKey<Biome>> set12 = ImmutableSet.<ResourceKey<Biome>>builder()
			.add(Biomes.THE_VOID)
			.add(Biomes.PLAINS)
			.add(Biomes.SUNFLOWER_PLAINS)
			.add(Biomes.SNOWY_PLAINS)
			.add(Biomes.ICE_SPIKES)
			.add(Biomes.DESERT)
			.add(Biomes.FOREST)
			.add(Biomes.FLOWER_FOREST)
			.add(Biomes.BIRCH_FOREST)
			.add(Biomes.DARK_FOREST)
			.add(Biomes.OLD_GROWTH_BIRCH_FOREST)
			.add(Biomes.OLD_GROWTH_PINE_TAIGA)
			.add(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
			.add(Biomes.TAIGA)
			.add(Biomes.SNOWY_TAIGA)
			.add(Biomes.SAVANNA)
			.add(Biomes.SAVANNA_PLATEAU)
			.add(Biomes.WINDSWEPT_HILLS)
			.add(Biomes.WINDSWEPT_GRAVELLY_HILLS)
			.add(Biomes.WINDSWEPT_FOREST)
			.add(Biomes.WINDSWEPT_SAVANNA)
			.add(Biomes.JUNGLE)
			.add(Biomes.SPARSE_JUNGLE)
			.add(Biomes.BAMBOO_JUNGLE)
			.add(Biomes.BADLANDS)
			.add(Biomes.ERODED_BADLANDS)
			.add(Biomes.WOODED_BADLANDS)
			.add(Biomes.MEADOW)
			.add(Biomes.GROVE)
			.add(Biomes.SNOWY_SLOPES)
			.add(Biomes.FROZEN_PEAKS)
			.add(Biomes.JAGGED_PEAKS)
			.add(Biomes.STONY_PEAKS)
			.add(Biomes.MUSHROOM_FIELDS)
			.add(Biomes.DRIPSTONE_CAVES)
			.add(Biomes.LUSH_CAVES)
			.build();
		register(structureConfigConsumer, BURIED_TREASURE, set3);
		register(structureConfigConsumer, DESERT_PYRAMID, Biomes.DESERT);
		register(structureConfigConsumer, IGLOO, Biomes.SNOWY_TAIGA);
		register(structureConfigConsumer, IGLOO, Biomes.SNOWY_PLAINS);
		register(structureConfigConsumer, IGLOO, Biomes.SNOWY_SLOPES);
		register(structureConfigConsumer, JUNGLE_TEMPLE, Biomes.BAMBOO_JUNGLE);
		register(structureConfigConsumer, JUNGLE_TEMPLE, Biomes.JUNGLE);
		register(structureConfigConsumer, MINESHAFT, set2);
		register(structureConfigConsumer, MINESHAFT, set4);
		register(structureConfigConsumer, MINESHAFT, set3);
		register(structureConfigConsumer, MINESHAFT, Biomes.STONY_SHORE);
		register(structureConfigConsumer, MINESHAFT, set5);
		register(structureConfigConsumer, MINESHAFT, set7);
		register(structureConfigConsumer, MINESHAFT, set8);
		register(structureConfigConsumer, MINESHAFT, set9);
		register(structureConfigConsumer, MINESHAFT, set10);
		register(structureConfigConsumer, MINESHAFT, Biomes.MUSHROOM_FIELDS);
		register(structureConfigConsumer, MINESHAFT, Biomes.ICE_SPIKES);
		register(structureConfigConsumer, MINESHAFT, Biomes.WINDSWEPT_SAVANNA);
		register(structureConfigConsumer, MINESHAFT, Biomes.DESERT);
		register(structureConfigConsumer, MINESHAFT, Biomes.SAVANNA);
		register(structureConfigConsumer, MINESHAFT, Biomes.SNOWY_PLAINS);
		register(structureConfigConsumer, MINESHAFT, Biomes.PLAINS);
		register(structureConfigConsumer, MINESHAFT, Biomes.SUNFLOWER_PLAINS);
		register(structureConfigConsumer, MINESHAFT, Biomes.SWAMP);
		register(structureConfigConsumer, MINESHAFT, Biomes.SAVANNA_PLATEAU);
		register(structureConfigConsumer, MINESHAFT, Biomes.DRIPSTONE_CAVES);
		register(structureConfigConsumer, MINESHAFT, Biomes.LUSH_CAVES);
		register(structureConfigConsumer, MINESHAFT_MESA, set6);
		register(structureConfigConsumer, OCEAN_MONUMENT, set);
		register(structureConfigConsumer, OCEAN_RUIN_COLD, Biomes.FROZEN_OCEAN);
		register(structureConfigConsumer, OCEAN_RUIN_COLD, Biomes.COLD_OCEAN);
		register(structureConfigConsumer, OCEAN_RUIN_COLD, Biomes.OCEAN);
		register(structureConfigConsumer, OCEAN_RUIN_COLD, Biomes.DEEP_FROZEN_OCEAN);
		register(structureConfigConsumer, OCEAN_RUIN_COLD, Biomes.DEEP_COLD_OCEAN);
		register(structureConfigConsumer, OCEAN_RUIN_COLD, Biomes.DEEP_OCEAN);
		register(structureConfigConsumer, OCEAN_RUIN_WARM, Biomes.LUKEWARM_OCEAN);
		register(structureConfigConsumer, OCEAN_RUIN_WARM, Biomes.WARM_OCEAN);
		register(structureConfigConsumer, OCEAN_RUIN_WARM, Biomes.DEEP_LUKEWARM_OCEAN);
		register(structureConfigConsumer, PILLAGER_OUTPOST, Biomes.DESERT);
		register(structureConfigConsumer, PILLAGER_OUTPOST, Biomes.PLAINS);
		register(structureConfigConsumer, PILLAGER_OUTPOST, Biomes.SAVANNA);
		register(structureConfigConsumer, PILLAGER_OUTPOST, Biomes.SNOWY_PLAINS);
		register(structureConfigConsumer, PILLAGER_OUTPOST, Biomes.TAIGA);
		register(structureConfigConsumer, PILLAGER_OUTPOST, set5);
		register(structureConfigConsumer, PILLAGER_OUTPOST, Biomes.GROVE);
		register(structureConfigConsumer, RUINED_PORTAL_DESERT, Biomes.DESERT);
		register(structureConfigConsumer, RUINED_PORTAL_JUNGLE, set9);
		register(structureConfigConsumer, RUINED_PORTAL_OCEAN, set2);
		register(structureConfigConsumer, RUINED_PORTAL_SWAMP, Biomes.SWAMP);
		register(structureConfigConsumer, RUINED_PORTAL_MOUNTAIN, set6);
		register(structureConfigConsumer, RUINED_PORTAL_MOUNTAIN, set7);
		register(structureConfigConsumer, RUINED_PORTAL_MOUNTAIN, Biomes.SAVANNA_PLATEAU);
		register(structureConfigConsumer, RUINED_PORTAL_MOUNTAIN, Biomes.WINDSWEPT_SAVANNA);
		register(structureConfigConsumer, RUINED_PORTAL_MOUNTAIN, Biomes.STONY_SHORE);
		register(structureConfigConsumer, RUINED_PORTAL_MOUNTAIN, set5);
		register(structureConfigConsumer, RUINED_PORTAL_STANDARD, Biomes.MUSHROOM_FIELDS);
		register(structureConfigConsumer, RUINED_PORTAL_STANDARD, Biomes.ICE_SPIKES);
		register(structureConfigConsumer, RUINED_PORTAL_STANDARD, set3);
		register(structureConfigConsumer, RUINED_PORTAL_STANDARD, set4);
		register(structureConfigConsumer, RUINED_PORTAL_STANDARD, set8);
		register(structureConfigConsumer, RUINED_PORTAL_STANDARD, set10);
		register(structureConfigConsumer, RUINED_PORTAL_STANDARD, Biomes.DRIPSTONE_CAVES);
		register(structureConfigConsumer, RUINED_PORTAL_STANDARD, Biomes.LUSH_CAVES);
		register(structureConfigConsumer, RUINED_PORTAL_STANDARD, Biomes.SAVANNA);
		register(structureConfigConsumer, RUINED_PORTAL_STANDARD, Biomes.SNOWY_PLAINS);
		register(structureConfigConsumer, RUINED_PORTAL_STANDARD, Biomes.PLAINS);
		register(structureConfigConsumer, RUINED_PORTAL_STANDARD, Biomes.SUNFLOWER_PLAINS);
		register(structureConfigConsumer, SHIPWRECK_BEACHED, set3);
		register(structureConfigConsumer, SHIPWRECK, set2);
		register(structureConfigConsumer, SWAMP_HUT, Biomes.SWAMP);
		register(structureConfigConsumer, VILLAGE_DESERT, Biomes.DESERT);
		register(structureConfigConsumer, VILLAGE_PLAINS, Biomes.PLAINS);
		register(structureConfigConsumer, VILLAGE_PLAINS, Biomes.MEADOW);
		register(structureConfigConsumer, VILLAGE_SAVANNA, Biomes.SAVANNA);
		register(structureConfigConsumer, VILLAGE_SNOWY, Biomes.SNOWY_PLAINS);
		register(structureConfigConsumer, VILLAGE_TAIGA, Biomes.TAIGA);
		register(structureConfigConsumer, WOODLAND_MANSION, Biomes.DARK_FOREST);
		register(structureConfigConsumer, STRONGHOLD, set12);
		register(structureConfigConsumer, NETHER_BRIDGE, set11);
		register(structureConfigConsumer, NETHER_FOSSIL, Biomes.SOUL_SAND_VALLEY);
		register(structureConfigConsumer, BASTION_REMNANT, Biomes.CRIMSON_FOREST);
		register(structureConfigConsumer, BASTION_REMNANT, Biomes.NETHER_WASTES);
		register(structureConfigConsumer, BASTION_REMNANT, Biomes.SOUL_SAND_VALLEY);
		register(structureConfigConsumer, BASTION_REMNANT, Biomes.WARPED_FOREST);
		register(structureConfigConsumer, RUINED_PORTAL_NETHER, set11);
		register(structureConfigConsumer, END_CITY, Biomes.END_HIGHLANDS);
		register(structureConfigConsumer, END_CITY, Biomes.END_MIDLANDS);
	}

	@FunctionalInterface
	public interface StructureConfigConsumer {
		void accept(StructureFeature<?> structureFeature, ResourceKey<ConfiguredStructureFeature<?, ?>> resourceKey, ResourceKey<Biome> resourceKey2);
	}
}
