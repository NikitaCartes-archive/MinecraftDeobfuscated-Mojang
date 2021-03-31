package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.OptionalInt;
import net.minecraft.resources.ResourceKey;

public abstract class WritableRegistry<T> extends Registry<T> {
	public WritableRegistry(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle) {
		super(resourceKey, lifecycle);
	}

	public abstract <V extends T> V registerMapping(int i, ResourceKey<T> resourceKey, V object, Lifecycle lifecycle);

	public abstract <V extends T> V register(ResourceKey<T> resourceKey, V object, Lifecycle lifecycle);

	public abstract <V extends T> V registerOrOverride(OptionalInt optionalInt, ResourceKey<T> resourceKey, V object, Lifecycle lifecycle);

	public abstract boolean isEmpty();
}
