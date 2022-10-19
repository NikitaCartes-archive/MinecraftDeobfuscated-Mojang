/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import net.minecraft.util.TaskChainer;
import org.slf4j.Logger;

public class FutureChain
implements TaskChainer,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private CompletableFuture<?> head = CompletableFuture.completedFuture(null);
    private final Executor checkedExecutor = runnable -> {
        if (!this.closed) {
            executor.execute(runnable);
        }
    };
    private volatile boolean closed;

    public FutureChain(Executor executor) {
    }

    @Override
    public void append(TaskChainer.DelayedTask delayedTask) {
        this.head = ((CompletableFuture)this.head.thenComposeAsync(object -> delayedTask.submit(this.checkedExecutor), this.checkedExecutor)).exceptionally(throwable -> {
            if (throwable instanceof CompletionException) {
                CompletionException completionException = (CompletionException)throwable;
                throwable = completionException.getCause();
            }
            if (throwable instanceof CancellationException) {
                CancellationException cancellationException = (CancellationException)throwable;
                throw cancellationException;
            }
            LOGGER.error("Chain link failed, continuing to next one", (Throwable)throwable);
            return null;
        });
    }

    @Override
    public void close() {
        this.closed = true;
    }
}

