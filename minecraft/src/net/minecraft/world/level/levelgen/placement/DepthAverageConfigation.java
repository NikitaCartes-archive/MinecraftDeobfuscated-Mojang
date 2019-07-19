package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.DecoratorConfiguration;

public class DepthAverageConfigation implements DecoratorConfiguration {
	public final int count;
	public final int baseline;
	public final int spread;

	public DepthAverageConfigation(int i, int j, int k) {
		this.count = i;
		this.baseline = j;
		this.spread = k;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("count"),
					dynamicOps.createInt(this.count),
					dynamicOps.createString("baseline"),
					dynamicOps.createInt(this.baseline),
					dynamicOps.createString("spread"),
					dynamicOps.createInt(this.spread)
				)
			)
		);
	}

	public static DepthAverageConfigation deserialize(Dynamic<?> dynamic) {
		int i = dynamic.get("count").asInt(0);
		int j = dynamic.get("baseline").asInt(0);
		int k = dynamic.get("spread").asInt(0);
		return new DepthAverageConfigation(i, j, k);
	}
}
