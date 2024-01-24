package net.minecraft.client.multiplayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.util.debugchart.SampleLogger;

@Environment(EnvType.CLIENT)
public class PingDebugMonitor {
	private final ClientPacketListener connection;
	private final SampleLogger delayTimer;

	public PingDebugMonitor(ClientPacketListener clientPacketListener, SampleLogger sampleLogger) {
		this.connection = clientPacketListener;
		this.delayTimer = sampleLogger;
	}

	public void tick() {
		this.connection.send(new ServerboundPingRequestPacket(Util.getMillis()));
	}

	public void onPongReceived(ClientboundPongResponsePacket clientboundPongResponsePacket) {
		this.delayTimer.logSample(Util.getMillis() - clientboundPongResponsePacket.time());
	}
}
