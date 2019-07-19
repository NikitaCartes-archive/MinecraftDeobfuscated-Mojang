package net.minecraft.util.profiling;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.time.Duration;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActiveProfiler implements ProfileCollector {
	private static final long WARNING_TIME_NANOS = Duration.ofMillis(100L).toNanos();
	private static final Logger LOGGER = LogManager.getLogger();
	private final List<String> paths = Lists.<String>newArrayList();
	private final LongList startTimes = new LongArrayList();
	private final Object2LongMap<String> times = new Object2LongOpenHashMap<>();
	private final Object2LongMap<String> counts = new Object2LongOpenHashMap<>();
	private final IntSupplier getTickTime;
	private final long startTimeNano;
	private final int startTimeTicks;
	private String path = "";
	private boolean started;

	public ActiveProfiler(long l, IntSupplier intSupplier) {
		this.startTimeNano = l;
		this.startTimeTicks = intSupplier.getAsInt();
		this.getTickTime = intSupplier;
	}

	@Override
	public void startTick() {
		if (this.started) {
			LOGGER.error("Profiler tick already started - missing endTick()?");
		} else {
			this.started = true;
			this.path = "";
			this.paths.clear();
			this.push("root");
		}
	}

	@Override
	public void endTick() {
		if (!this.started) {
			LOGGER.error("Profiler tick already ended - missing startTick()?");
		} else {
			this.pop();
			this.started = false;
			if (!this.path.isEmpty()) {
				LOGGER.error("Profiler tick ended before path was fully popped (remainder: '{}'). Mismatched push/pop?", () -> ProfileResults.demanglePath(this.path));
			}
		}
	}

	@Override
	public void push(String string) {
		if (!this.started) {
			LOGGER.error("Cannot push '{}' to profiler if profiler tick hasn't started - missing startTick()?", string);
		} else {
			if (!this.path.isEmpty()) {
				this.path = this.path + '\u001e';
			}

			this.path = this.path + string;
			this.paths.add(this.path);
			this.startTimes.add(Util.getNanos());
		}
	}

	@Override
	public void push(Supplier<String> supplier) {
		this.push((String)supplier.get());
	}

	@Override
	public void pop() {
		if (!this.started) {
			LOGGER.error("Cannot pop from profiler if profiler tick hasn't started - missing startTick()?");
		} else if (this.startTimes.isEmpty()) {
			LOGGER.error("Tried to pop one too many times! Mismatched push() and pop()?");
		} else {
			long l = Util.getNanos();
			long m = this.startTimes.removeLong(this.startTimes.size() - 1);
			this.paths.remove(this.paths.size() - 1);
			long n = l - m;
			this.times.put(this.path, this.times.getLong(this.path) + n);
			this.counts.put(this.path, this.counts.getLong(this.path) + 1L);
			if (n > WARNING_TIME_NANOS) {
				LOGGER.warn("Something's taking too long! '{}' took aprox {} ms", () -> ProfileResults.demanglePath(this.path), () -> (double)n / 1000000.0);
			}

			this.path = this.paths.isEmpty() ? "" : (String)this.paths.get(this.paths.size() - 1);
		}
	}

	@Override
	public void popPush(String string) {
		this.pop();
		this.push(string);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void popPush(Supplier<String> supplier) {
		this.pop();
		this.push(supplier);
	}

	@Override
	public ProfileResults getResults() {
		return new FilledProfileResults(this.times, this.counts, this.startTimeNano, this.startTimeTicks, Util.getNanos(), this.getTickTime.getAsInt());
	}
}
