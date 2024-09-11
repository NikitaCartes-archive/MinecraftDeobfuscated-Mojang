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
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
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
				CompletableFuture<Void> completableFuturex = preparableReloadListener.reload(
					preparationBarrier,
					resourceManagerx,
					profiledExecutor(executor2x, atomicLong, preparableReloadListener.getName()),
					profiledExecutor(executor3, atomicLong2, preparableReloadListener.getName())
				);
				return completableFuturex.thenApplyAsync(void_ -> {
					LOGGER.debug("Finished reloading {}", preparableReloadListener.getName());
					return new ProfiledReloadInstance.State(preparableReloadListener.getName(), atomicLong, atomicLong2);
				}, executor2);
			},
			completableFuture
		);
		this.total.start();
		this.allDone = this.allDone.thenApplyAsync(this::finish, executor2);
	}

	private static Executor profiledExecutor(Executor executor, AtomicLong atomicLong, String string) {
		return runnable -> executor.execute(() -> {
				ProfilerFiller profilerFiller = Profiler.get();
				profilerFiller.push(string);
				long l = Util.getNanos();
				runnable.run();
				atomicLong.addAndGet(Util.getNanos() - l);
				profilerFiller.pop();
			});
	}

	private List<ProfiledReloadInstance.State> finish(List<ProfiledReloadInstance.State> list) {
		this.total.stop();
		long l = 0L;
		LOGGER.info("Resource reload finished after {} ms", this.total.elapsed(TimeUnit.MILLISECONDS));

		for (ProfiledReloadInstance.State state : list) {
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

	public static record State(String name, AtomicLong preparationNanos, AtomicLong reloadNanos) {
	}
}
