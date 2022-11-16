package net.minecraft.client.telemetry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface TelemetryEventLogger {
	void log(TelemetryEventInstance telemetryEventInstance);
}
