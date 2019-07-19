package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomRandomFeatureConfig implements FeatureConfiguration {
	public final List<ConfiguredFeature<?>> features;
	public final int count;

	public RandomRandomFeatureConfig(List<ConfiguredFeature<?>> list, int i) {
		this.features = list;
		this.count = i;
	}

	public RandomRandomFeatureConfig(Feature<?>[] features, FeatureConfiguration[] featureConfigurations, int i) {
		this(
			(List<ConfiguredFeature<?>>)IntStream.range(0, features.length)
				.mapToObj(ix -> getConfiguredFeature(features[ix], featureConfigurations[ix]))
				.collect(Collectors.toList()),
			i
		);
	}

	private static <FC extends FeatureConfiguration> ConfiguredFeature<?> getConfiguredFeature(Feature<FC> feature, FeatureConfiguration featureConfiguration) {
		return new ConfiguredFeature<>(feature, (FC)featureConfiguration);
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("features"),
					dynamicOps.createList(this.features.stream().map(configuredFeature -> configuredFeature.serialize(dynamicOps).getValue())),
					dynamicOps.createString("count"),
					dynamicOps.createInt(this.count)
				)
			)
		);
	}

	public static <T> RandomRandomFeatureConfig deserialize(Dynamic<T> dynamic) {
		List<ConfiguredFeature<?>> list = dynamic.get("features").asList(ConfiguredFeature::deserialize);
		int i = dynamic.get("count").asInt(0);
		return new RandomRandomFeatureConfig(list, i);
	}
}
