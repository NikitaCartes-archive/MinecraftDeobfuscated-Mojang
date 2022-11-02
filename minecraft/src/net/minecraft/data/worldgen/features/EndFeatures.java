package net.minecraft.data.worldgen.features;

import com.google.common.collect.ImmutableList;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;

public class EndFeatures {
	public static final ResourceKey<ConfiguredFeature<?, ?>> END_SPIKE = FeatureUtils.createKey("end_spike");
	public static final ResourceKey<ConfiguredFeature<?, ?>> END_GATEWAY_RETURN = FeatureUtils.createKey("end_gateway_return");
	public static final ResourceKey<ConfiguredFeature<?, ?>> END_GATEWAY_DELAYED = FeatureUtils.createKey("end_gateway_delayed");
	public static final ResourceKey<ConfiguredFeature<?, ?>> CHORUS_PLANT = FeatureUtils.createKey("chorus_plant");
	public static final ResourceKey<ConfiguredFeature<?, ?>> END_ISLAND = FeatureUtils.createKey("end_island");

	public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> bootstapContext) {
		FeatureUtils.register(bootstapContext, END_SPIKE, Feature.END_SPIKE, new SpikeConfiguration(false, ImmutableList.of(), null));
		FeatureUtils.register(bootstapContext, END_GATEWAY_RETURN, Feature.END_GATEWAY, EndGatewayConfiguration.knownExit(ServerLevel.END_SPAWN_POINT, true));
		FeatureUtils.register(bootstapContext, END_GATEWAY_DELAYED, Feature.END_GATEWAY, EndGatewayConfiguration.delayedExitSearch());
		FeatureUtils.register(bootstapContext, CHORUS_PLANT, Feature.CHORUS_PLANT);
		FeatureUtils.register(bootstapContext, END_ISLAND, Feature.END_ISLAND);
	}
}
