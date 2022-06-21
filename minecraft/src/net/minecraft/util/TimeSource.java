package net.minecraft.util;

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

@FunctionalInterface
public interface TimeSource {
	long get(TimeUnit timeUnit);

	public interface NanoTimeSource extends TimeSource, LongSupplier {
		@Override
		default long get(TimeUnit timeUnit) {
			return timeUnit.convert(this.getAsLong(), TimeUnit.NANOSECONDS);
		}
	}
}
