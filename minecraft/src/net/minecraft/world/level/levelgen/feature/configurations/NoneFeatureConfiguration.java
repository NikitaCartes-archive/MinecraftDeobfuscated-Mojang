package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;

public class NoneFeatureConfiguration implements FeatureConfiguration {
	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.emptyMap());
	}

	public static <T> NoneFeatureConfiguration deserialize(Dynamic<T> dynamic) {
		return NONE;
	}

	public static NoneFeatureConfiguration random(Random random) {
		return NONE;
	}
}
