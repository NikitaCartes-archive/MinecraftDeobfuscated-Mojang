package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ClientboundAddMobPacket implements Packet<ClientGamePacketListener> {
	private final int id;
	private final UUID uuid;
	private final EntityType<?> type;
	private final double x;
	private final double y;
	private final double z;
	private final int xd;
	private final int yd;
	private final int zd;
	private final byte yRot;
	private final byte xRot;
	private final byte yHeadRot;

	public ClientboundAddMobPacket(LivingEntity livingEntity) {
		this.id = livingEntity.getId();
		this.uuid = livingEntity.getUUID();
		this.type = livingEntity.getType();
		this.x = livingEntity.getX();
		this.y = livingEntity.getY();
		this.z = livingEntity.getZ();
		this.yRot = (byte)((int)(livingEntity.getYRot() * 256.0F / 360.0F));
		this.xRot = (byte)((int)(livingEntity.getXRot() * 256.0F / 360.0F));
		this.yHeadRot = (byte)((int)(livingEntity.yHeadRot * 256.0F / 360.0F));
		double d = 3.9;
		Vec3 vec3 = livingEntity.getDeltaMovement();
		double e = Mth.clamp(vec3.x, -3.9, 3.9);
		double f = Mth.clamp(vec3.y, -3.9, 3.9);
		double g = Mth.clamp(vec3.z, -3.9, 3.9);
		this.xd = (int)(e * 8000.0);
		this.yd = (int)(f * 8000.0);
		this.zd = (int)(g * 8000.0);
	}

	public ClientboundAddMobPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readVarInt();
		this.uuid = friendlyByteBuf.readUUID();
		this.type = friendlyByteBuf.readById(Registry.ENTITY_TYPE);
		this.x = friendlyByteBuf.readDouble();
		this.y = friendlyByteBuf.readDouble();
		this.z = friendlyByteBuf.readDouble();
		this.yRot = friendlyByteBuf.readByte();
		this.xRot = friendlyByteBuf.readByte();
		this.yHeadRot = friendlyByteBuf.readByte();
		this.xd = friendlyByteBuf.readShort();
		this.yd = friendlyByteBuf.readShort();
		this.zd = friendlyByteBuf.readShort();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeUUID(this.uuid);
		friendlyByteBuf.writeId(Registry.ENTITY_TYPE, this.type);
		friendlyByteBuf.writeDouble(this.x);
		friendlyByteBuf.writeDouble(this.y);
		friendlyByteBuf.writeDouble(this.z);
		friendlyByteBuf.writeByte(this.yRot);
		friendlyByteBuf.writeByte(this.xRot);
		friendlyByteBuf.writeByte(this.yHeadRot);
		friendlyByteBuf.writeShort(this.xd);
		friendlyByteBuf.writeShort(this.yd);
		friendlyByteBuf.writeShort(this.zd);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAddMob(this);
	}

	public int getId() {
		return this.id;
	}

	public UUID getUUID() {
		return this.uuid;
	}

	public EntityType<?> getType() {
		return this.type;
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

	public int getXd() {
		return this.xd;
	}

	public int getYd() {
		return this.yd;
	}

	public int getZd() {
		return this.zd;
	}

	public byte getyRot() {
		return this.yRot;
	}

	public byte getxRot() {
		return this.xRot;
	}

	public byte getyHeadRot() {
		return this.yHeadRot;
	}
}
