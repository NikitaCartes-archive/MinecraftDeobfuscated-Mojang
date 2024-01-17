package net.minecraft.network.protocol.configuration;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundFinishConfigurationPacket implements Packet<ServerConfigurationPacketListener> {
	public static final ServerboundFinishConfigurationPacket INSTANCE = new ServerboundFinishConfigurationPacket();
	public static final StreamCodec<ByteBuf, ServerboundFinishConfigurationPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private ServerboundFinishConfigurationPacket() {
	}

	@Override
	public PacketType<ServerboundFinishConfigurationPacket> type() {
		return ConfigurationPacketTypes.SERVERBOUND_FINISH_CONFIGURATION;
	}

	public void handle(ServerConfigurationPacketListener serverConfigurationPacketListener) {
		serverConfigurationPacketListener.handleConfigurationFinished(this);
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
}
