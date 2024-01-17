package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketDecoder<T extends PacketListener> extends ByteToMessageDecoder implements ProtocolSwapHandler {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ProtocolInfo<T> protocolInfo;

	public PacketDecoder(ProtocolInfo<T> protocolInfo) {
		this.protocolInfo = protocolInfo;
	}

	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
		int i = byteBuf.readableBytes();
		if (i != 0) {
			Packet<? super T> packet = this.protocolInfo.codec().decode(byteBuf);
			PacketType<? extends Packet<? super T>> packetType = packet.type();
			JvmProfiler.INSTANCE.onPacketReceived(this.protocolInfo.id(), packetType, channelHandlerContext.channel().remoteAddress(), i);
			if (byteBuf.readableBytes() > 0) {
				throw new IOException(
					"Packet "
						+ this.protocolInfo.id().id()
						+ "/"
						+ packetType
						+ " ("
						+ packet.getClass().getSimpleName()
						+ ") was larger than I expected, found "
						+ byteBuf.readableBytes()
						+ " bytes extra whilst reading packet "
						+ packetType
				);
			} else {
				list.add(packet);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(Connection.PACKET_RECEIVED_MARKER, " IN: [{}:{}] {}", this.protocolInfo.id().id(), packetType, packet.getClass().getName());
				}

				ProtocolSwapHandler.handleInboundTerminalPacket(channelHandlerContext, packet);
			}
		}
	}
}
