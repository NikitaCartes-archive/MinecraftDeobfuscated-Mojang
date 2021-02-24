package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundAcceptTeleportationPacket implements Packet<ServerGamePacketListener> {
	private final int id;

	@Environment(EnvType.CLIENT)
	public ServerboundAcceptTeleportationPacket(int i) {
		this.id = i;
	}

	public ServerboundAcceptTeleportationPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.id);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleAcceptTeleportPacket(this);
	}

	public int getId() {
		return this.id;
	}
}
