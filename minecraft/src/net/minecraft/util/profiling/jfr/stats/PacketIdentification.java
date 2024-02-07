package net.minecraft.util.profiling.jfr.stats;

import jdk.jfr.consumer.RecordedEvent;

public record PacketIdentification(String direction, String protocolId, String packetId) {
	public static PacketIdentification from(RecordedEvent recordedEvent) {
		return new PacketIdentification(recordedEvent.getString("packetDirection"), recordedEvent.getString("protocolId"), recordedEvent.getString("packetId"));
	}
}
