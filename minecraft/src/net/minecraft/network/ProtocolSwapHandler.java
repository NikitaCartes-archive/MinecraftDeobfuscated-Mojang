package net.minecraft.network;

import io.netty.util.Attribute;
import net.minecraft.network.protocol.Packet;

public interface ProtocolSwapHandler {
	static void swapProtocolIfNeeded(Attribute<ConnectionProtocol.CodecData<?>> attribute, Packet<?> packet) {
		ConnectionProtocol connectionProtocol = packet.nextProtocol();
		if (connectionProtocol != null) {
			ConnectionProtocol.CodecData<?> codecData = attribute.get();
			ConnectionProtocol connectionProtocol2 = codecData.protocol();
			if (connectionProtocol != connectionProtocol2) {
				ConnectionProtocol.CodecData<?> codecData2 = connectionProtocol.codec(codecData.flow());
				attribute.set(codecData2);
			}
		}
	}
}
