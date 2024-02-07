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
import net.minecraft.util.profiling.jfr.stats.ChunkIdentification;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.IoSummary;
import net.minecraft.util.profiling.jfr.stats.PacketIdentification;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;
import net.minecraft.util.profiling.jfr.stats.TimedStatSummary;
import net.minecraft.world.level.chunk.ChunkStatus;

public record JfrStatsResult(
	Instant recordingStarted,
	Instant recordingEnded,
	Duration recordingDuration,
	@Nullable Duration worldCreationDuration,
	List<TickTimeStat> tickTimes,
	List<CpuLoadStat> cpuLoadStats,
	GcHeapStat.Summary heapSummary,
	ThreadAllocationStat.Summary threadAllocationSummary,
	IoSummary<PacketIdentification> receivedPacketsSummary,
	IoSummary<PacketIdentification> sentPacketsSummary,
	IoSummary<ChunkIdentification> writtenChunks,
	IoSummary<ChunkIdentification> readChunks,
	FileIOStat.Summary fileWrites,
	FileIOStat.Summary fileReads,
	List<ChunkGenStat> chunkGenStats
) {
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
