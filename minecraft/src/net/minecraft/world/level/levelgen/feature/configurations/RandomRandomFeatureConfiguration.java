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

public class RandomRandomFeatureConfiguration implements FeatureConfiguration {
	public final List<ConfiguredFeature<?, ?>> features;
	public final int count;

	public RandomRandomFeatureConfiguration(List<ConfiguredFeature<?, ?>> list, int i) {
		this.features = list;
		this.count = i;
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

	public static <T> RandomRandomFeatureConfiguration deserialize(Dynamic<T> dynamic) {
		List<ConfiguredFeature<?, ?>> list = dynamic.get("features").asList(ConfiguredFeature::deserialize);
		int i = dynamic.get("count").asInt(0);
		return new RandomRandomFeatureConfiguration(list, i);
	}

	public static RandomRandomFeatureConfiguration random(Random random) {
		return new RandomRandomFeatureConfiguration(
			(List<ConfiguredFeature<?, ?>>)Util.randomObjectStream(random, 1, 10, Registry.FEATURE).map(feature -> feature.random(random)).collect(Collectors.toList()),
			random.nextInt(5) + 3
		);
	}
}
