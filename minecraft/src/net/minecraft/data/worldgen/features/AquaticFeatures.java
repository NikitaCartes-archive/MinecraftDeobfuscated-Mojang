package net.minecraft.data.worldgen.features;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class AquaticFeatures {
	public static final ConfiguredFeature<?, ?> SEAGRASS_SHORT = FeatureUtils.register(
		"seagrass_short", Feature.SEAGRASS.configured(new ProbabilityFeatureConfiguration(0.3F))
	);
	public static final ConfiguredFeature<?, ?> SEAGRASS_SLIGHTLY_LESS_SHORT = FeatureUtils.register(
		"seagrass_slightly_less_short", Feature.SEAGRASS.configured(new ProbabilityFeatureConfiguration(0.4F))
	);
	public static final ConfiguredFeature<?, ?> SEAGRASS_MID = FeatureUtils.register(
		"seagrass_mid", Feature.SEAGRASS.configured(new ProbabilityFeatureConfiguration(0.6F))
	);
	public static final ConfiguredFeature<?, ?> SEAGRASS_TALL = FeatureUtils.register(
		"seagrass_tall", Feature.SEAGRASS.configured(new ProbabilityFeatureConfiguration(0.8F))
	);
	public static final ConfiguredFeature<?, ?> SEA_PICKLE = FeatureUtils.register("sea_pickle", Feature.SEA_PICKLE.configured(new CountConfiguration(20)));
	public static final ConfiguredFeature<?, ?> SEAGRASS_SIMPLE = FeatureUtils.register(
		"seagrass_simple", Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.SEAGRASS)))
	);
	public static final ConfiguredFeature<NoneFeatureConfiguration, ?> KELP = FeatureUtils.register("kelp", Feature.KELP.configured(FeatureConfiguration.NONE));
	public static final ConfiguredFeature<SimpleRandomFeatureConfiguration, ?> WARM_OCEAN_VEGETATION = FeatureUtils.register(
		"warm_ocean_vegetation",
		Feature.SIMPLE_RANDOM_SELECTOR
			.configured(
				new SimpleRandomFeatureConfiguration(
					List.of(
						(Supplier)() -> Feature.CORAL_TREE.configured(FeatureConfiguration.NONE).placed(),
						(Supplier)() -> Feature.CORAL_CLAW.configured(FeatureConfiguration.NONE).placed(),
						(Supplier)() -> Feature.CORAL_MUSHROOM.configured(FeatureConfiguration.NONE).placed()
					)
				)
			)
	);
}
