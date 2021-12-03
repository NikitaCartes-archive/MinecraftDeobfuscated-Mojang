/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import org.jetbrains.annotations.Nullable;

public interface IdMap<T>
extends Iterable<T> {
    public static final int DEFAULT = -1;

    public int getId(T var1);

    @Nullable
    public T byId(int var1);

    default public T byIdOrThrow(int i) {
        T object = this.byId(i);
        if (object == null) {
            throw new IllegalArgumentException("No value with id " + i);
        }
        return object;
    }

    public int size();
}

