package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;

public class MineshaftConfiguration implements FeatureConfiguration {
	public final double probability;
	public final MineshaftFeature.Type type;

	public MineshaftConfiguration(double d, MineshaftFeature.Type type) {
		this.probability = d;
		this.type = type;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("probability"),
					dynamicOps.createDouble(this.probability),
					dynamicOps.createString("type"),
					dynamicOps.createString(this.type.getName())
				)
			)
		);
	}

	public static <T> MineshaftConfiguration deserialize(Dynamic<T> dynamic) {
		float f = dynamic.get("probability").asFloat(0.0F);
		MineshaftFeature.Type type = MineshaftFeature.Type.byName(dynamic.get("type").asString(""));
		return new MineshaftConfiguration((double)f, type);
	}
}
