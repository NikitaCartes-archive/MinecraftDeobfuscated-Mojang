package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;

public record ServerboundChatSessionUpdatePacket(RemoteChatSession.Data chatSession) implements Packet<ServerGamePacketListener> {
	public ServerboundChatSessionUpdatePacket(FriendlyByteBuf friendlyByteBuf) {
		this(RemoteChatSession.Data.read(friendlyByteBuf));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		RemoteChatSession.Data.write(friendlyByteBuf, this.chatSession);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleChatSessionUpdate(this);
	}
}
