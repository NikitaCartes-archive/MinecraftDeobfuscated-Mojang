package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundResourcePackPacket implements Packet<ServerGamePacketListener> {
	private ServerboundResourcePackPacket.Action action;

	public ServerboundResourcePackPacket() {
	}

	public ServerboundResourcePackPacket(ServerboundResourcePackPacket.Action action) {
		this.action = action;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.action = friendlyByteBuf.readEnum(ServerboundResourcePackPacket.Action.class);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeEnum(this.action);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleResourcePackResponse(this);
	}

	public static enum Action {
		SUCCESSFULLY_LOADED,
		DECLINED,
		FAILED_DOWNLOAD,
		ACCEPTED;
	}
}
