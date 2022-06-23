package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;

public interface StrictQueue<T, F> {
	@Nullable
	F pop();

	boolean push(T object);

	boolean isEmpty();

	int size();

	public static final class FixedPriorityQueue implements StrictQueue<StrictQueue.IntRunnable, Runnable> {
		private final Queue<Runnable>[] queues;
		private final AtomicInteger size = new AtomicInteger();

		public FixedPriorityQueue(int i) {
			this.queues = new Queue[i];

			for (int j = 0; j < i; j++) {
				this.queues[j] = Queues.<Runnable>newConcurrentLinkedQueue();
			}
		}

		@Nullable
		public Runnable pop() {
			for (Queue<Runnable> queue : this.queues) {
				Runnable runnable = (Runnable)queue.poll();
				if (runnable != null) {
					this.size.decrementAndGet();
					return runnable;
				}
			}

			return null;
		}

		public boolean push(StrictQueue.IntRunnable intRunnable) {
			int i = intRunnable.priority;
			if (i < this.queues.length && i >= 0) {
				this.queues[i].add(intRunnable);
				this.size.incrementAndGet();
				return true;
			} else {
				throw new IndexOutOfBoundsException("Priority %d not supported. Expected range [0-%d]".formatted(i, this.queues.length - 1));
			}
		}

		@Override
		public boolean isEmpty() {
			return this.size.get() == 0;
		}

		@Override
		public int size() {
			return this.size.get();
		}
	}

	public static final class IntRunnable implements Runnable {
		final int priority;
		private final Runnable task;

		public IntRunnable(int i, Runnable runnable) {
			this.priority = i;
			this.task = runnable;
		}

		public void run() {
			this.task.run();
		}

		public int getPriority() {
			return this.priority;
		}
	}

	public static final class QueueStrictQueue<T> implements StrictQueue<T, T> {
		private final Queue<T> queue;

		public QueueStrictQueue(Queue<T> queue) {
			this.queue = queue;
		}

		@Nullable
		@Override
		public T pop() {
			return (T)this.queue.poll();
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
