/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import java.util.function.Supplier;

public class LazyLoadedValue<T> {
    private Supplier<T> factory;
    private T value;

    public LazyLoadedValue(Supplier<T> supplier) {
        this.factory = supplier;
    }

    public T get() {
        Supplier<T> supplier = this.factory;
        if (supplier != null) {
            this.value = supplier.get();
            this.factory = null;
        }
        return this.value;
    }
}

