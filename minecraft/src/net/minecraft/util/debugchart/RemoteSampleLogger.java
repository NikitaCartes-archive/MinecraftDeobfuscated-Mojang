package net.minecraft.util.debugchart;

import net.minecraft.network.protocol.game.ClientboundDebugSamplePacket;

public class RemoteSampleLogger extends AbstractSampleLogger {
	private final DebugSampleSubscriptionTracker subscriptionTracker;
	private final RemoteDebugSampleType sampleType;

	public RemoteSampleLogger(int i, DebugSampleSubscriptionTracker debugSampleSubscriptionTracker, RemoteDebugSampleType remoteDebugSampleType) {
		this(i, debugSampleSubscriptionTracker, remoteDebugSampleType, new long[i]);
	}

	public RemoteSampleLogger(int i, DebugSampleSubscriptionTracker debugSampleSubscriptionTracker, RemoteDebugSampleType remoteDebugSampleType, long[] ls) {
		super(i, ls);
		this.subscriptionTracker = debugSampleSubscriptionTracker;
		this.sampleType = remoteDebugSampleType;
	}

	@Override
	protected void useSample() {
		this.subscriptionTracker.broadcast(new ClientboundDebugSamplePacket((long[])this.sample.clone(), this.sampleType));
	}
}
