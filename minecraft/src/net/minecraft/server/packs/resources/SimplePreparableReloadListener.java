package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.profiling.ProfilerFiller;

public abstract class SimplePreparableReloadListener<T> implements PreparableReloadListener {
	@Override
	public final CompletableFuture<Void> reload(
		PreparableReloadListener.PreparationBarrier preparationBarrier,
		ResourceManager resourceManager,
		ProfilerFiller profilerFiller,
		ProfilerFiller profilerFiller2,
		Executor executor,
		Executor executor2
	) {
		return CompletableFuture.supplyAsync(() -> this.prepare(resourceManager, profilerFiller), executor)
			.thenCompose(preparationBarrier::wait)
			.thenAcceptAsync(object -> this.apply((T)object, resourceManager, profilerFiller2), executor2);
	}

	protected abstract T prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller);

	protected abstract void apply(T object, ResourceManager resourceManager, ProfilerFiller profilerFiller);
}
