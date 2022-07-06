package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundCustomChatCompletionsPacket(ClientboundCustomChatCompletionsPacket.Action action, List<String> entries)
	implements Packet<ClientGamePacketListener> {
	public ClientboundCustomChatCompletionsPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readEnum(ClientboundCustomChatCompletionsPacket.Action.class), friendlyByteBuf.readList(FriendlyByteBuf::readUtf));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeEnum(this.action);
		friendlyByteBuf.writeCollection(this.entries, FriendlyByteBuf::writeUtf);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleCustomChatCompletions(this);
	}

	public static enum Action {
		ADD,
		REMOVE,
		SET;
	}
}
