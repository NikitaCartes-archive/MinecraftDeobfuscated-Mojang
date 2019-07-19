package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.DecoratorConfiguration;

public class DecoratorRange implements DecoratorConfiguration {
	public final int min;
	public final int max;

	public DecoratorRange(int i, int j) {
		this.min = i;
		this.max = j;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(dynamicOps.createString("min"), dynamicOps.createInt(this.min), dynamicOps.createString("max"), dynamicOps.createInt(this.max))
			)
		);
	}

	public static DecoratorRange deserialize(Dynamic<?> dynamic) {
		int i = dynamic.get("min").asInt(0);
		int j = dynamic.get("max").asInt(0);
		return new DecoratorRange(i, j);
	}
}
