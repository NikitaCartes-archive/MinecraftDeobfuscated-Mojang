package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import org.slf4j.Logger;

public class FutureChain implements TaskChainer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private CompletableFuture<?> head = CompletableFuture.completedFuture(null);
	private final Executor executor;

	public FutureChain(Executor executor) {
		this.executor = executor;
	}

	@Override
	public void append(TaskChainer.DelayedTask delayedTask) {
		this.head = this.head.thenComposeAsync(object -> (CompletionStage)delayedTask.get(), this.executor).exceptionally(throwable -> {
			if (throwable instanceof CompletionException completionException) {
				throwable = completionException.getCause();
			}

			if (throwable instanceof CancellationException cancellationException) {
				throw cancellationException;
			} else {
				LOGGER.error("Chain link failed, continuing to next one", throwable);
				return null;
			}
		});
	}
}
