package net.minecraft;

import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public record TracingExecutor(ExecutorService service) implements Executor {
	public Executor forName(String string) {
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			return runnable -> this.service.execute(() -> {
					Thread thread = Thread.currentThread();
					String string2 = thread.getName();
					thread.setName(string);

					try (Zone zone = TracyClient.beginZone(string, SharedConstants.IS_RUNNING_IN_IDE)) {
						runnable.run();
					} finally {
						thread.setName(string2);
					}
				});
		} else {
			return (Executor)(TracyClient.isAvailable() ? runnable -> this.service.execute(() -> {
					try (Zone zone = TracyClient.beginZone(string, SharedConstants.IS_RUNNING_IN_IDE)) {
						runnable.run();
					}
				}) : this.service);
		}
	}

	public void execute(Runnable runnable) {
		this.service.execute(wrapUnnamed(runnable));
	}

	public void shutdownAndAwait(long l, TimeUnit timeUnit) {
		this.service.shutdown();

		boolean bl;
		try {
			bl = this.service.awaitTermination(l, timeUnit);
		} catch (InterruptedException var6) {
			bl = false;
		}

		if (!bl) {
			this.service.shutdownNow();
		}
	}

	private static Runnable wrapUnnamed(Runnable runnable) {
		return !TracyClient.isAvailable() ? runnable : () -> {
			try (Zone zone = TracyClient.beginZone("task", SharedConstants.IS_RUNNING_IN_IDE)) {
				runnable.run();
			}
		};
	}
}
