/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.metrics.storage;

import java.time.Instant;
import net.minecraft.util.profiling.ProfileResults;

public final class RecordedDeviation {
    public final Instant timestamp;
    public final int tick;
    public final ProfileResults profilerResultAtTick;

    public RecordedDeviation(Instant instant, int i, ProfileResults profileResults) {
        this.timestamp = instant;
        this.tick = i;
        this.profilerResultAtTick = profileResults;
    }
}

