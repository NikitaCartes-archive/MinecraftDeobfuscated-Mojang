package net.minecraft.util.profiling.jfr.parse;

import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.profiling.jfr.serialize.JfrResultJsonSerializer;
import net.minecraft.util.profiling.jfr.serialize.JfrResultTextSerializer;
import net.minecraft.util.profiling.jfr.stats.ChunkGenStat;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.PacketStat;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;
import net.minecraft.util.profiling.jfr.stats.TimedStatSummary;
import net.minecraft.world.level.chunk.ChunkStatus;

public class JfrStatsResult {
	private final Instant recordingStarted;
	private final Instant recordingEnded;
	private final JfrStatsResult.Durations durations;
	private final List<TickTimeStat> ticktimes;
	private final List<CpuLoadStat> cpuLoadStats;
	private final GcHeapStat.HeapSummary heapSummary;
	private final ThreadAllocationStat.ThreadAllocationSummary threadAllocationSummary;
	private final PacketStat.PacketSummary receivedPackets;
	private final PacketStat.PacketSummary sentPackets;
	private final FileIOStat.FileIOSummary fileWrites;
	private final FileIOStat.FileIOSummary fileReads;
	private final List<ChunkGenStat> chunkGenStats;

	public JfrStatsResult(
		Instant instant,
		Instant instant2,
		JfrStatsResult.Durations durations,
		List<TickTimeStat> list,
		List<CpuLoadStat> list2,
		GcHeapStat.HeapSummary heapSummary,
		ThreadAllocationStat.ThreadAllocationSummary threadAllocationSummary,
		PacketStat.PacketSummary packetSummary,
		PacketStat.PacketSummary packetSummary2,
		FileIOStat.FileIOSummary fileIOSummary,
		FileIOStat.FileIOSummary fileIOSummary2,
		List<ChunkGenStat> list3
	) {
		this.recordingStarted = instant;
		this.recordingEnded = instant2;
		this.durations = durations;
		this.ticktimes = list;
		this.cpuLoadStats = list2;
		this.heapSummary = heapSummary;
		this.threadAllocationSummary = threadAllocationSummary;
		this.receivedPackets = packetSummary;
		this.sentPackets = packetSummary2;
		this.fileWrites = fileIOSummary;
		this.fileReads = fileIOSummary2;
		this.chunkGenStats = list3;
	}

	public Instant getRecordingStarted() {
		return this.recordingStarted;
	}

	public Instant getRecordingEnded() {
		return this.recordingEnded;
	}

	public Duration getRecordingDuration() {
		return Duration.between(this.recordingStarted, this.recordingEnded);
	}

	public Optional<Duration> getWorldCreationDuration() {
		return this.durations.getWorldCreationDuration();
	}

	public List<TickTimeStat> getTicktimes() {
		return this.ticktimes;
	}

	public GcHeapStat.HeapSummary getHeapSummary() {
		return this.heapSummary;
	}

	public ThreadAllocationStat.ThreadAllocationSummary getThreadAllocationSummary() {
		return this.threadAllocationSummary;
	}

	public PacketStat.PacketSummary getReceivedPackets() {
		return this.receivedPackets;
	}

	public PacketStat.PacketSummary getSentPackets() {
		return this.sentPackets;
	}

	public FileIOStat.FileIOSummary getFileWrites() {
		return this.fileWrites;
	}

	public FileIOStat.FileIOSummary getFileReads() {
		return this.fileReads;
	}

	public List<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>> getChunkGenSummary() {
		Map<ChunkStatus, List<ChunkGenStat>> map = (Map<ChunkStatus, List<ChunkGenStat>>)this.chunkGenStats
			.stream()
			.collect(Collectors.groupingBy(chunkGenStat -> chunkGenStat.status));
		return (List<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>>)map.entrySet()
			.stream()
			.map(entry -> Pair.of((ChunkStatus)entry.getKey(), new TimedStatSummary((List)entry.getValue())))
			.sorted(Comparator.comparing(pair -> ((TimedStatSummary)pair.getSecond()).totalDuration).reversed())
			.collect(Collectors.toList());
	}

	public List<CpuLoadStat> getCpuStats() {
		return this.cpuLoadStats;
	}

	public String asText() {
		return new JfrResultTextSerializer().format(this);
	}

	public String asJson() throws IOException {
		return new JfrResultJsonSerializer().format(this);
	}

	public static class Durations {
		@Nullable
		private final Duration worldCreationDuration;

		public Durations(@Nullable Duration duration) {
			this.worldCreationDuration = duration;
		}

		Optional<Duration> getWorldCreationDuration() {
			return Optional.ofNullable(this.worldCreationDuration);
		}
	}
}
