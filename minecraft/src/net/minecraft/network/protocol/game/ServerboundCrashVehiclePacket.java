package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundCrashVehiclePacket(float speed) implements Packet<ServerGamePacketListener> {
	public ServerboundCrashVehiclePacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readFloat());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeFloat(this.speed);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleCrashVehicle(this);
	}
}
