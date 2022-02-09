/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;

public interface ReloadInstance {
    public CompletableFuture<?> done();

    public float getActualProgress();

    default public boolean isDone() {
        return this.done().isDone();
    }

    default public void checkExceptions() {
        CompletableFuture<?> completableFuture = this.done();
        if (completableFuture.isCompletedExceptionally()) {
            completableFuture.join();
        }
    }
}

