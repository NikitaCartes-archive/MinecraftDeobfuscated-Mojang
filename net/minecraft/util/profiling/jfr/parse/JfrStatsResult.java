/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.jfr.parse;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
import org.jetbrains.annotations.Nullable;

public record JfrStatsResult(Instant recordingStarted, Instant recordingEnded, Duration recordingDuration, @Nullable Duration worldCreationDuration, List<TickTimeStat> tickTimes, List<CpuLoadStat> cpuLoadStats, GcHeapStat.Summary heapSummary, ThreadAllocationStat.Summary threadAllocationSummary, PacketStat.Summary receivedPackets, PacketStat.Summary sentPackets, FileIOStat.Summary fileWrites, FileIOStat.Summary fileReads, List<ChunkGenStat> chunkGenStats) {
    public List<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>> chunkGenSummary() {
        Map<ChunkStatus, List<ChunkGenStat>> map = this.chunkGenStats.stream().collect(Collectors.groupingBy(ChunkGenStat::status));
        return map.entrySet().stream().map(entry -> Pair.of((ChunkStatus)entry.getKey(), TimedStatSummary.summary((List)entry.getValue()))).sorted(Comparator.comparing(pair -> ((TimedStatSummary)pair.getSecond()).totalDuration()).reversed()).toList();
    }

    public String asJson() {
        return new JfrResultJsonSerializer().format(this);
    }

    @Nullable
    public Duration worldCreationDuration() {
        return this.worldCreationDuration;
    }
}

