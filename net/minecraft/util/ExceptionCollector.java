/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import org.jetbrains.annotations.Nullable;

public class ExceptionCollector<T extends Throwable> {
    @Nullable
    private T result;

    public void add(T throwable) {
        if (this.result == null) {
            this.result = throwable;
        } else {
            ((Throwable)this.result).addSuppressed((Throwable)throwable);
        }
    }

    public void throwIfPresent() throws T {
        if (this.result != null) {
            throw this.result;
        }
    }
}

