package net.minecraft.network.chat;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public record ChatSender(UUID uuid, Component name) {
	public ChatSender(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readUUID(), friendlyByteBuf.readComponent());
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUUID(this.uuid);
		friendlyByteBuf.writeComponent(this.name);
	}
}
