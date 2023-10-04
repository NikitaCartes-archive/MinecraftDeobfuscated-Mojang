package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundDisconnectPacket implements Packet<ClientCommonPacketListener> {
	private final Component reason;

	public ClientboundDisconnectPacket(Component component) {
		this.reason = component;
	}

	public ClientboundDisconnectPacket(FriendlyByteBuf friendlyByteBuf) {
		this.reason = friendlyByteBuf.readComponentTrusted();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.reason);
	}

	public void handle(ClientCommonPacketListener clientCommonPacketListener) {
		clientCommonPacketListener.handleDisconnect(this);
	}

	public Component getReason() {
		return this.reason;
	}
}
