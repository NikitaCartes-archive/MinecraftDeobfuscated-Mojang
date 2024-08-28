package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public class ClientboundAddEntityPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAddEntityPacket> STREAM_CODEC = Packet.codec(
		ClientboundAddEntityPacket::write, ClientboundAddEntityPacket::new
	);
	private static final double MAGICAL_QUANTIZATION = 8000.0;
	private static final double LIMIT = 3.9;
	private final int id;
	private final UUID uuid;
	private final EntityType<?> type;
	private final double x;
	private final double y;
	private final double z;
	private final int xa;
	private final int ya;
	private final int za;
	private final byte xRot;
	private final byte yRot;
	private final byte yHeadRot;
	private final int data;

	public ClientboundAddEntityPacket(Entity entity, ServerEntity serverEntity) {
		this(entity, serverEntity, 0);
	}

	public ClientboundAddEntityPacket(Entity entity, ServerEntity serverEntity, int i) {
		this(
			entity.getId(),
			entity.getUUID(),
			serverEntity.getPositionBase().x(),
			serverEntity.getPositionBase().y(),
			serverEntity.getPositionBase().z(),
			serverEntity.getLastSentXRot(),
			serverEntity.getLastSentYRot(),
			entity.getType(),
			i,
			serverEntity.getLastSentMovement(),
			(double)serverEntity.getLastSentYHeadRot()
		);
	}

	public ClientboundAddEntityPacket(Entity entity, int i, BlockPos blockPos) {
		this(
			entity.getId(),
			entity.getUUID(),
			(double)blockPos.getX(),
			(double)blockPos.getY(),
			(double)blockPos.getZ(),
			entity.getXRot(),
			entity.getYRot(),
			entity.getType(),
			i,
			entity.getDeltaMovement(),
			(double)entity.getYHeadRot()
		);
	}

	public ClientboundAddEntityPacket(int i, UUID uUID, double d, double e, double f, float g, float h, EntityType<?> entityType, int j, Vec3 vec3, double k) {
		this.id = i;
		this.uuid = uUID;
		this.x = d;
		this.y = e;
		this.z = f;
		this.xRot = Mth.packDegrees(g);
		this.yRot = Mth.packDegrees(h);
		this.yHeadRot = Mth.packDegrees((float)k);
		this.type = entityType;
		this.data = j;
		this.xa = (int)(Mth.clamp(vec3.x, -3.9, 3.9) * 8000.0);
		this.ya = (int)(Mth.clamp(vec3.y, -3.9, 3.9) * 8000.0);
		this.za = (int)(Mth.clamp(vec3.z, -3.9, 3.9) * 8000.0);
	}

	private ClientboundAddEntityPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this.id = registryFriendlyByteBuf.readVarInt();
		this.uuid = registryFriendlyByteBuf.readUUID();
		this.type = ByteBufCodecs.registry(Registries.ENTITY_TYPE).decode(registryFriendlyByteBuf);
		this.x = registryFriendlyByteBuf.readDouble();
		this.y = registryFriendlyByteBuf.readDouble();
		this.z = registryFriendlyByteBuf.readDouble();
		this.xRot = registryFriendlyByteBuf.readByte();
		this.yRot = registryFriendlyByteBuf.readByte();
		this.yHeadRot = registryFriendlyByteBuf.readByte();
		this.data = registryFriendlyByteBuf.readVarInt();
		this.xa = registryFriendlyByteBuf.readShort();
		this.ya = registryFriendlyByteBuf.readShort();
		this.za = registryFriendlyByteBuf.readShort();
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeVarInt(this.id);
		registryFriendlyByteBuf.writeUUID(this.uuid);
		ByteBufCodecs.registry(Registries.ENTITY_TYPE).encode(registryFriendlyByteBuf, this.type);
		registryFriendlyByteBuf.writeDouble(this.x);
		registryFriendlyByteBuf.writeDouble(this.y);
		registryFriendlyByteBuf.writeDouble(this.z);
		registryFriendlyByteBuf.writeByte(this.xRot);
		registryFriendlyByteBuf.writeByte(this.yRot);
		registryFriendlyByteBuf.writeByte(this.yHeadRot);
		registryFriendlyByteBuf.writeVarInt(this.data);
		registryFriendlyByteBuf.writeShort(this.xa);
		registryFriendlyByteBuf.writeShort(this.ya);
		registryFriendlyByteBuf.writeShort(this.za);
	}

	@Override
	public PacketType<ClientboundAddEntityPacket> type() {
		return GamePacketTypes.CLIENTBOUND_ADD_ENTITY;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAddEntity(this);
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

	public double getXa() {
		return (double)this.xa / 8000.0;
	}

	public double getYa() {
		return (double)this.ya / 8000.0;
	}

	public double getZa() {
		return (double)this.za / 8000.0;
	}

	public float getXRot() {
		return Mth.unpackDegrees(this.xRot);
	}

	public float getYRot() {
		return Mth.unpackDegrees(this.yRot);
	}

	public float getYHeadRot() {
		return Mth.unpackDegrees(this.yHeadRot);
	}

	public int getData() {
		return this.data;
	}
}
