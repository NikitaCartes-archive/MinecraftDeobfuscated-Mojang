package net.minecraft.util.profiling;

import java.time.Duration;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameProfiler implements ProfilerFiller {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final long MAXIMUM_TICK_TIME_NANOS = Duration.ofMillis(300L).toNanos();
	private final IntSupplier getTickTime;
	private final GameProfiler.ProfilerImpl continuous = new GameProfiler.ProfilerImpl();
	private final GameProfiler.ProfilerImpl perTick = new GameProfiler.ProfilerImpl();

	public GameProfiler(IntSupplier intSupplier) {
		this.getTickTime = intSupplier;
	}

	public GameProfiler.Profiler continuous() {
		return this.continuous;
	}

	@Override
	public void startTick() {
		this.continuous.collector.startTick();
		this.perTick.collector.startTick();
	}

	@Override
	public void endTick() {
		this.continuous.collector.endTick();
		this.perTick.collector.endTick();
	}

	@Override
	public void push(String string) {
		this.continuous.collector.push(string);
		this.perTick.collector.push(string);
	}

	@Override
	public void push(Supplier<String> supplier) {
		this.continuous.collector.push(supplier);
		this.perTick.collector.push(supplier);
	}

	@Override
	public void pop() {
		this.continuous.collector.pop();
		this.perTick.collector.pop();
	}

	@Override
	public void popPush(String string) {
		this.continuous.collector.popPush(string);
		this.perTick.collector.popPush(string);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void popPush(Supplier<String> supplier) {
		this.continuous.collector.popPush(supplier);
		this.perTick.collector.popPush(supplier);
	}

	public interface Profiler {
		boolean isEnabled();

		ProfileResults disable();

		@Environment(EnvType.CLIENT)
		ProfileResults getResults();

		void enable();
	}

	class ProfilerImpl implements GameProfiler.Profiler {
		protected ProfileCollector collector = InactiveProfiler.INACTIVE;

		private ProfilerImpl() {
		}

		@Override
		public boolean isEnabled() {
			return this.collector != InactiveProfiler.INACTIVE;
		}

		@Override
		public ProfileResults disable() {
			ProfileResults profileResults = this.collector.getResults();
			this.collector = InactiveProfiler.INACTIVE;
			return profileResults;
		}

		@Environment(EnvType.CLIENT)
		@Override
		public ProfileResults getResults() {
			return this.collector.getResults();
		}

		@Override
		public void enable() {
			if (this.collector == InactiveProfiler.INACTIVE) {
				this.collector = new ActiveProfiler(Util.getNanos(), GameProfiler.this.getTickTime, true);
			}
		}
	}
}
