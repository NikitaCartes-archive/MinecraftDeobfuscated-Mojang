package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundEntityTagQueryPacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundEntityTagQueryPacket> STREAM_CODEC = Packet.codec(
		ServerboundEntityTagQueryPacket::write, ServerboundEntityTagQueryPacket::new
	);
	private final int transactionId;
	private final int entityId;

	public ServerboundEntityTagQueryPacket(int i, int j) {
		this.transactionId = i;
		this.entityId = j;
	}

	private ServerboundEntityTagQueryPacket(FriendlyByteBuf friendlyByteBuf) {
		this.transactionId = friendlyByteBuf.readVarInt();
		this.entityId = friendlyByteBuf.readVarInt();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.transactionId);
		friendlyByteBuf.writeVarInt(this.entityId);
	}

	@Override
	public PacketType<ServerboundEntityTagQueryPacket> type() {
		return GamePacketTypes.SERVERBOUND_ENTITY_TAG_QUERY;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleEntityTagQuery(this);
	}

	public int getTransactionId() {
		return this.transactionId;
	}

	public int getEntityId() {
		return this.entityId;
	}
}
