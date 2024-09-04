package net.minecraft.util.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public interface TaskScheduler<R extends Runnable> extends AutoCloseable {
	String name();

	void schedule(R runnable);

	default void close() {
	}

	R wrapRunnable(Runnable runnable);

	default <Source> CompletableFuture<Source> scheduleWithResult(Consumer<CompletableFuture<Source>> consumer) {
		CompletableFuture<Source> completableFuture = new CompletableFuture();
		this.schedule(this.wrapRunnable(() -> consumer.accept(completableFuture)));
		return completableFuture;
	}

	static TaskScheduler<Runnable> wrapExecutor(String string, Executor executor) {
		return new TaskScheduler<Runnable>() {
			@Override
			public String name() {
				return string;
			}

			@Override
			public void schedule(Runnable runnable) {
				executor.execute(runnable);
			}

			@Override
			public Runnable wrapRunnable(Runnable runnable) {
				return runnable;
			}

			public String toString() {
				return string;
			}
		};
	}
}
