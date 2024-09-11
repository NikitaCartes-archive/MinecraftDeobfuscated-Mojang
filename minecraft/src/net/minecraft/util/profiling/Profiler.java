package net.minecraft.util.profiling;

import com.mojang.jtracy.TracyClient;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class Profiler {
	private static final ThreadLocal<TracyZoneFiller> TRACY_FILLER = ThreadLocal.withInitial(TracyZoneFiller::new);
	private static final ThreadLocal<ProfilerFiller> ACTIVE = new ThreadLocal();
	private static final AtomicInteger ACTIVE_COUNT = new AtomicInteger();

	private Profiler() {
	}

	public static Profiler.Scope use(ProfilerFiller profilerFiller) {
		startUsing(profilerFiller);
		return Profiler::stopUsing;
	}

	private static void startUsing(ProfilerFiller profilerFiller) {
		if (ACTIVE.get() != null) {
			throw new IllegalStateException("Profiler is already active");
		} else {
			ProfilerFiller profilerFiller2 = decorateFiller(profilerFiller);
			ACTIVE.set(profilerFiller2);
			ACTIVE_COUNT.incrementAndGet();
			profilerFiller2.startTick();
		}
	}

	private static void stopUsing() {
		ProfilerFiller profilerFiller = (ProfilerFiller)ACTIVE.get();
		if (profilerFiller == null) {
			throw new IllegalStateException("Profiler was not active");
		} else {
			ACTIVE.remove();
			ACTIVE_COUNT.decrementAndGet();
			profilerFiller.endTick();
		}
	}

	private static ProfilerFiller decorateFiller(ProfilerFiller profilerFiller) {
		return ProfilerFiller.combine(getDefaultFiller(), profilerFiller);
	}

	public static ProfilerFiller get() {
		return ACTIVE_COUNT.get() == 0 ? getDefaultFiller() : (ProfilerFiller)Objects.requireNonNullElseGet((ProfilerFiller)ACTIVE.get(), Profiler::getDefaultFiller);
	}

	private static ProfilerFiller getDefaultFiller() {
		return (ProfilerFiller)(TracyClient.isAvailable() ? (ProfilerFiller)TRACY_FILLER.get() : InactiveProfiler.INSTANCE);
	}

	public interface Scope extends AutoCloseable {
		void close();
	}
}
