package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundClientCommandPacket implements Packet<ServerGamePacketListener> {
	private final ServerboundClientCommandPacket.Action action;

	public ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action action) {
		this.action = action;
	}

	public ServerboundClientCommandPacket(FriendlyByteBuf friendlyByteBuf) {
		this.action = friendlyByteBuf.readEnum(ServerboundClientCommandPacket.Action.class);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
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
