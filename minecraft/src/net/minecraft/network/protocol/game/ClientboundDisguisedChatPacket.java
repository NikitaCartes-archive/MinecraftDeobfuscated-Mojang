package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public record ClientboundDisguisedChatPacket(Component message, ChatType.BoundNetwork chatType) implements Packet<ClientGamePacketListener> {
	public ClientboundDisguisedChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readComponentTrusted(), new ChatType.BoundNetwork(friendlyByteBuf));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.message);
		this.chatType.write(friendlyByteBuf);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleDisguisedChat(this);
	}

	@Override
	public boolean isSkippable() {
		return true;
	}
}
