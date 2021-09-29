/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.Nullable;

public interface StrictQueue<T, F> {
    @Nullable
    public F pop();

    public boolean push(T var1);

    public boolean isEmpty();

    public int size();

    public static final class FixedPriorityQueue
    implements StrictQueue<IntRunnable, Runnable> {
        private final Queue<Runnable>[] queues;
        private final AtomicInteger size = new AtomicInteger();

        public FixedPriorityQueue(int i) {
            this.queues = new Queue[i];
            for (int j = 0; j < i; ++j) {
                this.queues[j] = Queues.newConcurrentLinkedQueue();
            }
        }

        @Override
        @Nullable
        public Runnable pop() {
            for (Queue<Runnable> queue : this.queues) {
                Runnable runnable = queue.poll();
                if (runnable == null) continue;
                this.size.decrementAndGet();
                return runnable;
            }
            return null;
        }

        @Override
        public boolean push(IntRunnable intRunnable) {
            int i = intRunnable.priority;
            if (i >= this.queues.length || i < 0) {
                throw new IndexOutOfBoundsException("Priority %d not supported. Expected range [0-%d]".formatted(i, this.queues.length - 1));
            }
            this.queues[i].add(intRunnable);
            this.size.incrementAndGet();
            return true;
        }

        @Override
        public boolean isEmpty() {
            return this.size.get() == 0;
        }

        @Override
        public int size() {
            return this.size.get();
        }

        @Override
        @Nullable
        public /* synthetic */ Object pop() {
            return this.pop();
        }
    }

    public static final class IntRunnable
    implements Runnable {
        final int priority;
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

        @Override
        public int size() {
            return this.queue.size();
        }
    }
}

