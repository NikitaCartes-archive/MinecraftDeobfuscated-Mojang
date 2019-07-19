package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public interface DecoratorConfiguration {
	NoneDecoratorConfiguration NONE = new NoneDecoratorConfiguration();

	<T> Dynamic<T> serialize(DynamicOps<T> dynamicOps);
}
