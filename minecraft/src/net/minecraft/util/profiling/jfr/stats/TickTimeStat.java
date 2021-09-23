package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.time.Instant;
import jdk.jfr.consumer.RecordedEvent;

public record TickTimeStat() {
	private final Instant timestamp;
	private final Duration currentAverage;

	public TickTimeStat(Instant instant, Duration duration) {
		this.timestamp = instant;
		this.currentAverage = duration;
	}

	public static TickTimeStat from(RecordedEvent recordedEvent) {
		return new TickTimeStat(recordedEvent.getStartTime(), recordedEvent.getDuration("averageTickDuration"));
	}
}
