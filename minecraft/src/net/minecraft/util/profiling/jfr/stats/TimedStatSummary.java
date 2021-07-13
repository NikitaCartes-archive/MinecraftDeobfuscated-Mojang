package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.util.profiling.jfr.Percentiles;

public class TimedStatSummary<T extends TimedStat> {
	public static final int[] PERCENTILES = new int[]{50, 75, 90, 99};
	public final T fastest;
	public final T slowest;
	public final Optional<T> secondSlowest;
	public final int count;
	public final Map<Integer, Double> percentilesNanos;
	public final Duration totalDuration;

	public TimedStatSummary(List<T> list) {
		if (list.isEmpty()) {
			throw new IllegalArgumentException("No values");
		} else {
			List<T> list2 = (List<T>)list.stream().sorted(Comparator.comparing(TimedStat::duration)).collect(Collectors.toList());
			this.totalDuration = (Duration)list2.stream().map(TimedStat::duration).reduce(Duration::plus).orElse(Duration.ZERO);
			this.fastest = (T)list2.get(0);
			this.slowest = (T)list2.get(list2.size() - 1);
			this.secondSlowest = list2.size() > 1 ? Optional.of((TimedStat)list2.get(list2.size() - 2)) : Optional.empty();
			this.count = list2.size();
			this.percentilesNanos = Percentiles.evaluate(list2.stream().mapToLong(timedStat -> timedStat.duration().toNanos()).toArray());
		}
	}
}
