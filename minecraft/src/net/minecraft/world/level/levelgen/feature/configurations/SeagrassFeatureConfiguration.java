package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;

public class SeagrassFeatureConfiguration implements FeatureConfiguration {
	public final int count;
	public final double tallSeagrassProbability;

	public SeagrassFeatureConfiguration(int i, double d) {
		this.count = i;
		this.tallSeagrassProbability = d;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("count"),
					dynamicOps.createInt(this.count),
					dynamicOps.createString("tall_seagrass_probability"),
					dynamicOps.createDouble(this.tallSeagrassProbability)
				)
			)
		);
	}

	public static <T> SeagrassFeatureConfiguration deserialize(Dynamic<T> dynamic) {
		int i = dynamic.get("count").asInt(0);
		double d = dynamic.get("tall_seagrass_probability").asDouble(0.0);
		return new SeagrassFeatureConfiguration(i, d);
	}

	public static SeagrassFeatureConfiguration random(Random random) {
		return new SeagrassFeatureConfiguration(random.nextInt(20), random.nextDouble() / 2.0);
	}
}
