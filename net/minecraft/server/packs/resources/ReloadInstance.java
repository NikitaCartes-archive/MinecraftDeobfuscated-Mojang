/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Unit;

public interface ReloadInstance {
    public CompletableFuture<Unit> done();

    @Environment(value=EnvType.CLIENT)
    public float getActualProgress();

    @Environment(value=EnvType.CLIENT)
    public boolean isApplying();

    @Environment(value=EnvType.CLIENT)
    public boolean isDone();

    @Environment(value=EnvType.CLIENT)
    public void checkExceptions();
}

