package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundChatPacket implements Packet<ClientGamePacketListener> {
	private Component message;
	private ChatType type;

	public ClientboundChatPacket() {
	}

	public ClientboundChatPacket(Component component) {
		this(component, ChatType.SYSTEM);
	}

	public ClientboundChatPacket(Component component, ChatType chatType) {
		this.message = component;
		this.type = chatType;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.message = friendlyByteBuf.readComponent();
		this.type = ChatType.getForIndex(friendlyByteBuf.readByte());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeComponent(this.message);
		friendlyByteBuf.writeByte(this.type.getIndex());
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleChat(this);
	}

	@Environment(EnvType.CLIENT)
	public Component getMessage() {
		return this.message;
	}

	public boolean isSystem() {
		return this.type == ChatType.SYSTEM || this.type == ChatType.GAME_INFO;
	}

	public ChatType getType() {
		return this.type;
	}

	@Override
	public boolean isSkippable() {
		return true;
	}
}
