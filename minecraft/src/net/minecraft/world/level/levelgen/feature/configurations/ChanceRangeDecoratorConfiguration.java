package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class ChanceRangeDecoratorConfiguration implements DecoratorConfiguration {
	public final float chance;
	public final int bottomOffset;
	public final int topOffset;
	public final int top;

	public ChanceRangeDecoratorConfiguration(float f, int i, int j, int k) {
		this.chance = f;
		this.bottomOffset = i;
		this.topOffset = j;
		this.top = k;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("chance"),
					dynamicOps.createFloat(this.chance),
					dynamicOps.createString("bottom_offset"),
					dynamicOps.createInt(this.bottomOffset),
					dynamicOps.createString("top_offset"),
					dynamicOps.createInt(this.topOffset),
					dynamicOps.createString("top"),
					dynamicOps.createInt(this.top)
				)
			)
		);
	}

	public static ChanceRangeDecoratorConfiguration deserialize(Dynamic<?> dynamic) {
		float f = dynamic.get("chance").asFloat(0.0F);
		int i = dynamic.get("bottom_offset").asInt(0);
		int j = dynamic.get("top_offset").asInt(0);
		int k = dynamic.get("top").asInt(0);
		return new ChanceRangeDecoratorConfiguration(f, i, j, k);
	}
}
