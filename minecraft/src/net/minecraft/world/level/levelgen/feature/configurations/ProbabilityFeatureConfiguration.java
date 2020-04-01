package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;

public class ProbabilityFeatureConfiguration implements CarverConfiguration, FeatureConfiguration {
	public final float probability;

	public ProbabilityFeatureConfiguration(float f) {
		this.probability = f;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("probability"), dynamicOps.createFloat(this.probability))));
	}

	public static <T> ProbabilityFeatureConfiguration deserialize(Dynamic<T> dynamic) {
		float f = dynamic.get("probability").asFloat(0.0F);
		return new ProbabilityFeatureConfiguration(f);
	}

	public static ProbabilityFeatureConfiguration random(Random random) {
		return new ProbabilityFeatureConfiguration(random.nextFloat() / 2.0F);
	}
}
