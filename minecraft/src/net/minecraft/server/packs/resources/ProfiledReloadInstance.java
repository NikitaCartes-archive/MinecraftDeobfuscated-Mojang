package net.minecraft.server.packs.resources;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.Util;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import org.slf4j.Logger;

public class ProfiledReloadInstance extends SimpleReloadInstance<ProfiledReloadInstance.State> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Stopwatch total = Stopwatch.createUnstarted();

	public ProfiledReloadInstance(
		ResourceManager resourceManager, List<PreparableReloadListener> list, Executor executor, Executor executor2, CompletableFuture<Unit> completableFuture
	) {
		super(
			executor,
			executor2,
			resourceManager,
			list,
			(preparationBarrier, resourceManagerx, preparableReloadListener, executor2x, executor3) -> {
				AtomicLong atomicLong = new AtomicLong();
				AtomicLong atomicLong2 = new AtomicLong();
				ActiveProfiler activeProfiler = new ActiveProfiler(Util.timeSource, () -> 0, false);
				ActiveProfiler activeProfiler2 = new ActiveProfiler(Util.timeSource, () -> 0, false);
				CompletableFuture<Void> completableFuturex = preparableReloadListener.reload(
					preparationBarrier, resourceManagerx, activeProfiler, activeProfiler2, runnable -> executor2x.execute(() -> {
							long l = Util.getNanos();
							runnable.run();
							atomicLong.addAndGet(Util.getNanos() - l);
						}), runnable -> executor3.execute(() -> {
							long l = Util.getNanos();
							runnable.run();
							atomicLong2.addAndGet(Util.getNanos() - l);
						})
				);
				return completableFuturex.thenApplyAsync(
					void_ -> {
						LOGGER.debug("Finished reloading " + preparableReloadListener.getName());
						return new ProfiledReloadInstance.State(
							preparableReloadListener.getName(), activeProfiler.getResults(), activeProfiler2.getResults(), atomicLong, atomicLong2
						);
					},
					executor2
				);
			},
			completableFuture
		);
		this.total.start();
		this.allDone.thenAcceptAsync(this::finish, executor2);
	}

	private void finish(List<ProfiledReloadInstance.State> list) {
		this.total.stop();
		int i = 0;
		LOGGER.info("Resource reload finished after {} ms", this.total.elapsed(TimeUnit.MILLISECONDS));

		for (ProfiledReloadInstance.State state : list) {
			ProfileResults profileResults = state.preparationResult;
			ProfileResults profileResults2 = state.reloadResult;
			int j = (int)((double)state.preparationNanos.get() / 1000000.0);
			int k = (int)((double)state.reloadNanos.get() / 1000000.0);
			int l = j + k;
			String string = state.name;
			LOGGER.info("{} took approximately {} ms ({} ms preparing, {} ms applying)", string, l, j, k);
			i += k;
		}

		LOGGER.info("Total blocking time: {} ms", i);
	}

	public static class State {
		final String name;
		final ProfileResults preparationResult;
		final ProfileResults reloadResult;
		final AtomicLong preparationNanos;
		final AtomicLong reloadNanos;

		State(String string, ProfileResults profileResults, ProfileResults profileResults2, AtomicLong atomicLong, AtomicLong atomicLong2) {
			this.name = string;
			this.preparationResult = profileResults;
			this.reloadResult = profileResults2;
			this.preparationNanos = atomicLong;
			this.reloadNanos = atomicLong2;
		}
	}
}
