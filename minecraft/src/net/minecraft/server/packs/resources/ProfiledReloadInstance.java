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
				CompletableFuture<Void> completableFuturexx = preparableReloadListener.reload(
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
				return completableFuturexx.thenApplyAsync(
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
		this.allDone = this.allDone.thenApplyAsync(this::finish, executor2);
	}

	private List<ProfiledReloadInstance.State> finish(List<ProfiledReloadInstance.State> list) {
		this.total.stop();
		long l = 0L;
		LOGGER.info("Resource reload finished after {} ms", this.total.elapsed(TimeUnit.MILLISECONDS));

		for(ProfiledReloadInstance.State state : list) {
			ProfileResults profileResults = state.preparationResult;
			ProfileResults profileResults2 = state.reloadResult;
			long m = TimeUnit.NANOSECONDS.toMillis(state.preparationNanos.get());
			long n = TimeUnit.NANOSECONDS.toMillis(state.reloadNanos.get());
			long o = m + n;
			String string = state.name;
			LOGGER.info("{} took approximately {} ms ({} ms preparing, {} ms applying)", string, o, m, n);
			l += n;
		}

		LOGGER.info("Total blocking time: {} ms", l);
		return list;
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
