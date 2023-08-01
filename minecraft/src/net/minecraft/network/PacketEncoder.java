package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.io.IOException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketEncoder extends MessageToByteEncoder<Packet<?>> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final AttributeKey<ConnectionProtocol.CodecData<?>> codecKey;

	public PacketEncoder(AttributeKey<ConnectionProtocol.CodecData<?>> attributeKey) {
		this.codecKey = attributeKey;
	}

	protected void encode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, ByteBuf byteBuf) throws Exception {
		Attribute<ConnectionProtocol.CodecData<?>> attribute = channelHandlerContext.channel().attr(this.codecKey);
		ConnectionProtocol.CodecData<?> codecData = attribute.get();
		if (codecData == null) {
			throw new RuntimeException("ConnectionProtocol unknown: " + packet);
		} else {
			int i = codecData.packetId(packet);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(Connection.PACKET_SENT_MARKER, "OUT: [{}:{}] {}", codecData.protocol().id(), i, packet.getClass().getName());
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
					}

					JvmProfiler.INSTANCE.onPacketSent(codecData.protocol(), i, channelHandlerContext.channel().remoteAddress(), k);
				} catch (Throwable var13) {
					LOGGER.error("Error receiving packet {}", i, var13);
					if (packet.isSkippable()) {
						throw new SkipPacketException(var13);
					}

					throw var13;
				} finally {
					ProtocolSwapHandler.swapProtocolIfNeeded(attribute, packet);
				}
			}
		}
	}
}
