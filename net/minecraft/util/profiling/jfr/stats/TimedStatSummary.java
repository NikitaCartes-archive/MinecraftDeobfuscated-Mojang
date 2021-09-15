/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.util.profiling.jfr.Percentiles;
import net.minecraft.util.profiling.jfr.stats.TimedStat;
import org.jetbrains.annotations.Nullable;

public record TimedStatSummary(T fastest, T slowest, @Nullable T secondSlowest, int count, Map<Integer, Double> percentilesNanos, Duration totalDuration) {
    public static <T extends TimedStat> TimedStatSummary<T> summary(List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("No values");
        }
        List<TimedStat> list2 = list.stream().sorted(Comparator.comparing(TimedStat::duration)).toList();
        Duration duration = list2.stream().map(TimedStat::duration).reduce(Duration::plus).orElse(Duration.ZERO);
        TimedStat timedStat2 = list2.get(0);
        TimedStat timedStat22 = list2.get(list2.size() - 1);
        TimedStat timedStat3 = list2.size() > 1 ? list2.get(list2.size() - 2) : null;
        int i = list2.size();
        Map<Integer, Double> map = Percentiles.evaluate(list2.stream().mapToLong(timedStat -> timedStat.duration().toNanos()).toArray());
        return new TimedStatSummary(timedStat2, timedStat22, timedStat3, i, map, duration);
    }

    @Nullable
    public T secondSlowest() {
        return this.secondSlowest;
    }
}

