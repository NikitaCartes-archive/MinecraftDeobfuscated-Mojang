package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;

public class CountRangeDecoratorConfiguration implements DecoratorConfiguration {
	public final int count;
	public final int bottomOffset;
	public final int topOffset;
	public final int maximum;

	public CountRangeDecoratorConfiguration(int i, int j, int k, int l) {
		this.count = i;
		this.bottomOffset = j;
		this.topOffset = k;
		this.maximum = l;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("count"),
					dynamicOps.createInt(this.count),
					dynamicOps.createString("bottom_offset"),
					dynamicOps.createInt(this.bottomOffset),
					dynamicOps.createString("top_offset"),
					dynamicOps.createInt(this.topOffset),
					dynamicOps.createString("maximum"),
					dynamicOps.createInt(this.maximum)
				)
			)
		);
	}

	public static CountRangeDecoratorConfiguration deserialize(Dynamic<?> dynamic) {
		int i = dynamic.get("count").asInt(0);
		int j = dynamic.get("bottom_offset").asInt(0);
		int k = dynamic.get("top_offset").asInt(0);
		int l = dynamic.get("maximum").asInt(0);
		return new CountRangeDecoratorConfiguration(i, j, k, l);
	}

	public static CountRangeDecoratorConfiguration random(Random random) {
		int i = random.nextInt(11) + 1;
		int j = random.nextInt(11) + 1;
		return new CountRangeDecoratorConfiguration(random.nextInt(16) + 1, i, j, i + j + random.nextInt(70) + 1);
	}
}
