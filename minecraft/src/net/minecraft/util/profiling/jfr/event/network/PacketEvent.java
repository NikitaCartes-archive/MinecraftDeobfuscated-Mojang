package net.minecraft.util.profiling.jfr.event.network;

import java.net.SocketAddress;
import jdk.jfr.Category;
import jdk.jfr.DataAmount;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@Category({"Minecraft", "Network"})
@StackTrace(false)
public abstract class PacketEvent extends Event {
	@Name("packetName")
	@Label("Packet name")
	public final String packetName;
	@Name("remoteAddress")
	@Label("Remote address")
	public final String remoteAddress;
	@Name("bytes")
	@Label("Bytes")
	@DataAmount
	public final int bytes;

	public PacketEvent(String string, SocketAddress socketAddress, int i) {
		this.packetName = string;
		this.remoteAddress = socketAddress.toString();
		this.bytes = i;
	}

	public static final class Fields {
		public static final String REMOTE_ADDRESS = "remoteAddress";
		public static final String PACKET_NAME = "packetName";
		public static final String BYTES = "bytes";

		private Fields() {
		}
	}
}
