package net.minecraft.network;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.SampleLogger;

public class BandwidthDebugMonitor {
	private final AtomicInteger bytesReceived = new AtomicInteger();
	private final SampleLogger bandwidthLogger;

	public BandwidthDebugMonitor(SampleLogger sampleLogger) {
		this.bandwidthLogger = sampleLogger;
	}

	public void onReceive(int i) {
		this.bytesReceived.getAndAdd(i);
	}

	public void tick() {
		this.bandwidthLogger.logSample((long)this.bytesReceived.getAndSet(0));
	}
}
