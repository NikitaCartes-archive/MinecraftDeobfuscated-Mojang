package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class HugeMushroomFeatureConfig implements FeatureConfiguration {
	public final boolean planted;

	public HugeMushroomFeatureConfig(boolean bl) {
		this.planted = bl;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("planted"), dynamicOps.createBoolean(this.planted))));
	}

	public static <T> HugeMushroomFeatureConfig deserialize(Dynamic<T> dynamic) {
		boolean bl = dynamic.get("planted").asBoolean(false);
		return new HugeMushroomFeatureConfig(bl);
	}
}
