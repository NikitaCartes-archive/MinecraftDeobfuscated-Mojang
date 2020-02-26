package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ClientboundTeleportEntityPacket implements Packet<ClientGamePacketListener> {
	private int id;
	private double x;
	private double y;
	private double z;
	private byte yRot;
	private byte xRot;
	private boolean onGround;

	public ClientboundTeleportEntityPacket() {
	}

	public ClientboundTeleportEntityPacket(Entity entity) {
		this.id = entity.getId();
		this.x = entity.getX();
		this.y = entity.getY();
		this.z = entity.getZ();
		this.yRot = (byte)((int)(entity.yRot * 256.0F / 360.0F));
		this.xRot = (byte)((int)(entity.xRot * 256.0F / 360.0F));
		this.onGround = entity.isOnGround();
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.id = friendlyByteBuf.readVarInt();
		this.x = friendlyByteBuf.readDouble();
		this.y = friendlyByteBuf.readDouble();
		this.z = friendlyByteBuf.readDouble();
		this.yRot = friendlyByteBuf.readByte();
		this.xRot = friendlyByteBuf.readByte();
		this.onGround = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeDouble(this.x);
		friendlyByteBuf.writeDouble(this.y);
		friendlyByteBuf.writeDouble(this.z);
		friendlyByteBuf.writeByte(this.yRot);
		friendlyByteBuf.writeByte(this.xRot);
		friendlyByteBuf.writeBoolean(this.onGround);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleTeleportEntity(this);
	}

	@Environment(EnvType.CLIENT)
	public int getId() {
		return this.id;
	}

	@Environment(EnvType.CLIENT)
	public double getX() {
		return this.x;
	}

	@Environment(EnvType.CLIENT)
	public double getY() {
		return this.y;
	}

	@Environment(EnvType.CLIENT)
	public double getZ() {
		return this.z;
	}

	@Environment(EnvType.CLIENT)
	public byte getyRot() {
		return this.yRot;
	}

	@Environment(EnvType.CLIENT)
	public byte getxRot() {
		return this.xRot;
	}

	@Environment(EnvType.CLIENT)
	public boolean isOnGround() {
		return this.onGround;
	}
}
