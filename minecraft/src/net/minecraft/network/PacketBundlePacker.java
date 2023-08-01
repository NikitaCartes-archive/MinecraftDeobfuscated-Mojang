package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.AttributeKey;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;

public class PacketBundlePacker extends MessageToMessageDecoder<Packet<?>> {
	@Nullable
	private BundlerInfo.Bundler currentBundler;
	@Nullable
	private BundlerInfo infoForCurrentBundler;
	private final AttributeKey<? extends BundlerInfo.Provider> bundlerAttributeKey;

	public PacketBundlePacker(AttributeKey<? extends BundlerInfo.Provider> attributeKey) {
		this.bundlerAttributeKey = attributeKey;
	}

	protected void decode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
		BundlerInfo.Provider provider = channelHandlerContext.channel().attr(this.bundlerAttributeKey).get();
		if (provider == null) {
			throw new DecoderException("Bundler not configured: " + packet);
		} else {
			BundlerInfo bundlerInfo = provider.bundlerInfo();
			if (this.currentBundler != null) {
				if (this.infoForCurrentBundler != bundlerInfo) {
					throw new DecoderException("Bundler handler changed during bundling");
				}

				Packet<?> packet2 = this.currentBundler.addPacket(packet);
				if (packet2 != null) {
					this.infoForCurrentBundler = null;
					this.currentBundler = null;
					list.add(packet2);
				}
			} else {
				BundlerInfo.Bundler bundler = bundlerInfo.startPacketBundling(packet);
				if (bundler != null) {
					this.currentBundler = bundler;
					this.infoForCurrentBundler = bundlerInfo;
				} else {
					list.add(packet);
				}
			}
		}
	}
}
