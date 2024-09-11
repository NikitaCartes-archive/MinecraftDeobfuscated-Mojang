package net.minecraft.util.profiling;

import java.util.function.Supplier;
import javax.annotation.Nullable;

public class Zone implements AutoCloseable {
	public static final Zone INACTIVE = new Zone(null);
	@Nullable
	private final ProfilerFiller profiler;

	Zone(@Nullable ProfilerFiller profilerFiller) {
		this.profiler = profilerFiller;
	}

	public Zone addText(String string) {
		if (this.profiler != null) {
			this.profiler.addZoneText(string);
		}

		return this;
	}

	public Zone addText(Supplier<String> supplier) {
		if (this.profiler != null) {
			this.profiler.addZoneText((String)supplier.get());
		}

		return this;
	}

	public Zone addValue(long l) {
		if (this.profiler != null) {
			this.profiler.addZoneValue(l);
		}

		return this;
	}

	public Zone setColor(int i) {
		if (this.profiler != null) {
			this.profiler.setZoneColor(i);
		}

		return this;
	}

	public void close() {
		if (this.profiler != null) {
			this.profiler.pop();
		}
	}
}
