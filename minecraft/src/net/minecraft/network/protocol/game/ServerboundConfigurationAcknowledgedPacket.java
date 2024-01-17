package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundConfigurationAcknowledgedPacket implements Packet<ServerGamePacketListener> {
	public static final ServerboundConfigurationAcknowledgedPacket INSTANCE = new ServerboundConfigurationAcknowledgedPacket();
	public static final StreamCodec<ByteBuf, ServerboundConfigurationAcknowledgedPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private ServerboundConfigurationAcknowledgedPacket() {
	}

	@Override
	public PacketType<ServerboundConfigurationAcknowledgedPacket> type() {
		return GamePacketTypes.SERVERBOUND_CONFIGURATION_ACKNOWLEDGED;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleConfigurationAcknowledged(this);
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
}
