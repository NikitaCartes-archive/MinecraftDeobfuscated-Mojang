package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundRenameItemPacket implements Packet<ServerGamePacketListener> {
	private final String name;

	public ServerboundRenameItemPacket(String string) {
		this.name = string;
	}

	public ServerboundRenameItemPacket(FriendlyByteBuf friendlyByteBuf) {
		this.name = friendlyByteBuf.readUtf();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.name);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleRenameItem(this);
	}

	public String getName() {
		return this.name;
	}
}
