package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundEntityEventPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundEntityEventPacket> STREAM_CODEC = Packet.codec(
		ClientboundEntityEventPacket::write, ClientboundEntityEventPacket::new
	);
	private final int entityId;
	private final byte eventId;

	public ClientboundEntityEventPacket(Entity entity, byte b) {
		this.entityId = entity.getId();
		this.eventId = b;
	}

	private ClientboundEntityEventPacket(FriendlyByteBuf friendlyByteBuf) {
		this.entityId = friendlyByteBuf.readInt();
		this.eventId = friendlyByteBuf.readByte();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.entityId);
		friendlyByteBuf.writeByte(this.eventId);
	}

	@Override
	public PacketType<ClientboundEntityEventPacket> type() {
		return GamePacketTypes.CLIENTBOUND_ENTITY_EVENT;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleEntityEvent(this);
	}

	@Nullable
	public Entity getEntity(Level level) {
		return level.getEntity(this.entityId);
	}

	public byte getEventId() {
		return this.eventId;
	}
}
