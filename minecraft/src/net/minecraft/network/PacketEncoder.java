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
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(Connection.PACKET_SENT_MARKER, "OUT: [{}:{}] {}", this.protocolInfo.id().id(), packetType, packet.getClass().getName());
		}

		try {
			int i = byteBuf.writerIndex();
			this.protocolInfo.codec().encode(byteBuf, packet);
			int j = byteBuf.writerIndex() - i;
			if (j > 8388608) {
				throw new IllegalArgumentException("Packet too big (is " + j + ", should be less than 8388608): " + packet);
			}

			JvmProfiler.INSTANCE.onPacketSent(this.protocolInfo.id(), packetType, channelHandlerContext.channel().remoteAddress(), j);
		} catch (Throwable var10) {
			LOGGER.error("Error receiving packet {}", packetType, var10);
			if (packet.isSkippable()) {
				throw new SkipPacketException(var10);
			}

			throw var10;
		} finally {
			ProtocolSwapHandler.handleOutboundTerminalPacket(channelHandlerContext, packet);
		}
	}
}
