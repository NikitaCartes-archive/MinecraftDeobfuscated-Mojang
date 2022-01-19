package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketDecoder extends ByteToMessageDecoder {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final PacketFlow flow;

	public PacketDecoder(PacketFlow packetFlow) {
		this.flow = packetFlow;
	}

	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
		int i = byteBuf.readableBytes();
		if (i != 0) {
			FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf);
			int j = friendlyByteBuf.readVarInt();
			Packet<?> packet = channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().createPacket(this.flow, j, friendlyByteBuf);
			if (packet == null) {
				throw new IOException("Bad packet id " + j);
			} else {
				int k = channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getId();
				JvmProfiler.INSTANCE.onPacketReceived(k, j, channelHandlerContext.channel().remoteAddress(), i);
				if (friendlyByteBuf.readableBytes() > 0) {
					throw new IOException(
						"Packet "
							+ channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getId()
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
						LOGGER.debug(
							Connection.PACKET_RECEIVED_MARKER,
							" IN: [{}:{}] {}",
							channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get(),
							j,
							packet.getClass().getName()
						);
					}
				}
			}
		}
	}
}
