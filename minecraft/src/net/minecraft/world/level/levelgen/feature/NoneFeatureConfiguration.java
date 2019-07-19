package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class NoneFeatureConfiguration implements FeatureConfiguration {
	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.emptyMap());
	}

	public static <T> NoneFeatureConfiguration deserialize(Dynamic<T> dynamic) {
		return FeatureConfiguration.NONE;
	}
}
