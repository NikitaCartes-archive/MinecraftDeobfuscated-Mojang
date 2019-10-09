package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.WeightedConfiguredFeature;

public class RandomFeatureConfiguration implements FeatureConfiguration {
	public final List<WeightedConfiguredFeature<?>> features;
	public final ConfiguredFeature<?, ?> defaultFeature;

	public RandomFeatureConfiguration(List<WeightedConfiguredFeature<?>> list, ConfiguredFeature<?, ?> configuredFeature) {
		this.features = list;
		this.defaultFeature = configuredFeature;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		T object = dynamicOps.createList(this.features.stream().map(weightedConfiguredFeature -> weightedConfiguredFeature.serialize(dynamicOps).getValue()));
		T object2 = this.defaultFeature.serialize(dynamicOps).getValue();
		return new Dynamic<>(
			dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("features"), object, dynamicOps.createString("default"), object2))
		);
	}

	public static <T> RandomFeatureConfiguration deserialize(Dynamic<T> dynamic) {
		List<WeightedConfiguredFeature<?>> list = dynamic.get("features").asList(WeightedConfiguredFeature::deserialize);
		ConfiguredFeature<?, ?> configuredFeature = ConfiguredFeature.deserialize(dynamic.get("default").orElseEmptyMap());
		return new RandomFeatureConfiguration(list, configuredFeature);
	}
}
