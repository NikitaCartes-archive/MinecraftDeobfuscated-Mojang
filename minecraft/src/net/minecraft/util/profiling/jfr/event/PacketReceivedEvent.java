package net.minecraft.util.profiling.jfr.event;

import java.net.SocketAddress;
import jdk.jfr.EventType;
import jdk.jfr.Label;
import jdk.jfr.Name;
import net.minecraft.obfuscate.DontObfuscate;

@Name("minecraft.PacketReceived")
@Label("Network Packet Received")
@DontObfuscate
public class PacketReceivedEvent extends PacketEvent {
	public static final String NAME = "minecraft.PacketReceived";
	public static final EventType TYPE = EventType.getEventType(PacketReceivedEvent.class);

	public PacketReceivedEvent(String string, String string2, String string3, SocketAddress socketAddress, int i) {
		super(string, string2, string3, socketAddress, i);
	}
}
