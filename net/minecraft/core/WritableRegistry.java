/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public abstract class WritableRegistry<T>
extends Registry<T> {
    public WritableRegistry(ResourceKey<Registry<T>> resourceKey, Lifecycle lifecycle) {
        super(resourceKey, lifecycle);
    }

    public abstract <V extends T> V registerMapping(int var1, ResourceKey<T> var2, V var3);

    public abstract <V extends T> V register(ResourceKey<T> var1, V var2);
}

