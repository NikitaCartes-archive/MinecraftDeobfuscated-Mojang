package net.minecraft.client.telemetry.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.telemetry.TelemetryEventSender;

@Environment(EnvType.CLIENT)
public interface TelemetryEventProducer {
	void send(TelemetryEventSender telemetryEventSender);
}
