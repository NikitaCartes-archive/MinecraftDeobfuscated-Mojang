/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import com.google.common.base.Stopwatch;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.Util;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProfiledReloadInstance
extends SimpleReloadInstance<State> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Stopwatch total = Stopwatch.createUnstarted();

    public ProfiledReloadInstance(ResourceManager resourceManager2, List<PreparableReloadListener> list, Executor executor, Executor executor22, CompletableFuture<Unit> completableFuture) {
        super(executor, executor22, resourceManager2, list, (preparationBarrier, resourceManager, preparableReloadListener, executor2, executor3) -> {
            AtomicLong atomicLong = new AtomicLong();
            AtomicLong atomicLong2 = new AtomicLong();
            ActiveProfiler activeProfiler = new ActiveProfiler(Util.timeSource, () -> 0, false);
            ActiveProfiler activeProfiler2 = new ActiveProfiler(Util.timeSource, () -> 0, false);
            CompletableFuture<Void> completableFuture = preparableReloadListener.reload(preparationBarrier, resourceManager, activeProfiler, activeProfiler2, runnable -> executor2.execute(() -> {
                long l = Util.getNanos();
                runnable.run();
                atomicLong.addAndGet(Util.getNanos() - l);
            }), runnable -> executor3.execute(() -> {
                long l = Util.getNanos();
                runnable.run();
                atomicLong2.addAndGet(Util.getNanos() - l);
            }));
            return completableFuture.thenApplyAsync(void_ -> {
                LOGGER.debug("Finished reloading " + preparableReloadListener.getName());
                return new State(preparableReloadListener.getName(), activeProfiler.getResults(), activeProfiler2.getResults(), atomicLong, atomicLong2);
            }, executor22);
        }, completableFuture);
        this.total.start();
        this.allDone.thenAcceptAsync(this::finish, executor22);
    }

    private void finish(List<State> list) {
        this.total.stop();
        int i = 0;
        LOGGER.info("Resource reload finished after {} ms", (Object)this.total.elapsed(TimeUnit.MILLISECONDS));
        for (State state : list) {
            ProfileResults profileResults = state.preparationResult;
            ProfileResults profileResults2 = state.reloadResult;
            int j = (int)((double)state.preparationNanos.get() / 1000000.0);
            int k = (int)((double)state.reloadNanos.get() / 1000000.0);
            int l = j + k;
            String string = state.name;
            LOGGER.info("{} took approximately {} ms ({} ms preparing, {} ms applying)", (Object)string, (Object)l, (Object)j, (Object)k);
            i += k;
        }
        LOGGER.info("Total blocking time: {} ms", (Object)i);
    }

    public static class State {
        final String name;
        final ProfileResults preparationResult;
        final ProfileResults reloadResult;
        final AtomicLong preparationNanos;
        final AtomicLong reloadNanos;

        State(String string, ProfileResults profileResults, ProfileResults profileResults2, AtomicLong atomicLong, AtomicLong atomicLong2) {
            this.name = string;
            this.preparationResult = profileResults;
            this.reloadResult = profileResults2;
            this.preparationNanos = atomicLong;
            this.reloadNanos = atomicLong2;
        }
    }
}

