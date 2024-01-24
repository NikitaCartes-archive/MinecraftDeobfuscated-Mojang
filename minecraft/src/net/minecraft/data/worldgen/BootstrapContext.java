package net.minecraft.data.worldgen;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface BootstrapContext<T> {
	Holder.Reference<T> register(ResourceKey<T> resourceKey, T object, Lifecycle lifecycle);

	default Holder.Reference<T> register(ResourceKey<T> resourceKey, T object) {
		return this.register(resourceKey, object, Lifecycle.stable());
	}

	<S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> resourceKey);
}
