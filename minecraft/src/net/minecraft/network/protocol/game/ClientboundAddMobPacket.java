package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ClientboundAddMobPacket implements Packet<ClientGamePacketListener> {
	private int id;
	private UUID uuid;
	private int type;
	private double x;
	private double y;
	private double z;
	private int xd;
	private int yd;
	private int zd;
	private byte yRot;
	private byte xRot;
	private byte yHeadRot;
	private SynchedEntityData entityData;
	private List<SynchedEntityData.DataItem<?>> unpack;

	public ClientboundAddMobPacket() {
	}

	public ClientboundAddMobPacket(LivingEntity livingEntity) {
		this.id = livingEntity.getId();
		this.uuid = livingEntity.getUUID();
		this.type = Registry.ENTITY_TYPE.getId(livingEntity.getType());
		this.x = livingEntity.x;
		this.y = livingEntity.y;
		this.z = livingEntity.z;
		this.yRot = (byte)((int)(livingEntity.yRot * 256.0F / 360.0F));
		this.xRot = (byte)((int)(livingEntity.xRot * 256.0F / 360.0F));
		this.yHeadRot = (byte)((int)(livingEntity.yHeadRot * 256.0F / 360.0F));
		double d = 3.9;
		Vec3 vec3 = livingEntity.getDeltaMovement();
		double e = Mth.clamp(vec3.x, -3.9, 3.9);
		double f = Mth.clamp(vec3.y, -3.9, 3.9);
		double g = Mth.clamp(vec3.z, -3.9, 3.9);
		this.xd = (int)(e * 8000.0);
		this.yd = (int)(f * 8000.0);
		this.zd = (int)(g * 8000.0);
		this.entityData = livingEntity.getEntityData();
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.id = friendlyByteBuf.readVarInt();
		this.uuid = friendlyByteBuf.readUUID();
		this.type = friendlyByteBuf.readVarInt();
		this.x = friendlyByteBuf.readDouble();
		this.y = friendlyByteBuf.readDouble();
		this.z = friendlyByteBuf.readDouble();
		this.yRot = friendlyByteBuf.readByte();
		this.xRot = friendlyByteBuf.readByte();
		this.yHeadRot = friendlyByteBuf.readByte();
		this.xd = friendlyByteBuf.readShort();
		this.yd = friendlyByteBuf.readShort();
		this.zd = friendlyByteBuf.readShort();
		this.unpack = SynchedEntityData.unpack(friendlyByteBuf);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeUUID(this.uuid);
		friendlyByteBuf.writeVarInt(this.type);
		friendlyByteBuf.writeDouble(this.x);
		friendlyByteBuf.writeDouble(this.y);
		friendlyByteBuf.writeDouble(this.z);
		friendlyByteBuf.writeByte(this.yRot);
		friendlyByteBuf.writeByte(this.xRot);
		friendlyByteBuf.writeByte(this.yHeadRot);
		friendlyByteBuf.writeShort(this.xd);
		friendlyByteBuf.writeShort(this.yd);
		friendlyByteBuf.writeShort(this.zd);
		this.entityData.packAll(friendlyByteBuf);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAddMob(this);
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public List<SynchedEntityData.DataItem<?>> getUnpackedData() {
		return this.unpack;
	}

	@Environment(EnvType.CLIENT)
	public int getId() {
		return this.id;
	}

	@Environment(EnvType.CLIENT)
	public UUID getUUID() {
		return this.uuid;
	}

	@Environment(EnvType.CLIENT)
	public int getType() {
		return this.type;
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
	public int getXd() {
		return this.xd;
	}

	@Environment(EnvType.CLIENT)
	public int getYd() {
		return this.yd;
	}

	@Environment(EnvType.CLIENT)
	public int getZd() {
		return this.zd;
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
	public byte getyHeadRot() {
		return this.yHeadRot;
	}
}
