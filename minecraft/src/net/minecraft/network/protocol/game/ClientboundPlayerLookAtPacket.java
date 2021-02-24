package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ClientboundPlayerLookAtPacket implements Packet<ClientGamePacketListener> {
	private final double x;
	private final double y;
	private final double z;
	private final int entity;
	private final EntityAnchorArgument.Anchor fromAnchor;
	private final EntityAnchorArgument.Anchor toAnchor;
	private final boolean atEntity;

	public ClientboundPlayerLookAtPacket(EntityAnchorArgument.Anchor anchor, double d, double e, double f) {
		this.fromAnchor = anchor;
		this.x = d;
		this.y = e;
		this.z = f;
		this.entity = 0;
		this.atEntity = false;
		this.toAnchor = null;
	}

	public ClientboundPlayerLookAtPacket(EntityAnchorArgument.Anchor anchor, Entity entity, EntityAnchorArgument.Anchor anchor2) {
		this.fromAnchor = anchor;
		this.entity = entity.getId();
		this.toAnchor = anchor2;
		Vec3 vec3 = anchor2.apply(entity);
		this.x = vec3.x;
		this.y = vec3.y;
		this.z = vec3.z;
		this.atEntity = true;
	}

	public ClientboundPlayerLookAtPacket(FriendlyByteBuf friendlyByteBuf) {
		this.fromAnchor = friendlyByteBuf.readEnum(EntityAnchorArgument.Anchor.class);
		this.x = friendlyByteBuf.readDouble();
		this.y = friendlyByteBuf.readDouble();
		this.z = friendlyByteBuf.readDouble();
		this.atEntity = friendlyByteBuf.readBoolean();
		if (this.atEntity) {
			this.entity = friendlyByteBuf.readVarInt();
			this.toAnchor = friendlyByteBuf.readEnum(EntityAnchorArgument.Anchor.class);
		} else {
			this.entity = 0;
			this.toAnchor = null;
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeEnum(this.fromAnchor);
		friendlyByteBuf.writeDouble(this.x);
		friendlyByteBuf.writeDouble(this.y);
		friendlyByteBuf.writeDouble(this.z);
		friendlyByteBuf.writeBoolean(this.atEntity);
		if (this.atEntity) {
			friendlyByteBuf.writeVarInt(this.entity);
			friendlyByteBuf.writeEnum(this.toAnchor);
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleLookAt(this);
	}

	@Environment(EnvType.CLIENT)
	public EntityAnchorArgument.Anchor getFromAnchor() {
		return this.fromAnchor;
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public Vec3 getPosition(Level level) {
		if (this.atEntity) {
			Entity entity = level.getEntity(this.entity);
			return entity == null ? new Vec3(this.x, this.y, this.z) : this.toAnchor.apply(entity);
		} else {
			return new Vec3(this.x, this.y, this.z);
		}
	}
}
