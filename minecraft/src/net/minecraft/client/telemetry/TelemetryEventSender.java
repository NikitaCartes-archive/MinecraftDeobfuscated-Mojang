package net.minecraft.client.telemetry;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface TelemetryEventSender {
	TelemetryEventSender DISABLED = (telemetryEventType, consumer) -> {
	};

	default TelemetryEventSender decorate(Consumer<TelemetryPropertyMap.Builder> consumer) {
		return (telemetryEventType, consumer2) -> this.send(telemetryEventType, builder -> {
				consumer2.accept(builder);
				consumer.accept(builder);
			});
	}

	void send(TelemetryEventType telemetryEventType, Consumer<TelemetryPropertyMap.Builder> consumer);
}
