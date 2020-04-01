package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;

public class BuriedTreasureConfiguration implements FeatureConfiguration {
	public final float probability;

	public BuriedTreasureConfiguration(float f) {
		this.probability = f;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("probability"), dynamicOps.createFloat(this.probability))));
	}

	public static <T> BuriedTreasureConfiguration deserialize(Dynamic<T> dynamic) {
		float f = dynamic.get("probability").asFloat(0.0F);
		return new BuriedTreasureConfiguration(f);
	}

	public static BuriedTreasureConfiguration random(Random random) {
		return new BuriedTreasureConfiguration(random.nextFloat() / 20.0F);
	}
}
