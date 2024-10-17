package net.minecraft.util.thread;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.CheckReturnValue;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsRegistry;
import net.minecraft.util.profiling.metrics.ProfilerMeasured;
import org.slf4j.Logger;

public abstract class BlockableEventLoop<R extends Runnable> implements ProfilerMeasured, TaskScheduler<R>, Executor {
	public static final long BLOCK_TIME_NANOS = 100000L;
	private final String name;
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Queue<R> pendingRunnables = Queues.<R>newConcurrentLinkedQueue();
	private int blockingCount;

	protected BlockableEventLoop(String string) {
		this.name = string;
		MetricsRegistry.INSTANCE.add(this);
	}

	protected abstract boolean shouldRun(R runnable);

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
		return this.scheduleExecutables() ? CompletableFuture.supplyAsync(supplier, this) : CompletableFuture.completedFuture(supplier.get());
	}

	private CompletableFuture<Void> submitAsync(Runnable runnable) {
		return CompletableFuture.supplyAsync(() -> {
			runnable.run();
			return null;
		}, this);
	}

	@CheckReturnValue
	public CompletableFuture<Void> submit(Runnable runnable) {
		if (this.scheduleExecutables()) {
			return this.submitAsync(runnable);
		} else {
			runnable.run();
			return CompletableFuture.completedFuture(null);
		}
	}

	public void executeBlocking(Runnable runnable) {
		if (!this.isSameThread()) {
			this.submitAsync(runnable).join();
		} else {
			runnable.run();
		}
	}

	@Override
	public void schedule(R runnable) {
		this.pendingRunnables.add(runnable);
		LockSupport.unpark(this.getRunningThread());
	}

	public void execute(Runnable runnable) {
		if (this.scheduleExecutables()) {
			this.schedule(this.wrapRunnable(runnable));
		} else {
			runnable.run();
		}
	}

	public void executeIfPossible(Runnable runnable) {
		this.execute(runnable);
	}

	protected void dropAllTasks() {
		this.pendingRunnables.clear();
	}

	protected void runAllTasks() {
		while (this.pollTask()) {
		}
	}

	public boolean pollTask() {
		R runnable = (R)this.pendingRunnables.peek();
		if (runnable == null) {
			return false;
		} else if (this.blockingCount == 0 && !this.shouldRun(runnable)) {
			return false;
		} else {
			this.doRunTask((R)this.pendingRunnables.remove());
			return true;
		}
	}

	public void managedBlock(BooleanSupplier booleanSupplier) {
		this.blockingCount++;

		try {
			while (!booleanSupplier.getAsBoolean()) {
				if (!this.pollTask()) {
					this.waitForTasks();
				}
			}
		} finally {
			this.blockingCount--;
		}
	}

	protected void waitForTasks() {
		Thread.yield();
		LockSupport.parkNanos("waiting for tasks", 100000L);
	}

	protected void doRunTask(R runnable) {
		try (Zone zone = TracyClient.beginZone("Task", SharedConstants.IS_RUNNING_IN_IDE)) {
			runnable.run();
		} catch (Exception var7) {
			LOGGER.error(LogUtils.FATAL_MARKER, "Error executing task on {}", this.name(), var7);
			if (isNonRecoverable(var7)) {
				throw var7;
			}
		}
	}

	@Override
	public List<MetricSampler> profiledMetrics() {
		return ImmutableList.of(MetricSampler.create(this.name + "-pending-tasks", MetricCategory.EVENT_LOOPS, this::getPendingTasksCount));
	}

	public static boolean isNonRecoverable(Throwable throwable) {
		return throwable instanceof ReportedException reportedException
			? isNonRecoverable(reportedException.getCause())
			: throwable instanceof OutOfMemoryError || throwable instanceof StackOverflowError;
	}
}
