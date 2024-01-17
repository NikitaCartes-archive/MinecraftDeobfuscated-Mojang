package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;

public class ClientboundMoveVehiclePacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundMoveVehiclePacket> STREAM_CODEC = Packet.codec(
		ClientboundMoveVehiclePacket::write, ClientboundMoveVehiclePacket::new
	);
	private final double x;
	private final double y;
	private final double z;
	private final float yRot;
	private final float xRot;

	public ClientboundMoveVehiclePacket(Entity entity) {
		this.x = entity.getX();
		this.y = entity.getY();
		this.z = entity.getZ();
		this.yRot = entity.getYRot();
		this.xRot = entity.getXRot();
	}

	private ClientboundMoveVehiclePacket(FriendlyByteBuf friendlyByteBuf) {
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
	public PacketType<ClientboundMoveVehiclePacket> type() {
		return GamePacketTypes.CLIENTBOUND_MOVE_VEHICLE;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleMoveVehicle(this);
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
