package net.minecraft.data.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.tags.BiomeTags;
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
		"pillager_outpost", StructureFeature.PILLAGER_OUTPOST.configured(new JigsawConfiguration(PillagerOutpostPools.START, 7), BiomeTags.HAS_PILLAGER_OUTPOST)
	);
	private static final Holder<ConfiguredStructureFeature<MineshaftConfiguration, ?>> MINESHAFT = register(
		"mineshaft", StructureFeature.MINESHAFT.configured(new MineshaftConfiguration(0.004F, MineshaftFeature.Type.NORMAL), BiomeTags.HAS_MINESHAFT)
	);
	private static final Holder<ConfiguredStructureFeature<MineshaftConfiguration, ?>> MINESHAFT_MESA = register(
		"mineshaft_mesa", StructureFeature.MINESHAFT.configured(new MineshaftConfiguration(0.004F, MineshaftFeature.Type.MESA), BiomeTags.HAS_MINESHAFT_MESA)
	);
	private static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> WOODLAND_MANSION = register(
		"mansion", StructureFeature.WOODLAND_MANSION.configured(NoneFeatureConfiguration.INSTANCE, BiomeTags.HAS_WOODLAND_MANSION)
	);
	private static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> JUNGLE_TEMPLE = register(
		"jungle_pyramid", StructureFeature.JUNGLE_TEMPLE.configured(NoneFeatureConfiguration.INSTANCE, BiomeTags.HAS_JUNGLE_TEMPLE)
	);
	private static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> DESERT_PYRAMID = register(
		"desert_pyramid", StructureFeature.DESERT_PYRAMID.configured(NoneFeatureConfiguration.INSTANCE, BiomeTags.HAS_DESERT_PYRAMID)
	);
	private static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> IGLOO = register(
		"igloo", StructureFeature.IGLOO.configured(NoneFeatureConfiguration.INSTANCE, BiomeTags.HAS_IGLOO)
	);
	private static final Holder<ConfiguredStructureFeature<ShipwreckConfiguration, ?>> SHIPWRECK = register(
		"shipwreck", StructureFeature.SHIPWRECK.configured(new ShipwreckConfiguration(false), BiomeTags.HAS_SHIPWRECK)
	);
	private static final Holder<ConfiguredStructureFeature<ShipwreckConfiguration, ?>> SHIPWRECK_BEACHED = register(
		"shipwreck_beached", StructureFeature.SHIPWRECK.configured(new ShipwreckConfiguration(true), BiomeTags.HAS_SHIPWRECK_BEACHED)
	);
	private static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> SWAMP_HUT = register(
		"swamp_hut", StructureFeature.SWAMP_HUT.configured(NoneFeatureConfiguration.INSTANCE, BiomeTags.HAS_SWAMP_HUT)
	);
	public static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> STRONGHOLD = register(
		"stronghold", StructureFeature.STRONGHOLD.configured(NoneFeatureConfiguration.INSTANCE, BiomeTags.HAS_STRONGHOLD)
	);
	private static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> OCEAN_MONUMENT = register(
		"monument", StructureFeature.OCEAN_MONUMENT.configured(NoneFeatureConfiguration.INSTANCE, BiomeTags.HAS_OCEAN_MONUMENT)
	);
	private static final Holder<ConfiguredStructureFeature<OceanRuinConfiguration, ?>> OCEAN_RUIN_COLD = register(
		"ocean_ruin_cold", StructureFeature.OCEAN_RUIN.configured(new OceanRuinConfiguration(OceanRuinFeature.Type.COLD, 0.3F, 0.9F), BiomeTags.HAS_OCEAN_RUIN_COLD)
	);
	private static final Holder<ConfiguredStructureFeature<OceanRuinConfiguration, ?>> OCEAN_RUIN_WARM = register(
		"ocean_ruin_warm", StructureFeature.OCEAN_RUIN.configured(new OceanRuinConfiguration(OceanRuinFeature.Type.WARM, 0.3F, 0.9F), BiomeTags.HAS_OCEAN_RUIN_WARM)
	);
	private static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> NETHER_BRIDGE = register(
		"fortress", StructureFeature.NETHER_BRIDGE.configured(NoneFeatureConfiguration.INSTANCE, BiomeTags.HAS_NETHER_FORTRESS)
	);
	private static final Holder<ConfiguredStructureFeature<RangeConfiguration, ?>> NETHER_FOSSIL = register(
		"nether_fossil",
		StructureFeature.NETHER_FOSSIL
			.configured(new RangeConfiguration(UniformHeight.of(VerticalAnchor.absolute(32), VerticalAnchor.belowTop(2))), BiomeTags.HAS_NETHER_FOSSIL)
	);
	private static final Holder<ConfiguredStructureFeature<NoneFeatureConfiguration, ?>> END_CITY = register(
		"end_city", StructureFeature.END_CITY.configured(NoneFeatureConfiguration.INSTANCE, BiomeTags.HAS_END_CITY)
	);
	private static final Holder<ConfiguredStructureFeature<ProbabilityFeatureConfiguration, ?>> BURIED_TREASURE = register(
		"buried_treasure", StructureFeature.BURIED_TREASURE.configured(new ProbabilityFeatureConfiguration(0.01F), BiomeTags.HAS_BURIED_TREASURE)
	);
	private static final Holder<ConfiguredStructureFeature<JigsawConfiguration, ?>> BASTION_REMNANT = register(
		"bastion_remnant", StructureFeature.BASTION_REMNANT.configured(new JigsawConfiguration(BastionPieces.START, 6), BiomeTags.HAS_BASTION_REMNANT)
	);
	private static final Holder<ConfiguredStructureFeature<JigsawConfiguration, ?>> VILLAGE_PLAINS = register(
		"village_plains", StructureFeature.VILLAGE.configured(new JigsawConfiguration(PlainVillagePools.START, 6), BiomeTags.HAS_VILLAGE_PLAINS)
	);
	private static final Holder<ConfiguredStructureFeature<JigsawConfiguration, ?>> VILLAGE_DESERT = register(
		"village_desert", StructureFeature.VILLAGE.configured(new JigsawConfiguration(DesertVillagePools.START, 6), BiomeTags.HAS_VILLAGE_DESERT)
	);
	private static final Holder<ConfiguredStructureFeature<JigsawConfiguration, ?>> VILLAGE_SAVANNA = register(
		"village_savanna", StructureFeature.VILLAGE.configured(new JigsawConfiguration(SavannaVillagePools.START, 6), BiomeTags.HAS_VILLAGE_SAVANNA)
	);
	private static final Holder<ConfiguredStructureFeature<JigsawConfiguration, ?>> VILLAGE_SNOWY = register(
		"village_snowy", StructureFeature.VILLAGE.configured(new JigsawConfiguration(SnowyVillagePools.START, 6), BiomeTags.HAS_VILLAGE_SNOWY)
	);
	private static final Holder<ConfiguredStructureFeature<JigsawConfiguration, ?>> VILLAGE_TAIGA = register(
		"village_taiga", StructureFeature.VILLAGE.configured(new JigsawConfiguration(TaigaVillagePools.START, 6), BiomeTags.HAS_VILLAGE_TAIGA)
	);
	private static final Holder<ConfiguredStructureFeature<RuinedPortalConfiguration, ?>> RUINED_PORTAL_STANDARD = register(
		"ruined_portal",
		StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.STANDARD), BiomeTags.HAS_RUINED_PORTAL_STANDARD)
	);
	private static final Holder<ConfiguredStructureFeature<RuinedPortalConfiguration, ?>> RUINED_PORTAL_DESERT = register(
		"ruined_portal_desert",
		StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.DESERT), BiomeTags.HAS_RUINED_PORTAL_DESERT)
	);
	private static final Holder<ConfiguredStructureFeature<RuinedPortalConfiguration, ?>> RUINED_PORTAL_JUNGLE = register(
		"ruined_portal_jungle",
		StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.JUNGLE), BiomeTags.HAS_RUINED_PORTAL_JUNGLE)
	);
	private static final Holder<ConfiguredStructureFeature<RuinedPortalConfiguration, ?>> RUINED_PORTAL_SWAMP = register(
		"ruined_portal_swamp",
		StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.SWAMP), BiomeTags.HAS_RUINED_PORTAL_SWAMP)
	);
	private static final Holder<ConfiguredStructureFeature<RuinedPortalConfiguration, ?>> RUINED_PORTAL_MOUNTAIN = register(
		"ruined_portal_mountain",
		StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.MOUNTAIN), BiomeTags.HAS_RUINED_PORTAL_MOUNTAIN)
	);
	private static final Holder<ConfiguredStructureFeature<RuinedPortalConfiguration, ?>> RUINED_PORTAL_OCEAN = register(
		"ruined_portal_ocean",
		StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.OCEAN), BiomeTags.HAS_RUINED_PORTAL_OCEAN)
	);
	private static final Holder<ConfiguredStructureFeature<RuinedPortalConfiguration, ?>> RUINED_PORTAL_NETHER = register(
		"ruined_portal_nether",
		StructureFeature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.NETHER), BiomeTags.HAS_RUINED_PORTAL_NETHER)
	);

	public static Holder<? extends ConfiguredStructureFeature<?, ?>> bootstrap() {
		return MINESHAFT;
	}

	private static <FC extends FeatureConfiguration, F extends StructureFeature<FC>> Holder<ConfiguredStructureFeature<FC, ?>> register(
		String string, ConfiguredStructureFeature<FC, F> configuredStructureFeature
	) {
		return BuiltinRegistries.registerExact(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, string, configuredStructureFeature);
	}
}
