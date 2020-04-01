package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class SimpleRandomFeatureConfiguration implements FeatureConfiguration {
	public final List<ConfiguredFeature<?, ?>> features;

	public SimpleRandomFeatureConfiguration(List<ConfiguredFeature<?, ?>> list) {
		this.features = list;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("features"),
					dynamicOps.createList(this.features.stream().map(configuredFeature -> configuredFeature.serialize(dynamicOps).getValue()))
				)
			)
		);
	}

	public static <T> SimpleRandomFeatureConfiguration deserialize(Dynamic<T> dynamic) {
		List<ConfiguredFeature<?, ?>> list = dynamic.get("features").asList(ConfiguredFeature::deserialize);
		return new SimpleRandomFeatureConfiguration(list);
	}

	public static SimpleRandomFeatureConfiguration random(Random random) {
		return new SimpleRandomFeatureConfiguration(
			(List<ConfiguredFeature<?, ?>>)Util.randomObjectStream(random, 1, 10, Registry.FEATURE).map(feature -> feature.random(random)).collect(Collectors.toList())
		);
	}
}
