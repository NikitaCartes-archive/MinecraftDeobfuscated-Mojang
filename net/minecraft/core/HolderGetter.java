/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public interface HolderGetter<T> {
    public Optional<Holder.Reference<T>> get(ResourceKey<T> var1);

    default public Holder.Reference<T> getOrThrow(ResourceKey<T> resourceKey) {
        return this.get(resourceKey).orElseThrow(() -> new IllegalStateException("Missing element " + resourceKey));
    }

    public Optional<HolderSet.Named<T>> get(TagKey<T> var1);

    default public HolderSet.Named<T> getOrThrow(TagKey<T> tagKey) {
        return this.get(tagKey).orElseThrow(() -> new IllegalStateException("Missing tag " + tagKey));
    }

    public static interface Provider {
        public <T> Optional<HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> var1);

        default public <T> HolderGetter<T> lookupOrThrow(ResourceKey<? extends Registry<? extends T>> resourceKey) {
            return this.lookup(resourceKey).orElseThrow(() -> new IllegalStateException("Registry " + resourceKey.location() + " not found"));
        }
    }
}

