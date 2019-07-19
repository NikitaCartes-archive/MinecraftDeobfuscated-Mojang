package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomFeatureConfig implements FeatureConfiguration {
	public final List<WeightedConfiguredFeature<?>> features;
	public final ConfiguredFeature<?> defaultFeature;

	public RandomFeatureConfig(List<WeightedConfiguredFeature<?>> list, ConfiguredFeature<?> configuredFeature) {
		this.features = list;
		this.defaultFeature = configuredFeature;
	}

	public RandomFeatureConfig(
		Feature<?>[] features, FeatureConfiguration[] featureConfigurations, float[] fs, Feature<?> feature, FeatureConfiguration featureConfiguration
	) {
		this(
			(List<WeightedConfiguredFeature<?>>)IntStream.range(0, features.length)
				.mapToObj(i -> getWeightedConfiguredFeature(features[i], featureConfigurations[i], fs[i]))
				.collect(Collectors.toList()),
			getDefaultFeature(feature, featureConfiguration)
		);
	}

	private static <FC extends FeatureConfiguration> WeightedConfiguredFeature<FC> getWeightedConfiguredFeature(
		Feature<FC> feature, FeatureConfiguration featureConfiguration, float f
	) {
		return new WeightedConfiguredFeature<>(feature, (FC)featureConfiguration, Float.valueOf(f));
	}

	private static <FC extends FeatureConfiguration> ConfiguredFeature<FC> getDefaultFeature(Feature<FC> feature, FeatureConfiguration featureConfiguration) {
		return new ConfiguredFeature<>(feature, (FC)featureConfiguration);
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		T object = dynamicOps.createList(this.features.stream().map(weightedConfiguredFeature -> weightedConfiguredFeature.serialize(dynamicOps).getValue()));
		T object2 = this.defaultFeature.serialize(dynamicOps).getValue();
		return new Dynamic<>(
			dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("features"), object, dynamicOps.createString("default"), object2))
		);
	}

	public static <T> RandomFeatureConfig deserialize(Dynamic<T> dynamic) {
		List<WeightedConfiguredFeature<?>> list = dynamic.get("features").asList(WeightedConfiguredFeature::deserialize);
		ConfiguredFeature<?> configuredFeature = ConfiguredFeature.deserialize(dynamic.get("default").orElseEmptyMap());
		return new RandomFeatureConfig(list, configuredFeature);
	}
}
