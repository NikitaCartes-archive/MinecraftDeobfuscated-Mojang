package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.phys.Vec3;

public class ServerboundThrowPacket implements Packet<ServerGamePacketListener> {
	private final Vec3 facing;

	public ServerboundThrowPacket(Vec3 vec3) {
		this.facing = vec3;
	}

	public ServerboundThrowPacket(FriendlyByteBuf friendlyByteBuf) {
		this.facing = new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeDouble(this.facing.x());
		friendlyByteBuf.writeDouble(this.facing.y());
		friendlyByteBuf.writeDouble(this.facing.z());
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleThrow(this);
	}

	public Vec3 getFacing() {
		return this.facing;
	}
}
