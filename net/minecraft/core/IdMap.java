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

    public int size();
}

