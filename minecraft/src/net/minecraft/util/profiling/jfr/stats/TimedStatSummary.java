package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.profiling.jfr.Percentiles;

public record TimedStatSummary<T extends TimedStat>(
	T fastest, T slowest, @Nullable T secondSlowest, int count, Map<Integer, Double> percentilesNanos, Duration totalDuration
) {
	public static <T extends TimedStat> TimedStatSummary<T> summary(List<T> list) {
		if (list.isEmpty()) {
			throw new IllegalArgumentException("No values");
		} else {
			List<T> list2 = list.stream().sorted(Comparator.comparing(TimedStat::duration)).toList();
			Duration duration = (Duration)list2.stream().map(TimedStat::duration).reduce(Duration::plus).orElse(Duration.ZERO);
			T timedStat = (T)list2.get(0);
			T timedStat2 = (T)list2.get(list2.size() - 1);
			T timedStat3 = (T)(list2.size() > 1 ? list2.get(list2.size() - 2) : null);
			int i = list2.size();
			Map<Integer, Double> map = Percentiles.evaluate(list2.stream().mapToLong(timedStatx -> timedStatx.duration().toNanos()).toArray());
			return new TimedStatSummary<>(timedStat, timedStat2, timedStat3, i, map, duration);
		}
	}
}
