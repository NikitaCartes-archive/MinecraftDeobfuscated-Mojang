package net.minecraft.data.worldgen.features;

import com.google.common.collect.ImmutableList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;

public class EndFeatures {
	public static final ConfiguredFeature<?, ?> END_SPIKE = FeatureUtils.register(
		"end_spike", Feature.END_SPIKE.configured(new SpikeConfiguration(false, ImmutableList.of(), null))
	);
	public static final ConfiguredFeature<?, ?> END_GATEWAY_RETURN = FeatureUtils.register(
		"end_gateway_return", Feature.END_GATEWAY.configured(EndGatewayConfiguration.knownExit(ServerLevel.END_SPAWN_POINT, true))
	);
	public static final ConfiguredFeature<?, ?> END_GATEWAY_DELAYED = FeatureUtils.register(
		"end_gateway_delayed", Feature.END_GATEWAY.configured(EndGatewayConfiguration.delayedExitSearch())
	);
	public static final ConfiguredFeature<?, ?> CHORUS_PLANT = FeatureUtils.register("chorus_plant", Feature.CHORUS_PLANT.configured(FeatureConfiguration.NONE));
	public static final ConfiguredFeature<?, ?> END_ISLAND = FeatureUtils.register("end_island", Feature.END_ISLAND.configured(FeatureConfiguration.NONE));
}
