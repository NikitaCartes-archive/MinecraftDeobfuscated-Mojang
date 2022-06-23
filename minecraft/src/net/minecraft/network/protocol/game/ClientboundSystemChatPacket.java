package net.minecraft.network.protocol.game;

import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public record ClientboundSystemChatPacket(Component content, int typeId) implements Packet<ClientGamePacketListener> {
	public ClientboundSystemChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readComponent(), friendlyByteBuf.readVarInt());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.content);
		friendlyByteBuf.writeVarInt(this.typeId);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSystemChat(this);
	}

	@Override
	public boolean isSkippable() {
		return true;
	}

	public ChatType resolveType(Registry<ChatType> registry) {
		return (ChatType)Objects.requireNonNull(registry.byId(this.typeId), "Invalid chat type");
	}
}
