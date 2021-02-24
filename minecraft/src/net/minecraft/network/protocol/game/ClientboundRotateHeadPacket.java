package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundRotateHeadPacket implements Packet<ClientGamePacketListener> {
	private final int entityId;
	private final byte yHeadRot;

	public ClientboundRotateHeadPacket(Entity entity, byte b) {
		this.entityId = entity.getId();
		this.yHeadRot = b;
	}

	public ClientboundRotateHeadPacket(FriendlyByteBuf friendlyByteBuf) {
		this.entityId = friendlyByteBuf.readVarInt();
		this.yHeadRot = friendlyByteBuf.readByte();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.entityId);
		friendlyByteBuf.writeByte(this.yHeadRot);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleRotateMob(this);
	}

	@Environment(EnvType.CLIENT)
	public Entity getEntity(Level level) {
		return level.getEntity(this.entityId);
	}

	@Environment(EnvType.CLIENT)
	public byte getYHeadRot() {
		return this.yHeadRot;
	}
}
