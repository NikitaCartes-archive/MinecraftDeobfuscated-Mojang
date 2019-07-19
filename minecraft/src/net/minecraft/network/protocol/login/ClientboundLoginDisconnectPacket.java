package net.minecraft.network.protocol.login;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundLoginDisconnectPacket implements Packet<ClientLoginPacketListener> {
	private Component reason;

	public ClientboundLoginDisconnectPacket() {
	}

	public ClientboundLoginDisconnectPacket(Component component) {
		this.reason = component;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.reason = Component.Serializer.fromJsonLenient(friendlyByteBuf.readUtf(262144));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeComponent(this.reason);
	}

	public void handle(ClientLoginPacketListener clientLoginPacketListener) {
		clientLoginPacketListener.handleDisconnect(this);
	}

	@Environment(EnvType.CLIENT)
	public Component getReason() {
		return this.reason;
	}
}
