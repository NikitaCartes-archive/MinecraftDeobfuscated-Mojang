/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.jfr.stats;

import com.google.common.base.MoreObjects;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedThread;

public record ThreadAllocationStat(Instant timestamp, String threadName, long totalBytes) {
    private static final String UNKNOWN_THREAD = "unknown";

    public static ThreadAllocationStat from(RecordedEvent recordedEvent) {
        RecordedThread recordedThread = recordedEvent.getThread("thread");
        String string = recordedThread == null ? UNKNOWN_THREAD : MoreObjects.firstNonNull(recordedThread.getJavaName(), UNKNOWN_THREAD);
        return new ThreadAllocationStat(recordedEvent.getStartTime(), string, recordedEvent.getLong("allocated"));
    }

    public static Summary summary(List<ThreadAllocationStat> list2) {
        TreeMap<String, Double> map = new TreeMap<String, Double>();
        Map<String, List<ThreadAllocationStat>> map2 = list2.stream().collect(Collectors.groupingBy(threadAllocationStat -> threadAllocationStat.threadName));
        map2.forEach((string, list) -> {
            if (list.size() < 2) {
                return;
            }
            ThreadAllocationStat threadAllocationStat = (ThreadAllocationStat)list.get(0);
            ThreadAllocationStat threadAllocationStat2 = (ThreadAllocationStat)list.get(list.size() - 1);
            long l = Duration.between(threadAllocationStat.timestamp, threadAllocationStat2.timestamp).getSeconds();
            long m = threadAllocationStat2.totalBytes - threadAllocationStat.totalBytes;
            map.put((String)string, (double)m / (double)l);
        });
        return new Summary(map);
    }

    public record Summary(Map<String, Double> allocationsPerSecondByThread) {
    }
}

