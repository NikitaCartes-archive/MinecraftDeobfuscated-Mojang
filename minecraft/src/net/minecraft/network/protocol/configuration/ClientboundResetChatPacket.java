package net.minecraft.network.protocol.configuration;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundResetChatPacket implements Packet<ClientConfigurationPacketListener> {
	public static final ClientboundResetChatPacket INSTANCE = new ClientboundResetChatPacket();
	public static final StreamCodec<ByteBuf, ClientboundResetChatPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private ClientboundResetChatPacket() {
	}

	@Override
	public PacketType<ClientboundResetChatPacket> type() {
		return ConfigurationPacketTypes.CLIENTBOUND_RESET_CHAT;
	}

	public void handle(ClientConfigurationPacketListener clientConfigurationPacketListener) {
		clientConfigurationPacketListener.handleResetChat(this);
	}
}
