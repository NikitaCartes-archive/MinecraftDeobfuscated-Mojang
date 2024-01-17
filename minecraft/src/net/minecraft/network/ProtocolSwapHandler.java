package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.protocol.Packet;

public interface ProtocolSwapHandler {
	static void handleInboundTerminalPacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet) {
		if (packet.isTerminal()) {
			channelHandlerContext.channel().config().setAutoRead(false);
			channelHandlerContext.pipeline().addBefore(channelHandlerContext.name(), "inbound_config", new UnconfiguredPipelineHandler.Inbound());
			channelHandlerContext.pipeline().remove(channelHandlerContext.name());
		}
	}

	static void handleOutboundTerminalPacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet) {
		if (packet.isTerminal()) {
			channelHandlerContext.pipeline().addAfter(channelHandlerContext.name(), "outbound_config", new UnconfiguredPipelineHandler.Outbound());
			channelHandlerContext.pipeline().remove(channelHandlerContext.name());
		}
	}
}
