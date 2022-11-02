/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface BootstapContext<T> {
    public Holder.Reference<T> register(ResourceKey<T> var1, T var2, Lifecycle var3);

    default public Holder.Reference<T> register(ResourceKey<T> resourceKey, T object) {
        return this.register(resourceKey, object, Lifecycle.stable());
    }

    public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> var1);
}

