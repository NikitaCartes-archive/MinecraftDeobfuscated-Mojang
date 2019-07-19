package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BlockableEventLoop<R extends Runnable> implements ProcessorHandle<R>, Executor {
	private final String name;
	private static final Logger LOGGER = LogManager.getLogger();
	private final Queue<R> pendingRunnables = Queues.<R>newConcurrentLinkedQueue();
	private int blockingCount;

	protected BlockableEventLoop(String string) {
		this.name = string;
	}

	protected abstract R wrapRunnable(Runnable runnable);

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

	@Environment(EnvType.CLIENT)
	public <V> CompletableFuture<V> submit(Supplier<V> supplier) {
		return this.scheduleExecutables() ? CompletableFuture.supplyAsync(supplier, this) : CompletableFuture.completedFuture(supplier.get());
	}

	private CompletableFuture<Void> submitAsync(Runnable runnable) {
		return CompletableFuture.supplyAsync(() -> {
			runnable.run();
			return null;
		}, this);
	}

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

	public void tell(R runnable) {
		this.pendingRunnables.add(runnable);
		LockSupport.unpark(this.getRunningThread());
	}

	public void execute(Runnable runnable) {
		if (this.scheduleExecutables()) {
			this.tell(this.wrapRunnable(runnable));
		} else {
			runnable.run();
		}
	}

	@Environment(EnvType.CLIENT)
	protected void dropAllTasks() {
		this.pendingRunnables.clear();
	}

	protected void runAllTasks() {
		while (this.pollTask()) {
		}
	}

	protected boolean pollTask() {
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
		try {
			runnable.run();
		} catch (Exception var3) {
			LOGGER.fatal("Error executing task on {}", this.name(), var3);
		}
	}
}
