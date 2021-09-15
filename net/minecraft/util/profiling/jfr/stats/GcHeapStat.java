/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;

public record GcHeapStat(Instant timestamp, long heapUsed, Timing timing) {
    public static GcHeapStat from(RecordedEvent recordedEvent) {
        return new GcHeapStat(recordedEvent.getStartTime(), recordedEvent.getLong("heapUsed"), recordedEvent.getString("when").equalsIgnoreCase("before gc") ? Timing.BEFORE_GC : Timing.AFTER_GC);
    }

    public static Summary summary(Duration duration, List<GcHeapStat> list, Duration duration2, int i) {
        return new Summary(duration, duration2, i, GcHeapStat.calculateAllocationRatePerSecond(list));
    }

    private static double calculateAllocationRatePerSecond(List<GcHeapStat> list) {
        long l = 0L;
        Map<Timing, List<GcHeapStat>> map = list.stream().collect(Collectors.groupingBy(gcHeapStat -> gcHeapStat.timing));
        List<GcHeapStat> list2 = map.get((Object)Timing.BEFORE_GC);
        List<GcHeapStat> list3 = map.get((Object)Timing.AFTER_GC);
        for (int i = 1; i < list2.size(); ++i) {
            GcHeapStat gcHeapStat2 = list2.get(i);
            GcHeapStat gcHeapStat22 = list3.get(i - 1);
            l += gcHeapStat2.heapUsed - gcHeapStat22.heapUsed;
        }
        Duration duration = Duration.between(list.get((int)1).timestamp, list.get((int)(list.size() - 1)).timestamp);
        return (double)l / (double)duration.getSeconds();
    }

    static enum Timing {
        BEFORE_GC,
        AFTER_GC;

    }

    public record Summary(Duration duration, Duration gcTotalDuration, int totalGCs, double allocationRateBytesPerSecond) {
        public float gcOverHead() {
            return (float)this.gcTotalDuration.toMillis() / (float)this.duration.toMillis();
        }
    }
}

