package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundEntityEventPacket implements Packet<ClientGamePacketListener> {
	private final int entityId;
	private final byte eventId;

	public ClientboundEntityEventPacket(Entity entity, byte b) {
		this.entityId = entity.getId();
		this.eventId = b;
	}

	public ClientboundEntityEventPacket(FriendlyByteBuf friendlyByteBuf) {
		this.entityId = friendlyByteBuf.readInt();
		this.eventId = friendlyByteBuf.readByte();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.entityId);
		friendlyByteBuf.writeByte(this.eventId);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleEntityEvent(this);
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public Entity getEntity(Level level) {
		return level.getEntity(this.entityId);
	}

	@Environment(EnvType.CLIENT)
	public byte getEventId() {
		return this.eventId;
	}
}
