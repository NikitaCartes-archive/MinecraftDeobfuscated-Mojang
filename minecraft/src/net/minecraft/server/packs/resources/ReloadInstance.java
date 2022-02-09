package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;

public interface ReloadInstance {
	CompletableFuture<?> done();

	float getActualProgress();

	default boolean isDone() {
		return this.done().isDone();
	}

	default void checkExceptions() {
		CompletableFuture<?> completableFuture = this.done();
		if (completableFuture.isCompletedExceptionally()) {
			completableFuture.join();
		}
	}
}
