package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class CountFeatureConfiguration implements FeatureConfiguration {
	public final int count;

	public CountFeatureConfiguration(int i) {
		this.count = i;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("count"), dynamicOps.createInt(this.count))));
	}

	public static <T> CountFeatureConfiguration deserialize(Dynamic<T> dynamic) {
		int i = dynamic.get("count").asInt(0);
		return new CountFeatureConfiguration(i);
	}
}
