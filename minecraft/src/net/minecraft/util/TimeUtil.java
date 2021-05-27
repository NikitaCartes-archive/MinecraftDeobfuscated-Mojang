package net.minecraft.util;

import java.util.concurrent.TimeUnit;
import net.minecraft.util.valueproviders.UniformInt;

public class TimeUtil {
	public static final long NANOSECONDS_PER_SECOND = TimeUnit.SECONDS.toNanos(1L);
	public static final long NANOSECONDS_PER_MILLISECOND = TimeUnit.MILLISECONDS.toNanos(1L);

	public static UniformInt rangeOfSeconds(int i, int j) {
		return UniformInt.of(i * 20, j * 20);
	}
}
