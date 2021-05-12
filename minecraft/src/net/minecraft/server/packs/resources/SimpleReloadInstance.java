package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.Util;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.InactiveProfiler;

public class SimpleReloadInstance<S> implements ReloadInstance {
	private static final int PREPARATION_PROGRESS_WEIGHT = 2;
	private static final int EXTRA_RELOAD_PROGRESS_WEIGHT = 2;
	private static final int LISTENER_PROGRESS_WEIGHT = 1;
	protected final ResourceManager resourceManager;
	protected final CompletableFuture<Unit> allPreparations = new CompletableFuture();
	protected final CompletableFuture<List<S>> allDone;
	final Set<PreparableReloadListener> preparingListeners;
	private final int listenerCount;
	private int startedReloads;
	private int finishedReloads;
	private final AtomicInteger startedTaskCounter = new AtomicInteger();
	private final AtomicInteger doneTaskCounter = new AtomicInteger();

	public static SimpleReloadInstance<Void> of(
		ResourceManager resourceManager, List<PreparableReloadListener> list, Executor executor, Executor executor2, CompletableFuture<Unit> completableFuture
	) {
		return new SimpleReloadInstance<>(
			executor,
			executor2,
			resourceManager,
			list,
			(preparationBarrier, resourceManagerx, preparableReloadListener, executor2x, executor3) -> preparableReloadListener.reload(
					preparationBarrier, resourceManagerx, InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, executor, executor3
				),
			completableFuture
		);
	}

	protected SimpleReloadInstance(
		Executor executor,
		Executor executor2,
		ResourceManager resourceManager,
		List<PreparableReloadListener> list,
		SimpleReloadInstance.StateFactory<S> stateFactory,
		CompletableFuture<Unit> completableFuture
	) {
		this.resourceManager = resourceManager;
		this.listenerCount = list.size();
		this.startedTaskCounter.incrementAndGet();
		completableFuture.thenRun(this.doneTaskCounter::incrementAndGet);
		List<CompletableFuture<S>> list2 = Lists.<CompletableFuture<S>>newArrayList();
		CompletableFuture<?> completableFuture2 = completableFuture;
		this.preparingListeners = Sets.<PreparableReloadListener>newHashSet(list);

		for (final PreparableReloadListener preparableReloadListener : list) {
			final CompletableFuture<?> completableFuture3 = completableFuture2;
			CompletableFuture<S> completableFuture4 = stateFactory.create(new PreparableReloadListener.PreparationBarrier() {
				@Override
				public <T> CompletableFuture<T> wait(T object) {
					executor2.execute(() -> {
						SimpleReloadInstance.this.preparingListeners.remove(preparableReloadListener);
						if (SimpleReloadInstance.this.preparingListeners.isEmpty()) {
							SimpleReloadInstance.this.allPreparations.complete(Unit.INSTANCE);
						}
					});
					return SimpleReloadInstance.this.allPreparations.thenCombine(completableFuture3, (unit, object2) -> object);
				}
			}, resourceManager, preparableReloadListener, runnable -> {
				this.startedTaskCounter.incrementAndGet();
				executor.execute(() -> {
					runnable.run();
					this.doneTaskCounter.incrementAndGet();
				});
			}, runnable -> {
				this.startedReloads++;
				executor2.execute(() -> {
					runnable.run();
					this.finishedReloads++;
				});
			});
			list2.add(completableFuture4);
			completableFuture2 = completableFuture4;
		}

		this.allDone = Util.sequenceFailFast(list2);
	}

	@Override
	public CompletableFuture<Unit> done() {
		return this.allDone.thenApply(list -> Unit.INSTANCE);
	}

	@Override
	public float getActualProgress() {
		int i = this.listenerCount - this.preparingListeners.size();
		float f = (float)(this.doneTaskCounter.get() * 2 + this.finishedReloads * 2 + i * 1);
		float g = (float)(this.startedTaskCounter.get() * 2 + this.startedReloads * 2 + this.listenerCount * 1);
		return f / g;
	}

	@Override
	public boolean isDone() {
		return this.allDone.isDone();
	}

	@Override
	public void checkExceptions() {
		if (this.allDone.isCompletedExceptionally()) {
			this.allDone.join();
		}
	}

	protected interface StateFactory<S> {
		CompletableFuture<S> create(
			PreparableReloadListener.PreparationBarrier preparationBarrier,
			ResourceManager resourceManager,
			PreparableReloadListener preparableReloadListener,
			Executor executor,
			Executor executor2
		);
	}
}
