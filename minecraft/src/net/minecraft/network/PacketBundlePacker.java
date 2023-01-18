package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

public class PacketBundlePacker extends MessageToMessageDecoder<Packet<?>> {
	@Nullable
	private BundlerInfo.Bundler currentBundler;
	@Nullable
	private BundlerInfo infoForCurrentBundler;
	private final PacketFlow flow;

	public PacketBundlePacker(PacketFlow packetFlow) {
		this.flow = packetFlow;
	}

	protected void decode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
		BundlerInfo.Provider provider = channelHandlerContext.channel().attr(BundlerInfo.BUNDLER_PROVIDER).get();
		if (provider == null) {
			throw new DecoderException("Bundler not configured: " + packet);
		} else {
			BundlerInfo bundlerInfo = provider.getBundlerInfo(this.flow);
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
