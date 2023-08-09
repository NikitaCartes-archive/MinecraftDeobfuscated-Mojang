package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import net.minecraft.resources.ResourceKey;

public interface WritableRegistry<T> extends Registry<T> {
	Holder.Reference<T> register(ResourceKey<T> resourceKey, T object, Lifecycle lifecycle);

	boolean isEmpty();

	HolderGetter<T> createRegistrationLookup();
}
