package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public record ClientboundSystemChatPacket(Component content, boolean overlay) implements Packet<ClientGamePacketListener> {
	public ClientboundSystemChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readComponentTrusted(), friendlyByteBuf.readBoolean());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.content);
		friendlyByteBuf.writeBoolean(this.overlay);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSystemChat(this);
	}

	@Override
	public boolean isSkippable() {
		return true;
	}
}
