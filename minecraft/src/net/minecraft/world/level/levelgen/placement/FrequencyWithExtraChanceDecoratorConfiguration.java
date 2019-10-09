package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class FrequencyWithExtraChanceDecoratorConfiguration implements DecoratorConfiguration {
	public final int count;
	public final float extraChance;
	public final int extraCount;

	public FrequencyWithExtraChanceDecoratorConfiguration(int i, float f, int j) {
		this.count = i;
		this.extraChance = f;
		this.extraCount = j;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("count"),
					dynamicOps.createInt(this.count),
					dynamicOps.createString("extra_chance"),
					dynamicOps.createFloat(this.extraChance),
					dynamicOps.createString("extra_count"),
					dynamicOps.createInt(this.extraCount)
				)
			)
		);
	}

	public static FrequencyWithExtraChanceDecoratorConfiguration deserialize(Dynamic<?> dynamic) {
		int i = dynamic.get("count").asInt(0);
		float f = dynamic.get("extra_chance").asFloat(0.0F);
		int j = dynamic.get("extra_count").asInt(0);
		return new FrequencyWithExtraChanceDecoratorConfiguration(i, f, j);
	}
}
