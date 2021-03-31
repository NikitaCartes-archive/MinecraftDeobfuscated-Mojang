/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import net.minecraft.util.Unit;

public interface ReloadInstance {
    public CompletableFuture<Unit> done();

    public float getActualProgress();

    public boolean isDone();

    public void checkExceptions();
}

