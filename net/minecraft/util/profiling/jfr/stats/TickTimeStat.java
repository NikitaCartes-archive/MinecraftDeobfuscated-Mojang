/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.jfr.stats;

import java.time.Instant;
import jdk.jfr.consumer.RecordedEvent;

public record TickTimeStat(Instant timestamp, float currentAverage) {
    public static TickTimeStat from(RecordedEvent recordedEvent) {
        return new TickTimeStat(recordedEvent.getStartTime(), recordedEvent.getFloat("averageTickMs"));
    }
}

