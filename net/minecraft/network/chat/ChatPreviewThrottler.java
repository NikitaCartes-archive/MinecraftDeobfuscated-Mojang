/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.Nullable;

public class ChatPreviewThrottler {
    private final AtomicReference<Request> scheduledRequest = new AtomicReference();
    @Nullable
    private CompletableFuture<?> runningRequest;

    public void tick() {
        if (this.runningRequest != null && this.runningRequest.isDone()) {
            this.runningRequest = null;
        }
        if (this.runningRequest == null) {
            this.tickIdle();
        }
    }

    private void tickIdle() {
        Request request = this.scheduledRequest.getAndSet(null);
        if (request != null) {
            this.runningRequest = request.run();
        }
    }

    public void schedule(Request request) {
        this.scheduledRequest.set(request);
    }

    @FunctionalInterface
    public static interface Request {
        public CompletableFuture<?> run();
    }
}

