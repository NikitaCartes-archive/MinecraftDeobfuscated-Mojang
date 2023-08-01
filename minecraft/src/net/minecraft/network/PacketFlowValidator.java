package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import org.slf4j.Logger;

public class PacketFlowValidator extends MessageToMessageCodec<Packet<?>, Packet<?>> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final AttributeKey<ConnectionProtocol.CodecData<?>> decoderKey;
	private final AttributeKey<ConnectionProtocol.CodecData<?>> encoderKey;

	public PacketFlowValidator(AttributeKey<ConnectionProtocol.CodecData<?>> attributeKey, AttributeKey<ConnectionProtocol.CodecData<?>> attributeKey2) {
		this.decoderKey = attributeKey;
		this.encoderKey = attributeKey2;
	}

	private static void validatePacket(
		ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list, AttributeKey<ConnectionProtocol.CodecData<?>> attributeKey
	) {
		Attribute<ConnectionProtocol.CodecData<?>> attribute = channelHandlerContext.channel().attr(attributeKey);
		ConnectionProtocol.CodecData<?> codecData = attribute.get();
		if (!codecData.isValidPacketType(packet)) {
			LOGGER.error("Unrecognized packet in pipeline {}:{} - {}", codecData.protocol().id(), codecData.flow(), packet);
		}

		ReferenceCountUtil.retain(packet);
		list.add(packet);
		ProtocolSwapHandler.swapProtocolIfNeeded(attribute, packet);
	}

	protected void decode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
		validatePacket(channelHandlerContext, packet, list, this.decoderKey);
	}

	protected void encode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
		validatePacket(channelHandlerContext, packet, list, this.encoderKey);
	}
}
