/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.InactiveProfiler;

public class SimpleReloadInstance<S>
implements ReloadInstance {
    protected final ResourceManager resourceManager;
    protected final CompletableFuture<Unit> allPreparations = new CompletableFuture();
    protected final CompletableFuture<List<S>> allDone;
    private final Set<PreparableReloadListener> preparingListeners;
    private final int listenerCount;
    private int startedReloads;
    private int finishedReloads;
    private final AtomicInteger startedTaskCounter = new AtomicInteger();
    private final AtomicInteger doneTaskCounter = new AtomicInteger();

    public static SimpleReloadInstance<Void> of(ResourceManager resourceManager2, List<PreparableReloadListener> list, Executor executor, Executor executor22, CompletableFuture<Unit> completableFuture) {
        return new SimpleReloadInstance<Void>(executor, executor22, resourceManager2, list, (preparationBarrier, resourceManager, preparableReloadListener, executor2, executor3) -> preparableReloadListener.reload(preparationBarrier, resourceManager, InactiveProfiler.INACTIVE, InactiveProfiler.INACTIVE, executor, executor3), completableFuture);
    }

    protected SimpleReloadInstance(Executor executor, final Executor executor2, ResourceManager resourceManager, List<PreparableReloadListener> list, StateFactory<S> stateFactory, CompletableFuture<Unit> completableFuture) {
        this.resourceManager = resourceManager;
        this.listenerCount = list.size();
        this.startedTaskCounter.incrementAndGet();
        completableFuture.thenRun(this.doneTaskCounter::incrementAndGet);
        ArrayList<CompletableFuture<S>> list2 = new ArrayList<CompletableFuture<S>>();
        CompletableFuture<Unit> completableFuture2 = completableFuture;
        this.preparingListeners = Sets.newHashSet(list);
        for (final PreparableReloadListener preparableReloadListener : list) {
            final CompletableFuture<Unit> completableFuture3 = completableFuture2;
            CompletableFuture<S> completableFuture4 = stateFactory.create(new PreparableReloadListener.PreparationBarrier(){

                @Override
                public <T> CompletableFuture<T> wait(T object) {
                    executor2.execute(() -> {
                        SimpleReloadInstance.this.preparingListeners.remove(preparableReloadListener);
                        if (SimpleReloadInstance.this.preparingListeners.isEmpty()) {
                            SimpleReloadInstance.this.allPreparations.complete(Unit.INSTANCE);
                        }
                    });
                    return SimpleReloadInstance.this.allPreparations.thenCombine((CompletionStage)completableFuture3, (unit, object2) -> object);
                }
            }, resourceManager, preparableReloadListener, runnable -> {
                this.startedTaskCounter.incrementAndGet();
                executor.execute(() -> {
                    runnable.run();
                    this.doneTaskCounter.incrementAndGet();
                });
            }, runnable -> {
                ++this.startedReloads;
                executor2.execute(() -> {
                    runnable.run();
                    ++this.finishedReloads;
                });
            });
            list2.add(completableFuture4);
            completableFuture2 = completableFuture4;
        }
        this.allDone = Util.sequence(list2);
    }

    @Override
    public CompletableFuture<Unit> done() {
        return this.allDone.thenApply(list -> Unit.INSTANCE);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public float getActualProgress() {
        int i = this.listenerCount - this.preparingListeners.size();
        float f = this.doneTaskCounter.get() * 2 + this.finishedReloads * 2 + i * 1;
        float g = this.startedTaskCounter.get() * 2 + this.startedReloads * 2 + this.listenerCount * 1;
        return f / g;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean isApplying() {
        return this.allPreparations.isDone();
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean isDone() {
        return this.allDone.isDone();
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void checkExceptions() {
        if (this.allDone.isCompletedExceptionally()) {
            this.allDone.join();
        }
    }

    public static interface StateFactory<S> {
        public CompletableFuture<S> create(PreparableReloadListener.PreparationBarrier var1, ResourceManager var2, PreparableReloadListener var3, Executor var4, Executor var5);
    }
}

