package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class HellSpringConfiguration implements FeatureConfiguration {
	public final boolean insideRock;

	public HellSpringConfiguration(boolean bl) {
		this.insideRock = bl;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("inside_rock"), dynamicOps.createBoolean(this.insideRock))));
	}

	public static <T> HellSpringConfiguration deserialize(Dynamic<T> dynamic) {
		boolean bl = dynamic.get("inside_rock").asBoolean(false);
		return new HellSpringConfiguration(bl);
	}
}
