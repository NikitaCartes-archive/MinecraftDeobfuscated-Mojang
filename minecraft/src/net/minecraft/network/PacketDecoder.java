package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketDecoder extends ByteToMessageDecoder implements ProtocolSwapHandler {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final AttributeKey<ConnectionProtocol.CodecData<?>> codecKey;

	public PacketDecoder(AttributeKey<ConnectionProtocol.CodecData<?>> attributeKey) {
		this.codecKey = attributeKey;
	}

	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
		int i = byteBuf.readableBytes();
		if (i != 0) {
			Attribute<ConnectionProtocol.CodecData<?>> attribute = channelHandlerContext.channel().attr(this.codecKey);
			ConnectionProtocol.CodecData<?> codecData = attribute.get();
			FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf);
			int j = friendlyByteBuf.readVarInt();
			Packet<?> packet = codecData.createPacket(j, friendlyByteBuf);
			if (packet == null) {
				throw new IOException("Bad packet id " + j);
			} else {
				JvmProfiler.INSTANCE.onPacketReceived(codecData.protocol(), j, channelHandlerContext.channel().remoteAddress(), i);
				if (friendlyByteBuf.readableBytes() > 0) {
					throw new IOException(
						"Packet "
							+ codecData.protocol().id()
							+ "/"
							+ j
							+ " ("
							+ packet.getClass().getSimpleName()
							+ ") was larger than I expected, found "
							+ friendlyByteBuf.readableBytes()
							+ " bytes extra whilst reading packet "
							+ j
					);
				} else {
					list.add(packet);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(Connection.PACKET_RECEIVED_MARKER, " IN: [{}:{}] {}", codecData.protocol().id(), j, packet.getClass().getName());
					}

					ProtocolSwapHandler.swapProtocolIfNeeded(attribute, packet);
				}
			}
		}
	}
}
