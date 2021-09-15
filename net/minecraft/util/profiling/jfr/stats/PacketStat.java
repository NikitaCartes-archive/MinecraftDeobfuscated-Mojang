/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.jfr.stats;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;
import net.minecraft.util.profiling.jfr.stats.TimeStamped;

public record PacketStat(Instant timestamp, String packetName, int bytes) implements TimeStamped
{
    public static PacketStat from(RecordedEvent recordedEvent) {
        return new PacketStat(recordedEvent.getStartTime(), recordedEvent.getString("packetName"), recordedEvent.getInt("bytes"));
    }

    public static Summary summary(Duration duration, List<PacketStat> list) {
        IntSummaryStatistics intSummaryStatistics = list.stream().mapToInt(packetStat -> packetStat.bytes).summaryStatistics();
        long l = list.size();
        long m = intSummaryStatistics.getSum();
        List<Pair<String, Long>> list2 = list.stream().collect(Collectors.groupingBy(packetStat -> packetStat.packetName, Collectors.summingLong(packetStat -> packetStat.bytes))).entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(5L).map(entry -> Pair.of((String)entry.getKey(), (Long)entry.getValue())).toList();
        return new Summary(l, m, list2, duration);
    }

    @Override
    public Instant getTimestamp() {
        return this.timestamp;
    }

    public record Summary(long totalCount, long totalSize, List<Pair<String, Long>> largestSizeContributors, Duration recordingDuration) {
        public double countsPerSecond() {
            return (double)this.totalCount / (double)this.recordingDuration.getSeconds();
        }

        public double sizePerSecond() {
            return (double)this.totalSize / (double)this.recordingDuration.getSeconds();
        }
    }
}

