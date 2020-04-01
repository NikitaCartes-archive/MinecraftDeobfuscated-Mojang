package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;

public class NoneDecoratorConfiguration implements DecoratorConfiguration {
	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.emptyMap());
	}

	public static NoneDecoratorConfiguration deserialize(Dynamic<?> dynamic) {
		return new NoneDecoratorConfiguration();
	}

	public static NoneDecoratorConfiguration random(Random random) {
		return DecoratorConfiguration.NONE;
	}
}
