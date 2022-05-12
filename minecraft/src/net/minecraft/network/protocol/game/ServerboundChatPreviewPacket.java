package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundChatPreviewPacket(int queryId, String query) implements Packet<ServerGamePacketListener> {
	public ServerboundChatPreviewPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readInt(), friendlyByteBuf.readUtf(256));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.queryId);
		friendlyByteBuf.writeUtf(this.query, 256);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleChatPreview(this);
	}
}
