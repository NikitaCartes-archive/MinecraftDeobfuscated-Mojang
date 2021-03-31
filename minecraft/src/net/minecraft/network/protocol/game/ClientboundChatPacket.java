package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundChatPacket implements Packet<ClientGamePacketListener> {
	private final Component message;
	private final ChatType type;
	private final UUID sender;

	public ClientboundChatPacket(Component component, ChatType chatType, UUID uUID) {
		this.message = component;
		this.type = chatType;
		this.sender = uUID;
	}

	public ClientboundChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this.message = friendlyByteBuf.readComponent();
		this.type = ChatType.getForIndex(friendlyByteBuf.readByte());
		this.sender = friendlyByteBuf.readUUID();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.message);
		friendlyByteBuf.writeByte(this.type.getIndex());
		friendlyByteBuf.writeUUID(this.sender);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleChat(this);
	}

	public Component getMessage() {
		return this.message;
	}

	public ChatType getType() {
		return this.type;
	}

	public UUID getSender() {
		return this.sender;
	}

	@Override
	public boolean isSkippable() {
		return true;
	}
}
