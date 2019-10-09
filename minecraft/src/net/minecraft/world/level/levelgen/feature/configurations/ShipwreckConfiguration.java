package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class ShipwreckConfiguration implements FeatureConfiguration {
	public final boolean isBeached;

	public ShipwreckConfiguration(boolean bl) {
		this.isBeached = bl;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("is_beached"), dynamicOps.createBoolean(this.isBeached))));
	}

	public static <T> ShipwreckConfiguration deserialize(Dynamic<T> dynamic) {
		boolean bl = dynamic.get("is_beached").asBoolean(false);
		return new ShipwreckConfiguration(bl);
	}
}
