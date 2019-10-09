package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class ChanceDecoratorConfiguration implements DecoratorConfiguration {
	public final int chance;

	public ChanceDecoratorConfiguration(int i) {
		this.chance = i;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("chance"), dynamicOps.createInt(this.chance))));
	}

	public static ChanceDecoratorConfiguration deserialize(Dynamic<?> dynamic) {
		int i = dynamic.get("chance").asInt(0);
		return new ChanceDecoratorConfiguration(i);
	}
}
