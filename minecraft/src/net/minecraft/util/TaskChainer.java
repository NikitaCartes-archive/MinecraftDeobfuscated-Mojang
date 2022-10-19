package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.slf4j.Logger;

@FunctionalInterface
public interface TaskChainer {
	Logger LOGGER = LogUtils.getLogger();

	static TaskChainer immediate(Executor executor) {
		return delayedTask -> delayedTask.submit(executor).exceptionally(throwable -> {
				LOGGER.error("Task failed", throwable);
				return null;
			});
	}

	void append(TaskChainer.DelayedTask delayedTask);

	public interface DelayedTask {
		CompletableFuture<?> submit(Executor executor);
	}
}
