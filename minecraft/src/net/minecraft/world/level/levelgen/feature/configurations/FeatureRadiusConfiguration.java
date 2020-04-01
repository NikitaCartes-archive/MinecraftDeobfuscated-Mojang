package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;

public class FeatureRadiusConfiguration implements FeatureConfiguration {
	public final int radius;

	public FeatureRadiusConfiguration(int i) {
		this.radius = i;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("radius"), dynamicOps.createInt(this.radius))));
	}

	public static <T> FeatureRadiusConfiguration deserialize(Dynamic<T> dynamic) {
		int i = dynamic.get("radius").asInt(0);
		return new FeatureRadiusConfiguration(i);
	}

	public static FeatureRadiusConfiguration random(Random random) {
		return new FeatureRadiusConfiguration(random.nextInt(20) + 1);
	}
}
