package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ClientboundMoveVehiclePacket implements Packet<ClientGamePacketListener> {
	private final double x;
	private final double y;
	private final double z;
	private final float yRot;
	private final float xRot;

	public ClientboundMoveVehiclePacket(Entity entity) {
		this.x = entity.getX();
		this.y = entity.getY();
		this.z = entity.getZ();
		this.yRot = entity.yRot;
		this.xRot = entity.xRot;
	}

	public ClientboundMoveVehiclePacket(FriendlyByteBuf friendlyByteBuf) {
		this.x = friendlyByteBuf.readDouble();
		this.y = friendlyByteBuf.readDouble();
		this.z = friendlyByteBuf.readDouble();
		this.yRot = friendlyByteBuf.readFloat();
		this.xRot = friendlyByteBuf.readFloat();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeDouble(this.x);
		friendlyByteBuf.writeDouble(this.y);
		friendlyByteBuf.writeDouble(this.z);
		friendlyByteBuf.writeFloat(this.yRot);
		friendlyByteBuf.writeFloat(this.xRot);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleMoveVehicle(this);
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
	public float getYRot() {
		return this.yRot;
	}

	@Environment(EnvType.CLIENT)
	public float getXRot() {
		return this.xRot;
	}
}
