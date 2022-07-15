/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.slf4j.Logger;

@FunctionalInterface
public interface TaskChainer {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final TaskChainer IMMEDIATE = delayedTask -> ((CompletableFuture)delayedTask.get()).exceptionally(throwable -> {
        LOGGER.error("Task failed", (Throwable)throwable);
        return null;
    });

    public void append(DelayedTask var1);

    public static interface DelayedTask
    extends Supplier<CompletableFuture<?>> {
    }
}

