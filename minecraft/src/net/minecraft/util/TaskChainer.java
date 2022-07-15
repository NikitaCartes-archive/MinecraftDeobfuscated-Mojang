package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.slf4j.Logger;

@FunctionalInterface
public interface TaskChainer {
	Logger LOGGER = LogUtils.getLogger();
	TaskChainer IMMEDIATE = delayedTask -> ((CompletableFuture)delayedTask.get()).exceptionally(throwable -> {
			LOGGER.error("Task failed", throwable);
			return null;
		});

	void append(TaskChainer.DelayedTask delayedTask);

	public interface DelayedTask extends Supplier<CompletableFuture<?>> {
	}
}
