package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketEncoder<T extends PacketListener> extends MessageToByteEncoder<Packet<T>> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ProtocolInfo<T> protocolInfo;

	public PacketEncoder(ProtocolInfo<T> protocolInfo) {
		this.protocolInfo = protocolInfo;
	}

	protected void encode(ChannelHandlerContext channelHandlerContext, Packet<T> packet, ByteBuf byteBuf) throws Exception {
		PacketType<? extends Packet<? super T>> packetType = packet.type();

		try {
			this.protocolInfo.codec().encode(byteBuf, packet);
			int i = byteBuf.readableBytes();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(Connection.PACKET_SENT_MARKER, "OUT: [{}:{}] {} -> {} bytes", this.protocolInfo.id().id(), packetType, packet.getClass().getName(), i);
			}

			JvmProfiler.INSTANCE.onPacketSent(this.protocolInfo.id(), packetType, channelHandlerContext.channel().remoteAddress(), i);
		} catch (Throwable var9) {
			LOGGER.error("Error sending packet {}", packetType, var9);
			if (packet.isSkippable()) {
				throw new SkipPacketException(var9);
			}

			throw var9;
		} finally {
			ProtocolSwapHandler.handleOutboundTerminalPacket(channelHandlerContext, packet);
		}
	}
}
