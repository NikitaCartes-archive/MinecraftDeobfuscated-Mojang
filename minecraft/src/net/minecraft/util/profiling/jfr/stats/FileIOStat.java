package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;

public class FileIOStat {
	final Instant timestamp;
	final Duration duration;
	@Nullable
	final String path;
	final long bytes;

	public FileIOStat(Instant instant, Duration duration, @Nullable String string, long l) {
		this.timestamp = instant;
		this.duration = duration;
		this.path = string;
		this.bytes = l;
	}

	public static class FileIOSummary {
		private final long totalBytes;
		private final int counts;
		private final Duration recordingDuration;
		private final List<FileIOStat> ioStats;

		public FileIOSummary(Duration duration, List<FileIOStat> list) {
			this.ioStats = list;
			this.totalBytes = list.stream().mapToLong(fileIOStat -> fileIOStat.bytes).sum();
			this.counts = list.size();
			this.recordingDuration = duration;
		}

		public long getTotalBytes() {
			return this.totalBytes;
		}

		public int getCounts() {
			return this.counts;
		}

		public double bytesPerSecond() {
			return (double)this.totalBytes / (double)this.recordingDuration.getSeconds();
		}

		public double countsPerSecond() {
			return (double)this.counts / (double)this.recordingDuration.getSeconds();
		}

		public Duration timeSpentInIO() {
			return (Duration)this.ioStats.stream().map(fileIOStat -> fileIOStat.duration).reduce(Duration.ZERO, Duration::plus);
		}

		public Stream<Pair<String, Long>> topContributors() {
			return ((Map)this.ioStats
					.stream()
					.filter(fileIOStat -> fileIOStat.path != null)
					.collect(Collectors.groupingBy(fileIOStat -> fileIOStat.path, Collectors.summingLong(fileIOStat -> fileIOStat.bytes))))
				.entrySet()
				.stream()
				.sorted(Entry.comparingByValue().reversed())
				.map(entry -> Pair.of((String)entry.getKey(), (Long)entry.getValue()));
		}
	}
}
