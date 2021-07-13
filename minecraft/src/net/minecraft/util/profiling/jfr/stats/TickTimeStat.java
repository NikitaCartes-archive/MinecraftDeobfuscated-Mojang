package net.minecraft.util.profiling.jfr.stats;

import java.time.Instant;
import jdk.jfr.consumer.RecordedEvent;

public class TickTimeStat {
	public final Instant timestamp;
	public final float currentAverage;

	public TickTimeStat(RecordedEvent recordedEvent) {
		this.timestamp = recordedEvent.getStartTime();
		this.currentAverage = recordedEvent.getFloat("averageTickMs");
	}
}
