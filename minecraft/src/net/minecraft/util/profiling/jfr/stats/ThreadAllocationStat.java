package net.minecraft.util.profiling.jfr.stats;

import com.google.common.base.MoreObjects;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedThread;

public class ThreadAllocationStat {
	private static final String UNKNOWN_THREAD = "unknown";
	final Instant timestamp;
	final String thread;
	final long bytes;

	public ThreadAllocationStat(RecordedEvent recordedEvent) {
		this.timestamp = recordedEvent.getStartTime();
		RecordedThread recordedThread = recordedEvent.getThread("thread");
		this.thread = recordedThread == null ? "unknown" : MoreObjects.firstNonNull(recordedThread.getJavaName(), "unknown");
		this.bytes = recordedEvent.getLong("allocated");
	}

	public static class ThreadAllocationSummary {
		public final Map<String, Long> allocationsPerSecondByThread = new TreeMap();

		public ThreadAllocationSummary(List<ThreadAllocationStat> list) {
			Map<String, List<ThreadAllocationStat>> map = (Map<String, List<ThreadAllocationStat>>)list.stream()
				.collect(Collectors.groupingBy(threadAllocationStat -> threadAllocationStat.thread));
			map.forEach((string, listx) -> {
				if (listx.size() >= 2) {
					ThreadAllocationStat threadAllocationStat = (ThreadAllocationStat)listx.get(0);
					ThreadAllocationStat threadAllocationStat2 = (ThreadAllocationStat)listx.get(listx.size() - 1);
					long l = Duration.between(threadAllocationStat.timestamp, threadAllocationStat2.timestamp).getSeconds();
					this.allocationsPerSecondByThread.put(string, (threadAllocationStat2.bytes - threadAllocationStat.bytes) / l);
				}
			});
		}
	}
}
