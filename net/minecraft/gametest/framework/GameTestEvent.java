/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import org.jetbrains.annotations.Nullable;

class GameTestEvent {
    @Nullable
    public final Long expectedDelay;
    public final Runnable assertion;

    private GameTestEvent(@Nullable Long long_, Runnable runnable) {
        this.expectedDelay = long_;
        this.assertion = runnable;
    }

    static GameTestEvent create(Runnable runnable) {
        return new GameTestEvent(null, runnable);
    }

    static GameTestEvent create(long l, Runnable runnable) {
        return new GameTestEvent(l, runnable);
    }
}

