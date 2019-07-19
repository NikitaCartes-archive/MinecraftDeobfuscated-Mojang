package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class NoneDecoratorConfiguration implements DecoratorConfiguration {
	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.emptyMap());
	}

	public static NoneDecoratorConfiguration deserialize(Dynamic<?> dynamic) {
		return new NoneDecoratorConfiguration();
	}
}
