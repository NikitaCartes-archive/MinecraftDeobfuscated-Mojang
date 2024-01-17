package net.minecraft.network.protocol.configuration;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundFinishConfigurationPacket implements Packet<ClientConfigurationPacketListener> {
	public static final ClientboundFinishConfigurationPacket INSTANCE = new ClientboundFinishConfigurationPacket();
	public static final StreamCodec<ByteBuf, ClientboundFinishConfigurationPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private ClientboundFinishConfigurationPacket() {
	}

	@Override
	public PacketType<ClientboundFinishConfigurationPacket> type() {
		return ConfigurationPacketTypes.CLIENTBOUND_FINISH_CONFIGURATION;
	}

	public void handle(ClientConfigurationPacketListener clientConfigurationPacketListener) {
		clientConfigurationPacketListener.handleConfigurationFinished(this);
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
}
