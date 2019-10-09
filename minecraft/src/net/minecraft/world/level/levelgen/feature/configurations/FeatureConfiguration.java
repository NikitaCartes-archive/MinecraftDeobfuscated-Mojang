package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public interface FeatureConfiguration {
	NoneFeatureConfiguration NONE = new NoneFeatureConfiguration();

	<T> Dynamic<T> serialize(DynamicOps<T> dynamicOps);
}
