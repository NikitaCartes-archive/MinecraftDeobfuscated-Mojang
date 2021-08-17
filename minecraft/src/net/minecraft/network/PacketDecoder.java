package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.util.profiling.jfr.event.network.PacketReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class PacketDecoder extends ByteToMessageDecoder {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Marker MARKER = MarkerManager.getMarker("PACKET_RECEIVED", Connection.PACKET_MARKER);
	private final PacketFlow flow;

	public PacketDecoder(PacketFlow packetFlow) {
		this.flow = packetFlow;
	}

	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
		int i = byteBuf.readableBytes();
		if (i != 0) {
			int j = byteBuf.readerIndex();
			FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf);
			int k = friendlyByteBuf.readVarInt();
			Packet<?> packet = channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().createPacket(this.flow, k, friendlyByteBuf);
			int l = byteBuf.readerIndex() - j;
			if (packet == null) {
				throw new IOException("Bad packet id " + k);
			} else {
				PacketReceivedEvent packetReceivedEvent = (PacketReceivedEvent)PacketReceivedEvent.EVENT.get();
				if (packetReceivedEvent.isEnabled() && packetReceivedEvent.shouldCommit()) {
					packetReceivedEvent.packetName = channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getId()
						+ "/"
						+ k
						+ " ("
						+ packet.getClass().getSimpleName()
						+ ")";
					packetReceivedEvent.bytes = l;
					packetReceivedEvent.commit();
					packetReceivedEvent.reset();
				}

				if (friendlyByteBuf.readableBytes() > 0) {
					throw new IOException(
						"Packet "
							+ channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getId()
							+ "/"
							+ k
							+ " ("
							+ packet.getClass().getSimpleName()
							+ ") was larger than I expected, found "
							+ friendlyByteBuf.readableBytes()
							+ " bytes extra whilst reading packet "
							+ k
					);
				} else {
					list.add(packet);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(MARKER, " IN: [{}:{}] {}", channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get(), k, packet.getClass().getName());
					}
				}
			}
		}
	}
}
