package net.minecraft.util;

import com.mojang.datafixers.types.DynamicOps;

public interface Serializer<O> {
	<T> T serialize(O object, DynamicOps<T> dynamicOps);
}
