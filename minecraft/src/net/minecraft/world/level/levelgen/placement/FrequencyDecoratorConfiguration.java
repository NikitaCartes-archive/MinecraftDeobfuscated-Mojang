package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class FrequencyDecoratorConfiguration implements DecoratorConfiguration {
	public final int count;

	public FrequencyDecoratorConfiguration(int i) {
		this.count = i;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("count"), dynamicOps.createInt(this.count))));
	}

	public static FrequencyDecoratorConfiguration deserialize(Dynamic<?> dynamic) {
		int i = dynamic.get("count").asInt(0);
		return new FrequencyDecoratorConfiguration(i);
	}

	public static FrequencyDecoratorConfiguration random(Random random) {
		return new FrequencyDecoratorConfiguration(random.nextInt(30) + 1);
	}
}
