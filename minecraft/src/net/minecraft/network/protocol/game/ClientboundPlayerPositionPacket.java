package net.minecraft.network.protocol.game;

import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.RelativeMovement;

public class ClientboundPlayerPositionPacket implements Packet<ClientGamePacketListener> {
	private final double x;
	private final double y;
	private final double z;
	private final float yRot;
	private final float xRot;
	private final Set<RelativeMovement> relativeArguments;
	private final int id;

	public ClientboundPlayerPositionPacket(double d, double e, double f, float g, float h, Set<RelativeMovement> set, int i) {
		this.x = d;
		this.y = e;
		this.z = f;
		this.yRot = g;
		this.xRot = h;
		this.relativeArguments = set;
		this.id = i;
	}

	public ClientboundPlayerPositionPacket(FriendlyByteBuf friendlyByteBuf) {
		this.x = friendlyByteBuf.readDouble();
		this.y = friendlyByteBuf.readDouble();
		this.z = friendlyByteBuf.readDouble();
		this.yRot = friendlyByteBuf.readFloat();
		this.xRot = friendlyByteBuf.readFloat();
		this.relativeArguments = RelativeMovement.unpack(friendlyByteBuf.readUnsignedByte());
		this.id = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeDouble(this.x);
		friendlyByteBuf.writeDouble(this.y);
		friendlyByteBuf.writeDouble(this.z);
		friendlyByteBuf.writeFloat(this.yRot);
		friendlyByteBuf.writeFloat(this.xRot);
		friendlyByteBuf.writeByte(RelativeMovement.pack(this.relativeArguments));
		friendlyByteBuf.writeVarInt(this.id);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleMovePlayer(this);
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

	public int getId() {
		return this.id;
	}

	public Set<RelativeMovement> getRelativeArguments() {
		return this.relativeArguments;
	}
}
