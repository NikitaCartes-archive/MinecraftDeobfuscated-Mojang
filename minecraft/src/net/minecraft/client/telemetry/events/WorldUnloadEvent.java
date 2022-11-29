package net.minecraft.client.telemetry.events;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;

@Environment(EnvType.CLIENT)
public class WorldUnloadEvent {
	private static final int NOT_TRACKING_TIME = -1;
	private Optional<Instant> worldLoadedTime = Optional.empty();
	private long totalTicks;
	private long lastGameTime;

	public void onPlayerInfoReceived() {
		this.lastGameTime = -1L;
		if (this.worldLoadedTime.isEmpty()) {
			this.worldLoadedTime = Optional.of(Instant.now());
		}
	}

	public void setTime(long l) {
		if (this.lastGameTime != -1L) {
			this.totalTicks = this.totalTicks + Math.max(0L, l - this.lastGameTime);
		}

		this.lastGameTime = l;
	}

	private int getTimeInSecondsSinceLoad(Instant instant) {
		Duration duration = Duration.between(instant, Instant.now());
		return (int)duration.toSeconds();
	}

	public void send(TelemetryEventSender telemetryEventSender) {
		this.worldLoadedTime.ifPresent(instant -> telemetryEventSender.send(TelemetryEventType.WORLD_UNLOADED, builder -> {
				builder.put(TelemetryProperty.SECONDS_SINCE_LOAD, this.getTimeInSecondsSinceLoad(instant));
				builder.put(TelemetryProperty.TICKS_SINCE_LOAD, (int)this.totalTicks);
			}));
	}
}
