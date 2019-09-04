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
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkTaskPriorityQueueSorter implements AutoCloseable, ChunkHolder.LevelChangeListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Map<ProcessorHandle<?>, ChunkTaskPriorityQueue<? extends Function<ProcessorHandle<Unit>, ?>>> queues;
	private final Set<ProcessorHandle<?>> sleeping;
	private final ProcessorMailbox<StrictQueue.IntRunnable> mailbox;

	public ChunkTaskPriorityQueueSorter(List<ProcessorHandle<?>> list, Executor executor, int i) {
		this.queues = (Map<ProcessorHandle<?>, ChunkTaskPriorityQueue<? extends Function<ProcessorHandle<Unit>, ?>>>)list.stream()
			.collect(Collectors.toMap(Function.identity(), processorHandle -> new ChunkTaskPriorityQueue(processorHandle.name() + "_queue", i)));
		this.sleeping = Sets.<ProcessorHandle<?>>newHashSet(list);
		this.mailbox = new ProcessorMailbox<>(new StrictQueue.FixedPriorityQueue(4), executor, "sorter");
	}

	public static ChunkTaskPriorityQueueSorter.Message<Runnable> message(Runnable runnable, long l, IntSupplier intSupplier) {
		return new ChunkTaskPriorityQueueSorter.Message<>(processorHandle -> () -> {
				runnable.run();
				processorHandle.tell(Unit.INSTANCE);
			}, l, intSupplier);
	}

	public static ChunkTaskPriorityQueueSorter.Message<Runnable> message(ChunkHolder chunkHolder, Runnable runnable) {
		return message(runnable, chunkHolder.getPos().toLong(), chunkHolder::getQueueLevel);
	}

	public static ChunkTaskPriorityQueueSorter.Release release(Runnable runnable, long l, boolean bl) {
		return new ChunkTaskPriorityQueueSorter.Release(runnable, l, bl);
	}

	public <T> ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<T>> getProcessor(ProcessorHandle<T> processorHandle, boolean bl) {
		return (ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<T>>)this.mailbox
			.ask(
				processorHandle2 -> new StrictQueue.IntRunnable(
						0,
						() -> {
							this.getQueue(processorHandle);
							processorHandle2.tell(
								ProcessorHandle.of(
									"chunk priority sorter around " + processorHandle.name(), message -> this.submit(processorHandle, message.task, message.pos, message.level, bl)
								)
							);
						}
					)
			)
			.join();
	}

	public ProcessorHandle<ChunkTaskPriorityQueueSorter.Release> getReleaseProcessor(ProcessorHandle<Runnable> processorHandle) {
		return (ProcessorHandle<ChunkTaskPriorityQueueSorter.Release>)this.mailbox
			.ask(
				processorHandle2 -> new StrictQueue.IntRunnable(
						0,
						() -> processorHandle2.tell(
								ProcessorHandle.of(
									"chunk priority sorter around " + processorHandle.name(), release -> this.release(processorHandle, release.pos, release.task, release.clearQueue)
								)
							)
					)
			)
			.join();
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
			ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> chunkTaskPriorityQueue = this.getQueue(processorHandle);
			chunkTaskPriorityQueue.release(l, bl);
			if (this.sleeping.remove(processorHandle)) {
				this.pollTask(chunkTaskPriorityQueue, processorHandle);
			}

			runnable.run();
		}));
	}

	private <T> void submit(ProcessorHandle<T> processorHandle, Function<ProcessorHandle<Unit>, T> function, long l, IntSupplier intSupplier, boolean bl) {
		this.mailbox.tell(new StrictQueue.IntRunnable(2, () -> {
			ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> chunkTaskPriorityQueue = this.getQueue(processorHandle);
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
			Stream<Either<Function<ProcessorHandle<Unit>, T>, Runnable>> stream = chunkTaskPriorityQueue.pop();
			if (stream == null) {
				this.sleeping.add(processorHandle);
			} else {
				Util.sequence((List)stream.map(either -> either.map(processorHandle::ask, runnable -> {
						runnable.run();
						return CompletableFuture.completedFuture(Unit.INSTANCE);
					})).collect(Collectors.toList())).thenAccept(list -> this.pollTask(chunkTaskPriorityQueue, processorHandle));
			}
		}));
	}

	private <T> ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> getQueue(ProcessorHandle<T> processorHandle) {
		ChunkTaskPriorityQueue<? extends Function<ProcessorHandle<Unit>, ?>> chunkTaskPriorityQueue = (ChunkTaskPriorityQueue<? extends Function<ProcessorHandle<Unit>, ?>>)this.queues
			.get(processorHandle);
		if (chunkTaskPriorityQueue == null) {
			throw (IllegalArgumentException)Util.pauseInIde((T)(new IllegalArgumentException("No queue for: " + processorHandle)));
		} else {
			return (ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>>)chunkTaskPriorityQueue;
		}
	}

	@VisibleForTesting
	public String getDebugStatus() {
		return (String)this.queues
				.entrySet()
				.stream()
				.map(
					entry -> ((ProcessorHandle)entry.getKey()).name()
							+ "=["
							+ (String)((ChunkTaskPriorityQueue)entry.getValue())
								.getAcquired()
								.stream()
								.map(long_ -> long_ + ":" + new ChunkPos(long_))
								.collect(Collectors.joining(","))
							+ "]"
				)
				.collect(Collectors.joining(","))
			+ ", s="
			+ this.sleeping.size();
	}

	public void close() {
		this.queues.keySet().forEach(ProcessorHandle::close);
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
}
