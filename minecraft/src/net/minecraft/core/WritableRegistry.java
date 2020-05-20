package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import net.minecraft.resources.ResourceKey;

public abstract class WritableRegistry<T> extends Registry<T> {
	public WritableRegistry(ResourceKey<Registry<T>> resourceKey, Lifecycle lifecycle) {
		super(resourceKey, lifecycle);
	}

	public abstract <V extends T> V registerMapping(int i, ResourceKey<T> resourceKey, V object);

	public abstract <V extends T> V register(ResourceKey<T> resourceKey, V object);

	public String toString() {
		return "Registry[" + WRITABLE_REGISTRY.getKey(this) + "]";
	}
}
