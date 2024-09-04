package net.minecraft.util.thread;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

public class ConsecutiveExecutor extends AbstractConsecutiveExecutor<Runnable> {
	public ConsecutiveExecutor(Executor executor, String string) {
		super(new StrictQueue.QueueStrictQueue(new ConcurrentLinkedQueue()), executor, string);
	}

	@Override
	public Runnable wrapRunnable(Runnable runnable) {
		return runnable;
	}
}
