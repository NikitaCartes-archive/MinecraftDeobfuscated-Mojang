/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import org.jetbrains.annotations.Nullable;

public class ChatPreviewThrottler {
    private boolean sentRequestThisTick;
    @Nullable
    private Runnable pendingRequest;

    public void tick() {
        Runnable runnable = this.pendingRequest;
        if (runnable != null) {
            runnable.run();
            this.pendingRequest = null;
        }
        this.sentRequestThisTick = false;
    }

    public void execute(Runnable runnable) {
        if (this.sentRequestThisTick) {
            this.pendingRequest = runnable;
        } else {
            runnable.run();
            this.sentRequestThisTick = true;
            this.pendingRequest = null;
        }
    }
}

