package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;

public record GcHeapStat() {
	private final Instant timestamp;
	private final long heapUsed;
	private final GcHeapStat.Timing timing;

	public GcHeapStat(Instant instant, long l, GcHeapStat.Timing timing) {
		this.timestamp = instant;
		this.heapUsed = l;
		this.timing = timing;
	}

	public static GcHeapStat from(RecordedEvent recordedEvent) {
		return new GcHeapStat(
			recordedEvent.getStartTime(),
			recordedEvent.getLong("heapUsed"),
			recordedEvent.getString("when").equalsIgnoreCase("before gc") ? GcHeapStat.Timing.BEFORE_GC : GcHeapStat.Timing.AFTER_GC
		);
	}

	public static GcHeapStat.Summary summary(Duration duration, List<GcHeapStat> list, Duration duration2, int i) {
		return new GcHeapStat.Summary(duration, duration2, i, calculateAllocationRatePerSecond(list));
	}

	private static double calculateAllocationRatePerSecond(List<GcHeapStat> list) {
		long l = 0L;
		Map<GcHeapStat.Timing, List<GcHeapStat>> map = (Map<GcHeapStat.Timing, List<GcHeapStat>>)list.stream()
			.collect(Collectors.groupingBy(gcHeapStatx -> gcHeapStatx.timing));
		List<GcHeapStat> list2 = (List<GcHeapStat>)map.get(GcHeapStat.Timing.BEFORE_GC);
		List<GcHeapStat> list3 = (List<GcHeapStat>)map.get(GcHeapStat.Timing.AFTER_GC);

		for (int i = 1; i < list2.size(); i++) {
			GcHeapStat gcHeapStat = (GcHeapStat)list2.get(i);
			GcHeapStat gcHeapStat2 = (GcHeapStat)list3.get(i - 1);
			l += gcHeapStat.heapUsed - gcHeapStat2.heapUsed;
		}

		Duration duration = Duration.between(((GcHeapStat)list.get(1)).timestamp, ((GcHeapStat)list.get(list.size() - 1)).timestamp);
		return (double)l / (double)duration.getSeconds();
	}

	public static record Summary() {
		private final Duration duration;
		private final Duration gcTotalDuration;
		private final int totalGCs;
		private final double allocationRateBytesPerSecond;

		public Summary(Duration duration, Duration duration2, int i, double d) {
			this.duration = duration;
			this.gcTotalDuration = duration2;
			this.totalGCs = i;
			this.allocationRateBytesPerSecond = d;
		}

		public float gcOverHead() {
			return (float)this.gcTotalDuration.toMillis() / (float)this.duration.toMillis();
		}
	}

	static enum Timing {
		BEFORE_GC,
		AFTER_GC;
	}
}
