package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.IOException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketEncoder extends MessageToByteEncoder<Packet<?>> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final PacketFlow flow;

	public PacketEncoder(PacketFlow packetFlow) {
		this.flow = packetFlow;
	}

	protected void encode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, ByteBuf byteBuf) throws Exception {
		ConnectionProtocol connectionProtocol = channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get();
		if (connectionProtocol == null) {
			throw new RuntimeException("ConnectionProtocol unknown: " + packet);
		} else {
			int i = connectionProtocol.getPacketId(this.flow, packet);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(
					Connection.PACKET_SENT_MARKER,
					"OUT: [{}:{}] {}",
					channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get(),
					i,
					packet.getClass().getName()
				);
			}

			if (i == -1) {
				throw new IOException("Can't serialize unregistered packet");
			} else {
				FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf);
				friendlyByteBuf.writeVarInt(i);

				try {
					int j = friendlyByteBuf.writerIndex();
					packet.write(friendlyByteBuf);
					int k = friendlyByteBuf.writerIndex() - j;
					if (k > 8388608) {
						throw new IllegalArgumentException("Packet too big (is " + k + ", should be less than 8388608): " + packet);
					} else {
						int l = channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getId();
						JvmProfiler.INSTANCE.onPacketSent(l, i, channelHandlerContext.channel().remoteAddress(), k);
					}
				} catch (Throwable var10) {
					LOGGER.error("Error receiving packet {}", i, var10);
					if (packet.isSkippable()) {
						throw new SkipPacketException(var10);
					} else {
						throw var10;
					}
				}
			}
		}
	}
}
