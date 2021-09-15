package net.minecraft.util.profiling.jfr.stats;

import java.time.Instant;
import jdk.jfr.consumer.RecordedEvent;

public record TickTimeStat() {
	private final Instant timestamp;
	private final float currentAverage;

	public TickTimeStat(Instant instant, float f) {
		this.timestamp = instant;
		this.currentAverage = f;
	}

	public static TickTimeStat from(RecordedEvent recordedEvent) {
		return new TickTimeStat(recordedEvent.getStartTime(), recordedEvent.getFloat("averageTickMs"));
	}
}
