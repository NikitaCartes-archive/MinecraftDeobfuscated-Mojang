package net.minecraft.util.profiling.jfr.parse;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.profiling.jfr.serialize.JfrResultJsonSerializer;
import net.minecraft.util.profiling.jfr.stats.ChunkGenStat;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.PacketStat;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;
import net.minecraft.util.profiling.jfr.stats.TimedStatSummary;
import net.minecraft.world.level.chunk.ChunkStatus;

public record JfrStatsResult() {
	private final Instant recordingStarted;
	private final Instant recordingEnded;
	private final Duration recordingDuration;
	@Nullable
	private final Duration worldCreationDuration;
	private final List<TickTimeStat> tickTimes;
	private final List<CpuLoadStat> cpuLoadStats;
	private final GcHeapStat.Summary heapSummary;
	private final ThreadAllocationStat.Summary threadAllocationSummary;
	private final PacketStat.Summary receivedPackets;
	private final PacketStat.Summary sentPackets;
	private final FileIOStat.Summary fileWrites;
	private final FileIOStat.Summary fileReads;
	private final List<ChunkGenStat> chunkGenStats;

	public JfrStatsResult(
		Instant instant,
		Instant instant2,
		Duration duration,
		@Nullable Duration duration2,
		List<TickTimeStat> list,
		List<CpuLoadStat> list2,
		GcHeapStat.Summary summary,
		ThreadAllocationStat.Summary summary2,
		PacketStat.Summary summary3,
		PacketStat.Summary summary4,
		FileIOStat.Summary summary5,
		FileIOStat.Summary summary6,
		List<ChunkGenStat> list3
	) {
		this.recordingStarted = instant;
		this.recordingEnded = instant2;
		this.recordingDuration = duration;
		this.worldCreationDuration = duration2;
		this.tickTimes = list;
		this.cpuLoadStats = list2;
		this.heapSummary = summary;
		this.threadAllocationSummary = summary2;
		this.receivedPackets = summary3;
		this.sentPackets = summary4;
		this.fileWrites = summary5;
		this.fileReads = summary6;
		this.chunkGenStats = list3;
	}

	public List<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>> chunkGenSummary() {
		Map<ChunkStatus, List<ChunkGenStat>> map = (Map<ChunkStatus, List<ChunkGenStat>>)this.chunkGenStats
			.stream()
			.collect(Collectors.groupingBy(ChunkGenStat::status));
		return map.entrySet()
			.stream()
			.map(entry -> Pair.of((ChunkStatus)entry.getKey(), TimedStatSummary.summary((List)entry.getValue())))
			.sorted(Comparator.comparing(pair -> ((TimedStatSummary)pair.getSecond()).totalDuration()).reversed())
			.toList();
	}

	public String asJson() {
		return new JfrResultJsonSerializer().format(this);
	}
}
