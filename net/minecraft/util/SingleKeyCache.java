/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import java.util.Objects;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

public class SingleKeyCache<K, V> {
    private final Function<K, V> computeValue;
    @Nullable
    private K cacheKey = null;
    @Nullable
    private V cachedValue;

    public SingleKeyCache(Function<K, V> function) {
        this.computeValue = function;
    }

    public V getValue(K object) {
        if (this.cachedValue == null || !Objects.equals(this.cacheKey, object)) {
            this.cachedValue = this.computeValue.apply(object);
            this.cacheKey = object;
        }
        return this.cachedValue;
    }
}

