/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.task;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.gui.task.RepeatedDelayStrategy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.TimeSource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class DataFetcher {
    static final Logger LOGGER = LogUtils.getLogger();
    final Executor executor;
    final TimeUnit resolution;
    final TimeSource timeSource;

    public DataFetcher(Executor executor, TimeUnit timeUnit, TimeSource timeSource) {
        this.executor = executor;
        this.resolution = timeUnit;
        this.timeSource = timeSource;
    }

    public <T> Task<T> createTask(String string, Callable<T> callable, Duration duration, RepeatedDelayStrategy repeatedDelayStrategy) {
        long l = this.resolution.convert(duration);
        if (l == 0L) {
            throw new IllegalArgumentException("Period of " + duration + " too short for selected resolution of " + this.resolution);
        }
        return new Task<T>(string, callable, l, repeatedDelayStrategy);
    }

    public Subscription createSubscription() {
        return new Subscription();
    }

    @Environment(value=EnvType.CLIENT)
    public class Task<T> {
        private final String id;
        private final Callable<T> updater;
        private final long period;
        private final RepeatedDelayStrategy repeatStrategy;
        @Nullable
        private CompletableFuture<ComputationResult<T>> pendingTask;
        @Nullable
        SuccessfulComputationResult<T> lastResult;
        private long nextUpdate = -1L;

        Task(String string, Callable<T> callable, long l, RepeatedDelayStrategy repeatedDelayStrategy) {
            this.id = string;
            this.updater = callable;
            this.period = l;
            this.repeatStrategy = repeatedDelayStrategy;
        }

        void updateIfNeeded(long l) {
            if (this.pendingTask != null) {
                ComputationResult computationResult = this.pendingTask.getNow(null);
                if (computationResult == null) {
                    return;
                }
                this.pendingTask = null;
                long m = computationResult.time;
                computationResult.value().ifLeft(object -> {
                    this.lastResult = new SuccessfulComputationResult<Object>(object, m);
                    this.nextUpdate = m + this.period * this.repeatStrategy.delayCyclesAfterSuccess();
                }).ifRight(exception -> {
                    long m = this.repeatStrategy.delayCyclesAfterFailure();
                    LOGGER.warn("Failed to process task {}, will repeat after {} cycles", this.id, m, exception);
                    this.nextUpdate = m + this.period * m;
                });
            }
            if (this.nextUpdate <= l) {
                this.pendingTask = CompletableFuture.supplyAsync(() -> {
                    try {
                        T object = this.updater.call();
                        long l = DataFetcher.this.timeSource.get(DataFetcher.this.resolution);
                        return new ComputationResult<T>(Either.left(object), l);
                    } catch (Exception exception) {
                        long l = DataFetcher.this.timeSource.get(DataFetcher.this.resolution);
                        return new ComputationResult(Either.right(exception), l);
                    }
                }, DataFetcher.this.executor);
            }
        }

        public void reset() {
            this.pendingTask = null;
            this.lastResult = null;
            this.nextUpdate = -1L;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class Subscription {
        private final List<SubscribedTask<?>> subscriptions = new ArrayList();

        public <T> void subscribe(Task<T> task, Consumer<T> consumer) {
            SubscribedTask<T> subscribedTask = new SubscribedTask<T>(task, consumer);
            this.subscriptions.add(subscribedTask);
            subscribedTask.runCallbackIfNeeded();
        }

        public void forceUpdate() {
            for (SubscribedTask<?> subscribedTask : this.subscriptions) {
                subscribedTask.runCallback();
            }
        }

        public void tick() {
            for (SubscribedTask<?> subscribedTask : this.subscriptions) {
                subscribedTask.update(DataFetcher.this.timeSource.get(DataFetcher.this.resolution));
            }
        }

        public void reset() {
            for (SubscribedTask<?> subscribedTask : this.subscriptions) {
                subscribedTask.reset();
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class SubscribedTask<T> {
        private final Task<T> task;
        private final Consumer<T> output;
        private long lastCheckTime = -1L;

        SubscribedTask(Task<T> task, Consumer<T> consumer) {
            this.task = task;
            this.output = consumer;
        }

        void update(long l) {
            this.task.updateIfNeeded(l);
            this.runCallbackIfNeeded();
        }

        void runCallbackIfNeeded() {
            SuccessfulComputationResult successfulComputationResult = this.task.lastResult;
            if (successfulComputationResult != null && this.lastCheckTime < successfulComputationResult.time) {
                this.output.accept(successfulComputationResult.value);
                this.lastCheckTime = successfulComputationResult.time;
            }
        }

        void runCallback() {
            SuccessfulComputationResult successfulComputationResult = this.task.lastResult;
            if (successfulComputationResult != null) {
                this.output.accept(successfulComputationResult.value);
                this.lastCheckTime = successfulComputationResult.time;
            }
        }

        void reset() {
            this.task.reset();
            this.lastCheckTime = -1L;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record SuccessfulComputationResult<T>(T value, long time) {
    }

    @Environment(value=EnvType.CLIENT)
    record ComputationResult<T>(Either<T, Exception> value, long time) {
    }
}

