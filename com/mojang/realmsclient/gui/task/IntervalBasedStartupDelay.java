/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.task;

import com.mojang.realmsclient.gui.task.RestartDelayCalculator;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class IntervalBasedStartupDelay
implements RestartDelayCalculator {
    private final Duration interval;
    private final Supplier<Clock> clock;
    @Nullable
    private Instant lastStartedTimestamp;

    public IntervalBasedStartupDelay(Duration duration) {
        this.interval = duration;
        this.clock = Clock::systemUTC;
    }

    @Override
    public void markExecutionStart() {
        this.lastStartedTimestamp = Instant.now(this.clock.get());
    }

    @Override
    public long getNextDelayMs() {
        if (this.lastStartedTimestamp == null) {
            return 0L;
        }
        return Math.max(0L, Duration.between(Instant.now(this.clock.get()), this.lastStartedTimestamp.plus(this.interval)).toMillis());
    }
}

