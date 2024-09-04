package net.minecraft.util.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.minecraft.util.profiling.metrics.MetricsRegistry;

public class PriorityConsecutiveExecutor extends AbstractConsecutiveExecutor<StrictQueue.RunnableWithPriority> {
	public PriorityConsecutiveExecutor(int i, Executor executor, String string) {
		super(new StrictQueue.FixedPriorityQueue(i), executor, string);
		MetricsRegistry.INSTANCE.add(this);
	}

	public StrictQueue.RunnableWithPriority wrapRunnable(Runnable runnable) {
		return new StrictQueue.RunnableWithPriority(0, runnable);
	}

	public <Source> CompletableFuture<Source> scheduleWithResult(int i, Consumer<CompletableFuture<Source>> consumer) {
		CompletableFuture<Source> completableFuture = new CompletableFuture();
		this.schedule(new StrictQueue.RunnableWithPriority(i, () -> consumer.accept(completableFuture)));
		return completableFuture;
	}
}
