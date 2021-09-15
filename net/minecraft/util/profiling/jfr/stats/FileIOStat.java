/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.jfr.stats;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

public record FileIOStat(Duration duration, @Nullable String path, long bytes) {
    public static Summary summary(Duration duration, List<FileIOStat> list) {
        long l = list.stream().mapToLong(fileIOStat -> fileIOStat.bytes).sum();
        return new Summary(l, (double)l / (double)duration.getSeconds(), list.size(), (double)list.size() / (double)duration.getSeconds(), list.stream().map(FileIOStat::duration).reduce(Duration.ZERO, Duration::plus), list.stream().filter(fileIOStat -> fileIOStat.path != null).collect(Collectors.groupingBy(fileIOStat -> fileIOStat.path, Collectors.summingLong(fileIOStat -> fileIOStat.bytes))).entrySet().stream().sorted(Map.Entry.comparingByValue().reversed()).map(entry -> Pair.of((String)entry.getKey(), (Long)entry.getValue())).limit(10L).toList());
    }

    @Nullable
    public String path() {
        return this.path;
    }

    public record Summary(long totalBytes, double bytesPerSecond, long counts, double countsPerSecond, Duration timeSpentInIO, List<Pair<String, Long>> topTenContributorsByTotalBytes) {
    }
}

