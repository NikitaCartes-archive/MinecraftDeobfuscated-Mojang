/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.thread;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.Util;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsRegistry;
import net.minecraft.util.profiling.metrics.ProfilerMeasured;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.StrictQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProcessorMailbox<T>
implements ProfilerMeasured,
ProcessorHandle<T>,
AutoCloseable,
Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int CLOSED_BIT = 1;
    private static final int SCHEDULED_BIT = 2;
    private final AtomicInteger status = new AtomicInteger(0);
    private final StrictQueue<? super T, ? extends Runnable> queue;
    private final Executor dispatcher;
    private final String name;

    public static ProcessorMailbox<Runnable> create(Executor executor, String string) {
        return new ProcessorMailbox<Runnable>(new StrictQueue.QueueStrictQueue(new ConcurrentLinkedQueue()), executor, string);
    }

    public ProcessorMailbox(StrictQueue<? super T, ? extends Runnable> strictQueue, Executor executor, String string) {
        this.dispatcher = executor;
        this.queue = strictQueue;
        this.name = string;
        MetricsRegistry.INSTANCE.add(this);
    }

    private boolean setAsScheduled() {
        int i;
        do {
            if (((i = this.status.get()) & 3) == 0) continue;
            return false;
        } while (!this.status.compareAndSet(i, i | 2));
        return true;
    }

    private void setAsIdle() {
        int i;
        while (!this.status.compareAndSet(i = this.status.get(), i & 0xFFFFFFFD)) {
        }
    }

    private boolean canBeScheduled() {
        if ((this.status.get() & 1) != 0) {
            return false;
        }
        return !this.queue.isEmpty();
    }

    @Override
    public void close() {
        int i;
        while (!this.status.compareAndSet(i = this.status.get(), i | 1)) {
        }
    }

    private boolean shouldProcess() {
        return (this.status.get() & 2) != 0;
    }

    private boolean pollTask() {
        if (!this.shouldProcess()) {
            return false;
        }
        Runnable runnable = this.queue.pop();
        if (runnable == null) {
            return false;
        }
        Util.wrapThreadWithTaskName(this.name, runnable).run();
        return true;
    }

    @Override
    public void run() {
        try {
            this.pollUntil(i -> i == 0);
        } finally {
            this.setAsIdle();
            this.registerForExecution();
        }
    }

    public void runAll() {
        try {
            this.pollUntil(i -> true);
        } finally {
            this.setAsIdle();
            this.registerForExecution();
        }
    }

    @Override
    public void tell(T object) {
        this.queue.push(object);
        this.registerForExecution();
    }

    private void registerForExecution() {
        if (this.canBeScheduled() && this.setAsScheduled()) {
            try {
                this.dispatcher.execute(this);
            } catch (RejectedExecutionException rejectedExecutionException) {
                try {
                    this.dispatcher.execute(this);
                } catch (RejectedExecutionException rejectedExecutionException2) {
                    LOGGER.error("Cound not schedule mailbox", (Throwable)rejectedExecutionException2);
                }
            }
        }
    }

    private int pollUntil(Int2BooleanFunction int2BooleanFunction) {
        int i = 0;
        while (int2BooleanFunction.get(i) && this.pollTask()) {
            ++i;
        }
        return i;
    }

    public int size() {
        return this.queue.size();
    }

    public String toString() {
        return this.name + " " + this.status.get() + " " + this.queue.isEmpty();
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public List<MetricSampler> profiledMetrics() {
        return ImmutableList.of(MetricSampler.create(this.name + "-queue-size", MetricCategory.MAIL_BOXES, this::size));
    }
}

