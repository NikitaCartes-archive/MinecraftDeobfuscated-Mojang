package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.AttributeKey;
import java.util.List;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;

public class PacketBundleUnpacker extends MessageToMessageEncoder<Packet<?>> {
	private final AttributeKey<? extends BundlerInfo.Provider> bundlerAttributeKey;

	public PacketBundleUnpacker(AttributeKey<? extends BundlerInfo.Provider> attributeKey) {
		this.bundlerAttributeKey = attributeKey;
	}

	protected void encode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
		BundlerInfo.Provider provider = channelHandlerContext.channel().attr(this.bundlerAttributeKey).get();
		if (provider == null) {
			throw new EncoderException("Bundler not configured: " + packet);
		} else {
			provider.bundlerInfo().unbundlePacket(packet, list::add);
		}
	}
}
