package net.minecraft.util;

import com.mojang.datafixers.types.DynamicOps;

public interface Serializable {
	<T> T serialize(DynamicOps<T> dynamicOps);
}
