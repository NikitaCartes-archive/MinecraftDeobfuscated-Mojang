/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DefaultedRegistry<T>
extends Registry<T> {
    @Override
    @NotNull
    public ResourceLocation getKey(T var1);

    @Override
    @NotNull
    public T get(@Nullable ResourceLocation var1);

    @Override
    @NotNull
    public T byId(int var1);

    public ResourceLocation getDefaultKey();
}

