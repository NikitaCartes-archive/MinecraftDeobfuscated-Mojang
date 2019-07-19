package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.IOException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class PacketEncoder extends MessageToByteEncoder<Packet<?>> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Marker MARKER = MarkerManager.getMarker("PACKET_SENT", Connection.PACKET_MARKER);
	private final PacketFlow flow;

	public PacketEncoder(PacketFlow packetFlow) {
		this.flow = packetFlow;
	}

	protected void encode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, ByteBuf byteBuf) throws Exception {
		ConnectionProtocol connectionProtocol = channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get();
		if (connectionProtocol == null) {
			throw new RuntimeException("ConnectionProtocol unknown: " + packet);
		} else {
			Integer integer = connectionProtocol.getPacketId(this.flow, packet);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(MARKER, "OUT: [{}:{}] {}", channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get(), integer, packet.getClass().getName());
			}

			if (integer == null) {
				throw new IOException("Can't serialize unregistered packet");
			} else {
				FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf);
				friendlyByteBuf.writeVarInt(integer);

				try {
					packet.write(friendlyByteBuf);
				} catch (Throwable var8) {
					LOGGER.error(var8);
					if (packet.isSkippable()) {
						throw new SkipPacketException(var8);
					} else {
						throw var8;
					}
				}
			}
		}
	}
}
