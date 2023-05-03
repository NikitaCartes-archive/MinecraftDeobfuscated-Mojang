package net.minecraft.client.telemetry.events;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class GameLoadTimesEvent {
	public static final GameLoadTimesEvent INSTANCE = new GameLoadTimesEvent(Ticker.systemTicker());
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Ticker timeSource;
	private final Map<TelemetryProperty<GameLoadTimesEvent.Measurement>, Stopwatch> measurements = new HashMap();
	private OptionalLong bootstrapTime = OptionalLong.empty();

	protected GameLoadTimesEvent(Ticker ticker) {
		this.timeSource = ticker;
	}

	public synchronized void beginStep(TelemetryProperty<GameLoadTimesEvent.Measurement> telemetryProperty) {
		this.beginStep(telemetryProperty, telemetryPropertyx -> Stopwatch.createStarted(this.timeSource));
	}

	public synchronized void beginStep(TelemetryProperty<GameLoadTimesEvent.Measurement> telemetryProperty, Stopwatch stopwatch) {
		this.beginStep(telemetryProperty, telemetryPropertyx -> stopwatch);
	}

	private synchronized void beginStep(
		TelemetryProperty<GameLoadTimesEvent.Measurement> telemetryProperty, Function<TelemetryProperty<GameLoadTimesEvent.Measurement>, Stopwatch> function
	) {
		this.measurements.computeIfAbsent(telemetryProperty, function);
	}

	public synchronized void endStep(TelemetryProperty<GameLoadTimesEvent.Measurement> telemetryProperty) {
		Stopwatch stopwatch = (Stopwatch)this.measurements.get(telemetryProperty);
		if (stopwatch == null) {
			LOGGER.warn("Attempted to end step for {} before starting it", telemetryProperty.id());
		} else {
			if (stopwatch.isRunning()) {
				stopwatch.stop();
			}
		}
	}

	public void send(TelemetryEventSender telemetryEventSender) {
		telemetryEventSender.send(
			TelemetryEventType.GAME_LOAD_TIMES,
			builder -> {
				synchronized (this) {
					this.measurements
						.forEach(
							(telemetryProperty, stopwatch) -> {
								if (!stopwatch.isRunning()) {
									long l = stopwatch.elapsed(TimeUnit.MILLISECONDS);
									builder.put(telemetryProperty, new GameLoadTimesEvent.Measurement((int)l));
								} else {
									LOGGER.warn(
										"Measurement {} was discarded since it was still ongoing when the event {} was sent.",
										telemetryProperty.id(),
										TelemetryEventType.GAME_LOAD_TIMES.id()
									);
								}
							}
						);
					this.bootstrapTime.ifPresent(l -> builder.put(TelemetryProperty.LOAD_TIME_BOOTSTRAP_MS, new GameLoadTimesEvent.Measurement((int)l)));
					this.measurements.clear();
				}
			}
		);
	}

	public synchronized void setBootstrapTime(long l) {
		this.bootstrapTime = OptionalLong.of(l);
	}

	@Environment(EnvType.CLIENT)
	public static record Measurement(int millis) {
		public static final Codec<GameLoadTimesEvent.Measurement> CODEC = Codec.INT.xmap(GameLoadTimesEvent.Measurement::new, measurement -> measurement.millis);
	}
}
