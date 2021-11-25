/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.thread;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsRegistry;
import net.minecraft.util.profiling.metrics.ProfilerMeasured;
import net.minecraft.util.thread.ProcessorHandle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BlockableEventLoop<R extends Runnable>
implements ProfilerMeasured,
ProcessorHandle<R>,
Executor {
    private final String name;
    private static final Logger LOGGER = LogManager.getLogger();
    private final Queue<R> pendingRunnables = Queues.newConcurrentLinkedQueue();
    private int blockingCount;

    protected BlockableEventLoop(String string) {
        this.name = string;
        MetricsRegistry.INSTANCE.add(this);
    }

    protected abstract R wrapRunnable(Runnable var1);

    protected abstract boolean shouldRun(R var1);

    public boolean isSameThread() {
        return Thread.currentThread() == this.getRunningThread();
    }

    protected abstract Thread getRunningThread();

    protected boolean scheduleExecutables() {
        return !this.isSameThread();
    }

    public int getPendingTasksCount() {
        return this.pendingRunnables.size();
    }

    @Override
    public String name() {
        return this.name;
    }

    public <V> CompletableFuture<V> submit(Supplier<V> supplier) {
        if (this.scheduleExecutables()) {
            return CompletableFuture.supplyAsync(supplier, this);
        }
        return CompletableFuture.completedFuture(supplier.get());
    }

    private CompletableFuture<Void> submitAsync(Runnable runnable) {
        return CompletableFuture.supplyAsync(() -> {
            runnable.run();
            return null;
        }, this);
    }

    public CompletableFuture<Void> submit(Runnable runnable) {
        if (this.scheduleExecutables()) {
            return this.submitAsync(runnable);
        }
        runnable.run();
        return CompletableFuture.completedFuture(null);
    }

    public void executeBlocking(Runnable runnable) {
        if (!this.isSameThread()) {
            this.submitAsync(runnable).join();
        } else {
            runnable.run();
        }
    }

    @Override
    public void tell(R runnable) {
        this.pendingRunnables.add(runnable);
        LockSupport.unpark(this.getRunningThread());
    }

    @Override
    public void execute(Runnable runnable) {
        if (this.scheduleExecutables()) {
            this.tell(this.wrapRunnable(runnable));
        } else {
            runnable.run();
        }
    }

    protected void dropAllTasks() {
        this.pendingRunnables.clear();
    }

    protected void runAllTasks() {
        while (this.pollTask()) {
        }
    }

    public boolean pollTask() {
        Runnable runnable = (Runnable)this.pendingRunnables.peek();
        if (runnable == null) {
            return false;
        }
        if (this.blockingCount == 0 && !this.shouldRun(runnable)) {
            return false;
        }
        this.doRunTask((Runnable)this.pendingRunnables.remove());
        return true;
    }

    public void managedBlock(BooleanSupplier booleanSupplier) {
        ++this.blockingCount;
        try {
            while (!booleanSupplier.getAsBoolean()) {
                if (this.pollTask()) continue;
                this.waitForTasks();
            }
        } finally {
            --this.blockingCount;
        }
    }

    protected void waitForTasks() {
        Thread.yield();
        LockSupport.parkNanos("waiting for tasks", 100000L);
    }

    protected void doRunTask(R runnable) {
        try {
            runnable.run();
        } catch (Exception exception) {
            LOGGER.fatal("Error executing task on {}", (Object)this.name(), (Object)exception);
        }
    }

    @Override
    public List<MetricSampler> profiledMetrics() {
        return ImmutableList.of(MetricSampler.create(this.name + "-pending-tasks", MetricCategory.EVENT_LOOPS, this::getPendingTasksCount));
    }

    @Override
    public /* synthetic */ void tell(Object object) {
        this.tell((R)((Runnable)object));
    }
}

