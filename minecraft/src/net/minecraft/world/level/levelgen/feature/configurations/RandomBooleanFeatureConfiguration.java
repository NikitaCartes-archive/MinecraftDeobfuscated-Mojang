package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class RandomBooleanFeatureConfiguration implements FeatureConfiguration {
	public final ConfiguredFeature<?, ?> featureTrue;
	public final ConfiguredFeature<?, ?> featureFalse;

	public RandomBooleanFeatureConfiguration(ConfiguredFeature<?, ?> configuredFeature, ConfiguredFeature<?, ?> configuredFeature2) {
		this.featureTrue = configuredFeature;
		this.featureFalse = configuredFeature2;
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

	public static <T> RandomBooleanFeatureConfiguration deserialize(Dynamic<T> dynamic) {
		ConfiguredFeature<?, ?> configuredFeature = ConfiguredFeature.deserialize(dynamic.get("feature_true").orElseEmptyMap());
		ConfiguredFeature<?, ?> configuredFeature2 = ConfiguredFeature.deserialize(dynamic.get("feature_false").orElseEmptyMap());
		return new RandomBooleanFeatureConfiguration(configuredFeature, configuredFeature2);
	}

	public static RandomBooleanFeatureConfiguration random(Random random) {
		return new RandomBooleanFeatureConfiguration(Registry.FEATURE.getRandom(random).random(random), Registry.FEATURE.getRandom(random).random(random));
	}
}
