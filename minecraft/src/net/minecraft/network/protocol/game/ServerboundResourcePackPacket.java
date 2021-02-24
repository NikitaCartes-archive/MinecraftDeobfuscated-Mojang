package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundResourcePackPacket implements Packet<ServerGamePacketListener> {
	private final ServerboundResourcePackPacket.Action action;

	public ServerboundResourcePackPacket(ServerboundResourcePackPacket.Action action) {
		this.action = action;
	}

	public ServerboundResourcePackPacket(FriendlyByteBuf friendlyByteBuf) {
		this.action = friendlyByteBuf.readEnum(ServerboundResourcePackPacket.Action.class);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeEnum(this.action);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleResourcePackResponse(this);
	}

	public ServerboundResourcePackPacket.Action getAction() {
		return this.action;
	}

	public static enum Action {
		SUCCESSFULLY_LOADED,
		DECLINED,
		FAILED_DOWNLOAD,
		ACCEPTED;
	}
}
