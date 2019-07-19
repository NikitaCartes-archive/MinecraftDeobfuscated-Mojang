package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundClientCommandPacket implements Packet<ServerGamePacketListener> {
	private ServerboundClientCommandPacket.Action action;

	public ServerboundClientCommandPacket() {
	}

	public ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action action) {
		this.action = action;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.action = friendlyByteBuf.readEnum(ServerboundClientCommandPacket.Action.class);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeEnum(this.action);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleClientCommand(this);
	}

	public ServerboundClientCommandPacket.Action getAction() {
		return this.action;
	}

	public static enum Action {
		PERFORM_RESPAWN,
		REQUEST_STATS;
	}
}
