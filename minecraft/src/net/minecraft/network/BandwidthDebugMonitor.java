package net.minecraft.network;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.debugchart.LocalSampleLogger;

public class BandwidthDebugMonitor {
	private final AtomicInteger bytesReceived = new AtomicInteger();
	private final LocalSampleLogger bandwidthLogger;

	public BandwidthDebugMonitor(LocalSampleLogger localSampleLogger) {
		this.bandwidthLogger = localSampleLogger;
	}

	public void onReceive(int i) {
		this.bytesReceived.getAndAdd(i);
	}

	public void tick() {
		this.bandwidthLogger.logSample((long)this.bytesReceived.getAndSet(0));
	}
}
