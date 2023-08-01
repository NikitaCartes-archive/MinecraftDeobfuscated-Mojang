package net.minecraft.network.protocol.game;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundStartConfigurationPacket() implements Packet<ClientGamePacketListener> {
	public ClientboundStartConfigurationPacket(FriendlyByteBuf friendlyByteBuf) {
		this();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleConfigurationStart(this);
	}

	@Override
	public ConnectionProtocol nextProtocol() {
		return ConnectionProtocol.CONFIGURATION;
	}
}
