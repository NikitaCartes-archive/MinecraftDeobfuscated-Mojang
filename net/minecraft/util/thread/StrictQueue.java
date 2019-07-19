/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jetbrains.annotations.Nullable;

public interface StrictQueue<T, F> {
    @Nullable
    public F pop();

    public boolean push(T var1);

    public boolean isEmpty();

    public static final class FixedPriorityQueue
    implements StrictQueue<IntRunnable, Runnable> {
        private final List<Queue<Runnable>> queueList;

        public FixedPriorityQueue(int i2) {
            this.queueList = IntStream.range(0, i2).mapToObj(i -> Queues.newConcurrentLinkedQueue()).collect(Collectors.toList());
        }

        @Override
        @Nullable
        public Runnable pop() {
            for (Queue<Runnable> queue : this.queueList) {
                Runnable runnable = queue.poll();
                if (runnable == null) continue;
                return runnable;
            }
            return null;
        }

        @Override
        public boolean push(IntRunnable intRunnable) {
            int i = intRunnable.getPriority();
            this.queueList.get(i).add(intRunnable);
            return true;
        }

        @Override
        public boolean isEmpty() {
            return this.queueList.stream().allMatch(Collection::isEmpty);
        }

        @Override
        @Nullable
        public /* synthetic */ Object pop() {
            return this.pop();
        }
    }

    public static final class IntRunnable
    implements Runnable {
        private final int priority;
        private final Runnable task;

        public IntRunnable(int i, Runnable runnable) {
            this.priority = i;
            this.task = runnable;
        }

        @Override
        public void run() {
            this.task.run();
        }

        public int getPriority() {
            return this.priority;
        }
    }

    public static final class QueueStrictQueue<T>
    implements StrictQueue<T, T> {
        private final Queue<T> queue;

        public QueueStrictQueue(Queue<T> queue) {
            this.queue = queue;
        }

        @Override
        @Nullable
        public T pop() {
            return this.queue.poll();
        }

        @Override
        public boolean push(T object) {
            return this.queue.add(object);
        }

        @Override
        public boolean isEmpty() {
            return this.queue.isEmpty();
        }
    }
}

