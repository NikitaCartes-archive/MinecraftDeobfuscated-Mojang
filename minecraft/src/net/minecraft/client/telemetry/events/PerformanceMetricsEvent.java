package net.minecraft.client.telemetry.events;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;

@Environment(EnvType.CLIENT)
public final class PerformanceMetricsEvent extends AggregatedTelemetryEvent {
	private static final long DEDICATED_MEMORY_KB = toKilobytes(Runtime.getRuntime().maxMemory());
	private final LongList fpsSamples = new LongArrayList();
	private final LongList frameTimeSamples = new LongArrayList();
	private final LongList usedMemorySamples = new LongArrayList();

	@Override
	public void tick(TelemetryEventSender telemetryEventSender) {
		if (Minecraft.getInstance().telemetryOptInExtra()) {
			super.tick(telemetryEventSender);
		}
	}

	private void resetValues() {
		this.fpsSamples.clear();
		this.frameTimeSamples.clear();
		this.usedMemorySamples.clear();
	}

	@Override
	public void takeSample() {
		this.fpsSamples.add((long)Minecraft.getInstance().getFps());
		this.takeUsedMemorySample();
		this.frameTimeSamples.add(Minecraft.getInstance().getFrameTimeNs());
	}

	private void takeUsedMemorySample() {
		long l = Runtime.getRuntime().totalMemory();
		long m = Runtime.getRuntime().freeMemory();
		long n = l - m;
		this.usedMemorySamples.add(toKilobytes(n));
	}

	@Override
	public void sendEvent(TelemetryEventSender telemetryEventSender) {
		telemetryEventSender.send(TelemetryEventType.PERFORMANCE_METRICS, builder -> {
			builder.put(TelemetryProperty.FRAME_RATE_SAMPLES, new LongArrayList(this.fpsSamples));
			builder.put(TelemetryProperty.RENDER_TIME_SAMPLES, new LongArrayList(this.frameTimeSamples));
			builder.put(TelemetryProperty.USED_MEMORY_SAMPLES, new LongArrayList(this.usedMemorySamples));
			builder.put(TelemetryProperty.NUMBER_OF_SAMPLES, this.getSampleCount());
			builder.put(TelemetryProperty.RENDER_DISTANCE, Minecraft.getInstance().options.getEffectiveRenderDistance());
			builder.put(TelemetryProperty.DEDICATED_MEMORY_KB, (int)DEDICATED_MEMORY_KB);
		});
		this.resetValues();
	}

	private static long toKilobytes(long l) {
		return l / 1000L;
	}
}
