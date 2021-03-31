package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundChatPacket implements Packet<ServerGamePacketListener> {
	private static final int MAX_MESSAGE_LENGTH = 256;
	private final String message;

	public ServerboundChatPacket(String string) {
		if (string.length() > 256) {
			string = string.substring(0, 256);
		}

		this.message = string;
	}

	public ServerboundChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this.message = friendlyByteBuf.readUtf(256);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.message);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleChat(this);
	}

	public String getMessage() {
		return this.message;
	}
}
