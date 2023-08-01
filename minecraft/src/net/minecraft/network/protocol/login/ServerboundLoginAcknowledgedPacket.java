package net.minecraft.network.protocol.login;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundLoginAcknowledgedPacket() implements Packet<ServerLoginPacketListener> {
	public ServerboundLoginAcknowledgedPacket(FriendlyByteBuf friendlyByteBuf) {
		this();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
	}

	public void handle(ServerLoginPacketListener serverLoginPacketListener) {
		serverLoginPacketListener.handleLoginAcknowledgement(this);
	}

	@Override
	public ConnectionProtocol nextProtocol() {
		return ConnectionProtocol.CONFIGURATION;
	}
}
