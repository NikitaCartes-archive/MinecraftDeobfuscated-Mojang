/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import java.util.function.Consumer;

@FunctionalInterface
public interface AbortableIterationConsumer<T> {
    public Continuation accept(T var1);

    public static <T> AbortableIterationConsumer<T> forConsumer(Consumer<T> consumer) {
        return object -> {
            consumer.accept(object);
            return Continuation.CONTINUE;
        };
    }

    public static enum Continuation {
        CONTINUE,
        ABORT;


        public boolean shouldAbort() {
            return this == ABORT;
        }
    }
}

