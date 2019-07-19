/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

public abstract class SimplePreparableReloadListener<T>
implements PreparableReloadListener {
    @Override
    public final CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
        return ((CompletableFuture)CompletableFuture.supplyAsync(() -> this.prepare(resourceManager, profilerFiller), executor).thenCompose(preparationBarrier::wait)).thenAcceptAsync(object -> this.apply(object, resourceManager, profilerFiller2), executor2);
    }

    protected abstract T prepare(ResourceManager var1, ProfilerFiller var2);

    protected abstract void apply(T var1, ResourceManager var2, ProfilerFiller var3);
}

