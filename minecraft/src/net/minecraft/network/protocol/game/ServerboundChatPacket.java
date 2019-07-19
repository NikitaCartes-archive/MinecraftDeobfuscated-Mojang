package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundChatPacket implements Packet<ServerGamePacketListener> {
	private String message;

	public ServerboundChatPacket() {
	}

	public ServerboundChatPacket(String string) {
		if (string.length() > 256) {
			string = string.substring(0, 256);
		}

		this.message = string;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.message = friendlyByteBuf.readUtf(256);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeUtf(this.message);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleChat(this);
	}

	public String getMessage() {
		return this.message;
	}
}
