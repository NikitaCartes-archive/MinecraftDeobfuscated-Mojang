/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;

@Deprecated
public class LazyLoadedValue<T> {
    private final Supplier<T> factory = Suppliers.memoize(supplier::get);

    public LazyLoadedValue(Supplier<T> supplier) {
    }

    public T get() {
        return this.factory.get();
    }
}

