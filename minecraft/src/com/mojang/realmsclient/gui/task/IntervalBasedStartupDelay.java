package com.mojang.realmsclient.gui.task;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class IntervalBasedStartupDelay implements RestartDelayCalculator {
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
		this.lastStartedTimestamp = Instant.now((Clock)this.clock.get());
	}

	@Override
	public long getNextDelayMs() {
		return this.lastStartedTimestamp == null
			? 0L
			: Math.max(0L, Duration.between(Instant.now((Clock)this.clock.get()), this.lastStartedTimestamp.plus(this.interval)).toMillis());
	}
}
