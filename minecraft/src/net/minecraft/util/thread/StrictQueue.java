package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;

public interface StrictQueue<T, F> {
	@Nullable
	F pop();

	boolean push(T object);

	boolean isEmpty();

	public static final class FixedPriorityQueue implements StrictQueue<StrictQueue.IntRunnable, Runnable> {
		private final List<Queue<Runnable>> queueList;

		public FixedPriorityQueue(int i) {
			this.queueList = (List<Queue<Runnable>>)IntStream.range(0, i).mapToObj(ix -> Queues.newConcurrentLinkedQueue()).collect(Collectors.toList());
		}

		@Nullable
		public Runnable pop() {
			for (Queue<Runnable> queue : this.queueList) {
				Runnable runnable = (Runnable)queue.poll();
				if (runnable != null) {
					return runnable;
				}
			}

			return null;
		}

		public boolean push(StrictQueue.IntRunnable intRunnable) {
			int i = intRunnable.getPriority();
			((Queue)this.queueList.get(i)).add(intRunnable);
			return true;
		}

		@Override
		public boolean isEmpty() {
			return this.queueList.stream().allMatch(Collection::isEmpty);
		}
	}

	public static final class IntRunnable implements Runnable {
		private final int priority;
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
	}
}
