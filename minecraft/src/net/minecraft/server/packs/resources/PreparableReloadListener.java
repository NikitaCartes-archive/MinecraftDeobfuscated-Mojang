package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.profiling.ProfilerFiller;

public interface PreparableReloadListener {
	CompletableFuture<Void> reload(
		PreparableReloadListener.PreparationBarrier preparationBarrier,
		ResourceManager resourceManager,
		ProfilerFiller profilerFiller,
		ProfilerFiller profilerFiller2,
		Executor executor,
		Executor executor2
	);

	default String getName() {
		return this.getClass().getSimpleName();
	}

	public interface PreparationBarrier {
		<T> CompletableFuture<T> wait(T object);
	}
}
