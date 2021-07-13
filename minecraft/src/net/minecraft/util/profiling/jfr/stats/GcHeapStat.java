package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;

public class GcHeapStat {
	final Instant timestamp;
	private final int gcId;
	final long heapUsed;
	final GcHeapStat.Timing timing;

	public GcHeapStat(RecordedEvent recordedEvent) {
		this.timestamp = recordedEvent.getStartTime();
		this.gcId = recordedEvent.getInt("gcId");
		this.heapUsed = recordedEvent.getLong("heapUsed");
		this.timing = recordedEvent.getString("when").equalsIgnoreCase("before gc") ? GcHeapStat.Timing.BEFORE_GC : GcHeapStat.Timing.AFTER_GC;
	}

	public static class HeapSummary {
		public final double allocationRateBytesPerSecond;
		public final int totalGCs;
		public final Duration gcTotalDuration;
		private final Duration duration;

		public HeapSummary(Duration duration, List<GcHeapStat> list, Duration duration2, int i) {
			this.duration = duration;
			this.gcTotalDuration = duration2;
			this.totalGCs = i;
			this.allocationRateBytesPerSecond = calculateAllocationRatePerSecond(list);
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

		public float getGCOverHead() {
			return (float)this.gcTotalDuration.toMillis() / (float)this.duration.toMillis();
		}
	}

	static enum Timing {
		BEFORE_GC,
		AFTER_GC;
	}
}
