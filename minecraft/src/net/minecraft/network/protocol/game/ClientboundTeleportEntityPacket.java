package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ClientboundTeleportEntityPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundTeleportEntityPacket> STREAM_CODEC = Packet.codec(
		ClientboundTeleportEntityPacket::write, ClientboundTeleportEntityPacket::new
	);
	private final int id;
	private final double x;
	private final double y;
	private final double z;
	private final byte yRot;
	private final byte xRot;
	private final boolean onGround;

	public ClientboundTeleportEntityPacket(Entity entity) {
		this.id = entity.getId();
		Vec3 vec3 = entity.trackingPosition();
		this.x = vec3.x;
		this.y = vec3.y;
		this.z = vec3.z;
		this.yRot = Mth.packDegrees(entity.getYRot());
		this.xRot = Mth.packDegrees(entity.getXRot());
		this.onGround = entity.onGround();
	}

	private ClientboundTeleportEntityPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readVarInt();
		this.x = friendlyByteBuf.readDouble();
		this.y = friendlyByteBuf.readDouble();
		this.z = friendlyByteBuf.readDouble();
		this.yRot = friendlyByteBuf.readByte();
		this.xRot = friendlyByteBuf.readByte();
		this.onGround = friendlyByteBuf.readBoolean();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeDouble(this.x);
		friendlyByteBuf.writeDouble(this.y);
		friendlyByteBuf.writeDouble(this.z);
		friendlyByteBuf.writeByte(this.yRot);
		friendlyByteBuf.writeByte(this.xRot);
		friendlyByteBuf.writeBoolean(this.onGround);
	}

	@Override
	public PacketType<ClientboundTeleportEntityPacket> type() {
		return GamePacketTypes.CLIENTBOUND_TELEPORT_ENTITY;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleTeleportEntity(this);
	}

	public int getId() {
		return this.id;
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

	public float getyRot() {
		return Mth.unpackDegrees(this.yRot);
	}

	public float getxRot() {
		return Mth.unpackDegrees(this.xRot);
	}

	public boolean isOnGround() {
		return this.onGround;
	}
}
