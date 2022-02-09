package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.OptionalInt;
import net.minecraft.resources.ResourceKey;

public abstract class WritableRegistry<T> extends Registry<T> {
	public WritableRegistry(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle) {
		super(resourceKey, lifecycle);
	}

	public abstract Holder<T> registerMapping(int i, ResourceKey<T> resourceKey, T object, Lifecycle lifecycle);

	public abstract Holder<T> register(ResourceKey<T> resourceKey, T object, Lifecycle lifecycle);

	public abstract Holder<T> registerOrOverride(OptionalInt optionalInt, ResourceKey<T> resourceKey, T object, Lifecycle lifecycle);

	public abstract boolean isEmpty();
}
