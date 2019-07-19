/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkTaskPriorityQueue;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkTaskPriorityQueueSorter
implements AutoCloseable,
ChunkHolder.LevelChangeListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<ProcessorHandle<?>, ChunkTaskPriorityQueue<? extends Function<ProcessorHandle<Unit>, ?>>> queues;
    private final Set<ProcessorHandle<?>> sleeping;
    private final ProcessorMailbox<StrictQueue.IntRunnable> mailbox;

    public ChunkTaskPriorityQueueSorter(List<ProcessorHandle<?>> list, Executor executor, int i) {
        this.queues = list.stream().collect(Collectors.toMap(Function.identity(), processorHandle -> new ChunkTaskPriorityQueue(processorHandle.name() + "_queue", i)));
        this.sleeping = Sets.newHashSet(list);
        this.mailbox = new ProcessorMailbox<StrictQueue.IntRunnable>(new StrictQueue.FixedPriorityQueue(4), executor, "sorter");
    }

    public static Message<Runnable> message(Runnable runnable, long l, IntSupplier intSupplier) {
        return new Message<Runnable>(processorHandle -> () -> {
            runnable.run();
            processorHandle.tell(Unit.INSTANCE);
        }, l, intSupplier);
    }

    public static Message<Runnable> message(ChunkHolder chunkHolder, Runnable runnable) {
        return ChunkTaskPriorityQueueSorter.message(runnable, chunkHolder.getPos().toLong(), chunkHolder::getQueueLevel);
    }

    public static Release release(Runnable runnable, long l, boolean bl) {
        return new Release(runnable, l, bl);
    }

    public <T> ProcessorHandle<Message<T>> getProcessor(ProcessorHandle<T> processorHandle, boolean bl) {
        return (ProcessorHandle)this.mailbox.ask(processorHandle2 -> new StrictQueue.IntRunnable(0, () -> {
            this.getQueue(processorHandle);
            processorHandle2.tell(ProcessorHandle.of("chunk priority sorter around " + processorHandle.name(), message -> this.submit(processorHandle, ((Message)message).task, ((Message)message).pos, ((Message)message).level, bl)));
        })).join();
    }

    public ProcessorHandle<Release> getReleaseProcessor(ProcessorHandle<Runnable> processorHandle) {
        return (ProcessorHandle)this.mailbox.ask(processorHandle2 -> new StrictQueue.IntRunnable(0, () -> processorHandle2.tell(ProcessorHandle.of("chunk priority sorter around " + processorHandle.name(), release -> this.release(processorHandle, ((Release)release).pos, ((Release)release).task, ((Release)release).clearQueue))))).join();
    }

    @Override
    public void onLevelChange(ChunkPos chunkPos, IntSupplier intSupplier, int i, IntConsumer intConsumer) {
        this.mailbox.tell(new StrictQueue.IntRunnable(0, () -> {
            int j = intSupplier.getAsInt();
            this.queues.values().forEach(chunkTaskPriorityQueue -> chunkTaskPriorityQueue.resortChunkTasks(j, chunkPos, i));
            intConsumer.accept(i);
        }));
    }

    private <T> void release(ProcessorHandle<T> processorHandle, long l, Runnable runnable, boolean bl) {
        this.mailbox.tell(new StrictQueue.IntRunnable(1, () -> {
            ChunkTaskPriorityQueue chunkTaskPriorityQueue = this.getQueue(processorHandle);
            chunkTaskPriorityQueue.release(l, bl);
            if (this.sleeping.remove(processorHandle)) {
                this.pollTask(chunkTaskPriorityQueue, processorHandle);
            }
            runnable.run();
        }));
    }

    private <T> void submit(ProcessorHandle<T> processorHandle, Function<ProcessorHandle<Unit>, T> function, long l, IntSupplier intSupplier, boolean bl) {
        this.mailbox.tell(new StrictQueue.IntRunnable(2, () -> {
            ChunkTaskPriorityQueue chunkTaskPriorityQueue = this.getQueue(processorHandle);
            int i = intSupplier.getAsInt();
            chunkTaskPriorityQueue.submit(Optional.of(function), l, i);
            if (bl) {
                chunkTaskPriorityQueue.submit(Optional.empty(), l, i);
            }
            if (this.sleeping.remove(processorHandle)) {
                this.pollTask(chunkTaskPriorityQueue, processorHandle);
            }
        }));
    }

    private <T> void pollTask(ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> chunkTaskPriorityQueue, ProcessorHandle<T> processorHandle) {
        this.mailbox.tell(new StrictQueue.IntRunnable(3, () -> {
            Stream<Either<Either, Runnable>> stream = chunkTaskPriorityQueue.pop();
            if (stream == null) {
                this.sleeping.add(processorHandle);
            } else {
                Util.sequence(stream.map(either -> either.map(processorHandle::ask, runnable -> {
                    runnable.run();
                    return CompletableFuture.completedFuture(Unit.INSTANCE);
                })).collect(Collectors.toList())).thenAccept(list -> this.pollTask(chunkTaskPriorityQueue, processorHandle));
            }
        }));
    }

    private <T> ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> getQueue(ProcessorHandle<T> processorHandle) {
        ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> chunkTaskPriorityQueue = this.queues.get(processorHandle);
        if (chunkTaskPriorityQueue == null) {
            throw new IllegalArgumentException("No queue for: " + processorHandle);
        }
        return chunkTaskPriorityQueue;
    }

    @VisibleForTesting
    public String getDebugStatus() {
        return this.queues.entrySet().stream().map(entry -> ((ProcessorHandle)entry.getKey()).name() + "=[" + ((ChunkTaskPriorityQueue)entry.getValue()).getAcquired().stream().map(long_ -> long_ + ":" + new ChunkPos((long)long_)).collect(Collectors.joining(",")) + "]").collect(Collectors.joining(",")) + ", s=" + this.sleeping.size();
    }

    @Override
    public void close() {
        this.queues.keySet().forEach(ProcessorHandle::close);
    }

    public static final class Release {
        private final Runnable task;
        private final long pos;
        private final boolean clearQueue;

        private Release(Runnable runnable, long l, boolean bl) {
            this.task = runnable;
            this.pos = l;
            this.clearQueue = bl;
        }
    }

    public static final class Message<T> {
        private final Function<ProcessorHandle<Unit>, T> task;
        private final long pos;
        private final IntSupplier level;

        private Message(Function<ProcessorHandle<Unit>, T> function, long l, IntSupplier intSupplier) {
            this.task = function;
            this.pos = l;
            this.level = intSupplier;
        }
    }
}

