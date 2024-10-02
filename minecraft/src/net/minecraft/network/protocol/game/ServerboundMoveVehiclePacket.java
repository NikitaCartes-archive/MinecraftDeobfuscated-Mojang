package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;

public class ServerboundMoveVehiclePacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundMoveVehiclePacket> STREAM_CODEC = Packet.codec(
		ServerboundMoveVehiclePacket::write, ServerboundMoveVehiclePacket::new
	);
	private final double x;
	private final double y;
	private final double z;
	private final float yRot;
	private final float xRot;

	public ServerboundMoveVehiclePacket(Entity entity) {
		this.x = entity.lerpTargetX();
		this.y = entity.lerpTargetY();
		this.z = entity.lerpTargetZ();
		this.yRot = entity.getYRot();
		this.xRot = entity.getXRot();
	}

	private ServerboundMoveVehiclePacket(FriendlyByteBuf friendlyByteBuf) {
		this.x = friendlyByteBuf.readDouble();
		this.y = friendlyByteBuf.readDouble();
		this.z = friendlyByteBuf.readDouble();
		this.yRot = friendlyByteBuf.readFloat();
		this.xRot = friendlyByteBuf.readFloat();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeDouble(this.x);
		friendlyByteBuf.writeDouble(this.y);
		friendlyByteBuf.writeDouble(this.z);
		friendlyByteBuf.writeFloat(this.yRot);
		friendlyByteBuf.writeFloat(this.xRot);
	}

	@Override
	public PacketType<ServerboundMoveVehiclePacket> type() {
		return GamePacketTypes.SERVERBOUND_MOVE_VEHICLE;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleMoveVehicle(this);
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public double getZ() {
		return this.z;
	}

	public float getYRot() {
		return this.yRot;
	}

	public float getXRot() {
		return this.xRot;
	}
}
