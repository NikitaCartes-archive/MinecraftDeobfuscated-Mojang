package net.minecraft.data.worldgen;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class Carvers {
	public static final ConfiguredWorldCarver<ProbabilityFeatureConfiguration> CAVE = register(
		"cave", WorldCarver.CAVE.configured(new ProbabilityFeatureConfiguration(0.14285715F))
	);
	public static final ConfiguredWorldCarver<ProbabilityFeatureConfiguration> CANYON = register(
		"canyon", WorldCarver.CANYON.configured(new ProbabilityFeatureConfiguration(0.02F))
	);
	public static final ConfiguredWorldCarver<ProbabilityFeatureConfiguration> OCEAN_CAVE = register(
		"ocean_cave", WorldCarver.CAVE.configured(new ProbabilityFeatureConfiguration(0.06666667F))
	);
	public static final ConfiguredWorldCarver<ProbabilityFeatureConfiguration> NETHER_CAVE = register(
		"nether_cave", WorldCarver.NETHER_CAVE.configured(new ProbabilityFeatureConfiguration(0.2F))
	);

	private static <WC extends CarverConfiguration> ConfiguredWorldCarver<WC> register(String string, ConfiguredWorldCarver<WC> configuredWorldCarver) {
		return BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_CARVER, string, configuredWorldCarver);
	}
}
