package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ClientboundSetEntityMotionPacket implements Packet<ClientGamePacketListener> {
	private final int id;
	private final int xa;
	private final int ya;
	private final int za;

	public ClientboundSetEntityMotionPacket(Entity entity) {
		this(entity.getId(), entity.getDeltaMovement());
	}

	public ClientboundSetEntityMotionPacket(int i, Vec3 vec3) {
		this.id = i;
		double d = 3.9;
		double e = Mth.clamp(vec3.x, -3.9, 3.9);
		double f = Mth.clamp(vec3.y, -3.9, 3.9);
		double g = Mth.clamp(vec3.z, -3.9, 3.9);
		this.xa = (int)(e * 8000.0);
		this.ya = (int)(f * 8000.0);
		this.za = (int)(g * 8000.0);
	}

	public ClientboundSetEntityMotionPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readVarInt();
		this.xa = friendlyByteBuf.readShort();
		this.ya = friendlyByteBuf.readShort();
		this.za = friendlyByteBuf.readShort();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeShort(this.xa);
		friendlyByteBuf.writeShort(this.ya);
		friendlyByteBuf.writeShort(this.za);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetEntityMotion(this);
	}

	@Environment(EnvType.CLIENT)
	public int getId() {
		return this.id;
	}

	@Environment(EnvType.CLIENT)
	public int getXa() {
		return this.xa;
	}

	@Environment(EnvType.CLIENT)
	public int getYa() {
		return this.ya;
	}

	@Environment(EnvType.CLIENT)
	public int getZa() {
		return this.za;
	}
}
