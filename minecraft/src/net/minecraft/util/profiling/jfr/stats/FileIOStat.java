package net.minecraft.util.profiling.jfr.stats;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public record FileIOStat() {
	private final Duration duration;
	@Nullable
	private final String path;
	private final long bytes;

	public FileIOStat(Duration duration, @Nullable String string, long l) {
		this.duration = duration;
		this.path = string;
		this.bytes = l;
	}

	public static FileIOStat.Summary summary(Duration duration, List<FileIOStat> list) {
		long l = list.stream().mapToLong(fileIOStat -> fileIOStat.bytes).sum();
		return new FileIOStat.Summary(
			l,
			(double)l / (double)duration.getSeconds(),
			(long)list.size(),
			(double)list.size() / (double)duration.getSeconds(),
			(Duration)list.stream().map(FileIOStat::duration).reduce(Duration.ZERO, Duration::plus),
			((Map)list.stream()
					.filter(fileIOStat -> fileIOStat.path != null)
					.collect(Collectors.groupingBy(fileIOStat -> fileIOStat.path, Collectors.summingLong(fileIOStat -> fileIOStat.bytes))))
				.entrySet()
				.stream()
				.sorted(Entry.comparingByValue().reversed())
				.map(entry -> Pair.of((String)entry.getKey(), (Long)entry.getValue()))
				.limit(10L)
				.toList()
		);
	}

	public static record Summary() {
		private final long totalBytes;
		private final double bytesPerSecond;
		private final long counts;
		private final double countsPerSecond;
		private final Duration timeSpentInIO;
		private final List<Pair<String, Long>> topTenContributorsByTotalBytes;

		public Summary(long l, double d, long m, double e, Duration duration, List<Pair<String, Long>> list) {
			this.totalBytes = l;
			this.bytesPerSecond = d;
			this.counts = m;
			this.countsPerSecond = e;
			this.timeSpentInIO = duration;
			this.topTenContributorsByTotalBytes = list;
		}
	}
}
