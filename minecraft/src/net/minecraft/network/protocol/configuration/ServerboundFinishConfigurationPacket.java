package net.minecraft.network.protocol.configuration;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundFinishConfigurationPacket() implements Packet<ServerConfigurationPacketListener> {
	public ServerboundFinishConfigurationPacket(FriendlyByteBuf friendlyByteBuf) {
		this();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
	}

	public void handle(ServerConfigurationPacketListener serverConfigurationPacketListener) {
		serverConfigurationPacketListener.handleConfigurationFinished(this);
	}

	@Override
	public ConnectionProtocol nextProtocol() {
		return ConnectionProtocol.PLAY;
	}
}
