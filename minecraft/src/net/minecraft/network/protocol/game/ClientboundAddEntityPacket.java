package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public class ClientboundAddEntityPacket implements Packet<ClientGamePacketListener> {
	private int id;
	private UUID uuid;
	private double x;
	private double y;
	private double z;
	private int xa;
	private int ya;
	private int za;
	private int xRot;
	private int yRot;
	private EntityType<?> type;
	private int data;

	public ClientboundAddEntityPacket() {
	}

	public ClientboundAddEntityPacket(int i, UUID uUID, double d, double e, double f, float g, float h, EntityType<?> entityType, int j, Vec3 vec3) {
		this.id = i;
		this.uuid = uUID;
		this.x = d;
		this.y = e;
		this.z = f;
		this.xRot = Mth.floor(g * 256.0F / 360.0F);
		this.yRot = Mth.floor(h * 256.0F / 360.0F);
		this.type = entityType;
		this.data = j;
		this.xa = (int)(Mth.clamp(vec3.x, -3.9, 3.9) * 8000.0);
		this.ya = (int)(Mth.clamp(vec3.y, -3.9, 3.9) * 8000.0);
		this.za = (int)(Mth.clamp(vec3.z, -3.9, 3.9) * 8000.0);
	}

	public ClientboundAddEntityPacket(Entity entity) {
		this(entity, 0);
	}

	public ClientboundAddEntityPacket(Entity entity, int i) {
		this(entity.getId(), entity.getUUID(), entity.x, entity.y, entity.z, entity.xRot, entity.yRot, entity.getType(), i, entity.getDeltaMovement());
	}

	public ClientboundAddEntityPacket(Entity entity, EntityType<?> entityType, int i, BlockPos blockPos) {
		this(
			entity.getId(),
			entity.getUUID(),
			(double)blockPos.getX(),
			(double)blockPos.getY(),
			(double)blockPos.getZ(),
			entity.xRot,
			entity.yRot,
			entityType,
			i,
			entity.getDeltaMovement()
		);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.id = friendlyByteBuf.readVarInt();
		this.uuid = friendlyByteBuf.readUUID();
		this.type = Registry.ENTITY_TYPE.byId(friendlyByteBuf.readVarInt());
		this.x = friendlyByteBuf.readDouble();
		this.y = friendlyByteBuf.readDouble();
		this.z = friendlyByteBuf.readDouble();
		this.xRot = friendlyByteBuf.readByte();
		this.yRot = friendlyByteBuf.readByte();
		this.data = friendlyByteBuf.readInt();
		this.xa = friendlyByteBuf.readShort();
		this.ya = friendlyByteBuf.readShort();
		this.za = friendlyByteBuf.readShort();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeUUID(this.uuid);
		friendlyByteBuf.writeVarInt(Registry.ENTITY_TYPE.getId(this.type));
		friendlyByteBuf.writeDouble(this.x);
		friendlyByteBuf.writeDouble(this.y);
		friendlyByteBuf.writeDouble(this.z);
		friendlyByteBuf.writeByte(this.xRot);
		friendlyByteBuf.writeByte(this.yRot);
		friendlyByteBuf.writeInt(this.data);
		friendlyByteBuf.writeShort(this.xa);
		friendlyByteBuf.writeShort(this.ya);
		friendlyByteBuf.writeShort(this.za);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAddEntity(this);
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
	public double getXa() {
		return (double)this.xa / 8000.0;
	}

	@Environment(EnvType.CLIENT)
	public double getYa() {
		return (double)this.ya / 8000.0;
	}

	@Environment(EnvType.CLIENT)
	public double getZa() {
		return (double)this.za / 8000.0;
	}

	@Environment(EnvType.CLIENT)
	public int getxRot() {
		return this.xRot;
	}

	@Environment(EnvType.CLIENT)
	public int getyRot() {
		return this.yRot;
	}

	@Environment(EnvType.CLIENT)
	public EntityType<?> getType() {
		return this.type;
	}

	@Environment(EnvType.CLIENT)
	public int getData() {
		return this.data;
	}
}
