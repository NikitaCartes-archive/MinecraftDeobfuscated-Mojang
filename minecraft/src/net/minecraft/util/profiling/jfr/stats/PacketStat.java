package net.minecraft.util.profiling.jfr.stats;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;

public class PacketStat implements TimeStamped {
	final Instant timestamp;
	final String packetName;
	final int bytes;

	public PacketStat(RecordedEvent recordedEvent) {
		this.timestamp = recordedEvent.getStartTime();
		this.packetName = recordedEvent.getString("packetName");
		this.bytes = recordedEvent.getInt("bytes");
	}

	@Override
	public Instant getTimestamp() {
		return this.timestamp;
	}

	public static class PacketSummary {
		private final long totalCount;
		private final long totalSize;
		private final List<Pair<String, Long>> largestSizeContributors;
		private final Duration recordingDuration;

		public PacketSummary(Duration duration, List<PacketStat> list) {
			this.recordingDuration = duration;
			IntSummaryStatistics intSummaryStatistics = list.stream().mapToInt(packetStat -> packetStat.bytes).summaryStatistics();
			this.totalSize = intSummaryStatistics.getSum();
			this.totalCount = (long)list.size();
			this.largestSizeContributors = (List<Pair<String, Long>>)((Map)list.stream()
					.collect(Collectors.groupingBy(packetStat -> packetStat.packetName, Collectors.summingLong(packetStat -> (long)packetStat.bytes))))
				.entrySet()
				.stream()
				.sorted(Entry.comparingByValue(Comparator.reverseOrder()))
				.limit(5L)
				.map(entry -> Pair.of((String)entry.getKey(), (Long)entry.getValue()))
				.collect(Collectors.toList());
		}

		public long getTotalCount() {
			return this.totalCount;
		}

		public double getCountPerSecond() {
			return (double)this.totalCount / (double)this.recordingDuration.getSeconds();
		}

		public double getSizePerSecond() {
			return (double)this.totalSize / (double)this.recordingDuration.getSeconds();
		}

		public long getTotalSize() {
			return this.totalSize;
		}

		public List<Pair<String, Long>> getLargestSizeContributors() {
			return this.largestSizeContributors;
		}

		public Duration getRecordingDuration() {
			return this.recordingDuration;
		}
	}
}
