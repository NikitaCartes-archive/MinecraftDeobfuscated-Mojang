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

public record PacketStat() implements TimeStamped {
	private final Instant timestamp;
	private final String packetName;
	private final int bytes;

	public PacketStat(Instant instant, String string, int i) {
		this.timestamp = instant;
		this.packetName = string;
		this.bytes = i;
	}

	public static PacketStat from(RecordedEvent recordedEvent) {
		return new PacketStat(recordedEvent.getStartTime(), recordedEvent.getString("packetName"), recordedEvent.getInt("bytes"));
	}

	public static PacketStat.Summary summary(Duration duration, List<PacketStat> list) {
		IntSummaryStatistics intSummaryStatistics = list.stream().mapToInt(packetStat -> packetStat.bytes).summaryStatistics();
		long l = (long)list.size();
		long m = intSummaryStatistics.getSum();
		List<Pair<String, Long>> list2 = ((Map)list.stream()
				.collect(Collectors.groupingBy(packetStat -> packetStat.packetName, Collectors.summingLong(packetStat -> (long)packetStat.bytes))))
			.entrySet()
			.stream()
			.sorted(Entry.comparingByValue(Comparator.reverseOrder()))
			.limit(5L)
			.map(entry -> Pair.of((String)entry.getKey(), (Long)entry.getValue()))
			.toList();
		return new PacketStat.Summary(l, m, list2, duration);
	}

	@Override
	public Instant getTimestamp() {
		return this.timestamp;
	}

	public static record Summary() {
		private final long totalCount;
		private final long totalSize;
		private final List<Pair<String, Long>> largestSizeContributors;
		private final Duration recordingDuration;

		public Summary(long l, long m, List<Pair<String, Long>> list, Duration duration) {
			this.totalCount = l;
			this.totalSize = m;
			this.largestSizeContributors = list;
			this.recordingDuration = duration;
		}

		public double countsPerSecond() {
			return (double)this.totalCount / (double)this.recordingDuration.getSeconds();
		}

		public double sizePerSecond() {
			return (double)this.totalSize / (double)this.recordingDuration.getSeconds();
		}
	}
}
