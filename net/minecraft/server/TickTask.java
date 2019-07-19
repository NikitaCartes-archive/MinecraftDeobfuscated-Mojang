/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server;

public class TickTask
implements Runnable {
    private final int tick;
    private final Runnable runnable;

    public TickTask(int i, Runnable runnable) {
        this.tick = i;
        this.runnable = runnable;
    }

    public int getTick() {
        return this.tick;
    }

    @Override
    public void run() {
        this.runnable.run();
    }
}

