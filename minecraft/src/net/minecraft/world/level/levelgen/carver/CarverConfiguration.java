package net.minecraft.world.level.levelgen.carver;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public interface CarverConfiguration {
	NoneCarverConfiguration NONE = new NoneCarverConfiguration();

	<T> Dynamic<T> serialize(DynamicOps<T> dynamicOps);
}
