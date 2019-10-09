package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class NoiseDependantDecoratorConfiguration implements DecoratorConfiguration {
	public final double noiseLevel;
	public final int belowNoise;
	public final int aboveNoise;

	public NoiseDependantDecoratorConfiguration(double d, int i, int j) {
		this.noiseLevel = d;
		this.belowNoise = i;
		this.aboveNoise = j;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("noise_level"),
					dynamicOps.createDouble(this.noiseLevel),
					dynamicOps.createString("below_noise"),
					dynamicOps.createInt(this.belowNoise),
					dynamicOps.createString("above_noise"),
					dynamicOps.createInt(this.aboveNoise)
				)
			)
		);
	}

	public static NoiseDependantDecoratorConfiguration deserialize(Dynamic<?> dynamic) {
		double d = dynamic.get("noise_level").asDouble(0.0);
		int i = dynamic.get("below_noise").asInt(0);
		int j = dynamic.get("above_noise").asInt(0);
		return new NoiseDependantDecoratorConfiguration(d, i, j);
	}
}
