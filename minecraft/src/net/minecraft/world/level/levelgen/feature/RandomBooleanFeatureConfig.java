package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class RandomBooleanFeatureConfig implements FeatureConfiguration {
	public final ConfiguredFeature<?> featureTrue;
	public final ConfiguredFeature<?> featureFalse;

	public RandomBooleanFeatureConfig(ConfiguredFeature<?> configuredFeature, ConfiguredFeature<?> configuredFeature2) {
		this.featureTrue = configuredFeature;
		this.featureFalse = configuredFeature2;
	}

	public RandomBooleanFeatureConfig(
		Feature<?> feature, FeatureConfiguration featureConfiguration, Feature<?> feature2, FeatureConfiguration featureConfiguration2
	) {
		this(getFeature(feature, featureConfiguration), getFeature(feature2, featureConfiguration2));
	}

	private static <FC extends FeatureConfiguration> ConfiguredFeature<FC> getFeature(Feature<FC> feature, FeatureConfiguration featureConfiguration) {
		return new ConfiguredFeature<>(feature, (FC)featureConfiguration);
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("feature_true"),
					this.featureTrue.serialize(dynamicOps).getValue(),
					dynamicOps.createString("feature_false"),
					this.featureFalse.serialize(dynamicOps).getValue()
				)
			)
		);
	}

	public static <T> RandomBooleanFeatureConfig deserialize(Dynamic<T> dynamic) {
		ConfiguredFeature<?> configuredFeature = ConfiguredFeature.deserialize(dynamic.get("feature_true").orElseEmptyMap());
		ConfiguredFeature<?> configuredFeature2 = ConfiguredFeature.deserialize(dynamic.get("feature_false").orElseEmptyMap());
		return new RandomBooleanFeatureConfig(configuredFeature, configuredFeature2);
	}
}
