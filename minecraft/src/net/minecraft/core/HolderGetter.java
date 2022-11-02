package net.minecraft.core;

import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public interface HolderGetter<T> {
	Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey);

	default Holder.Reference<T> getOrThrow(ResourceKey<T> resourceKey) {
		return (Holder.Reference<T>)this.get(resourceKey).orElseThrow(() -> new IllegalStateException("Missing element " + resourceKey));
	}

	Optional<HolderSet.Named<T>> get(TagKey<T> tagKey);

	default HolderSet.Named<T> getOrThrow(TagKey<T> tagKey) {
		return (HolderSet.Named<T>)this.get(tagKey).orElseThrow(() -> new IllegalStateException("Missing tag " + tagKey));
	}

	public interface Provider {
		<T> Optional<HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey);

		default <T> HolderGetter<T> lookupOrThrow(ResourceKey<? extends Registry<? extends T>> resourceKey) {
			return (HolderGetter<T>)this.lookup(resourceKey).orElseThrow(() -> new IllegalStateException("Registry " + resourceKey.location() + " not found"));
		}
	}
}
