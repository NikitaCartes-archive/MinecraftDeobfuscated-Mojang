package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundHorseScreenOpenPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundHorseScreenOpenPacket> STREAM_CODEC = Packet.codec(
		ClientboundHorseScreenOpenPacket::write, ClientboundHorseScreenOpenPacket::new
	);
	private final int containerId;
	private final int size;
	private final int entityId;

	public ClientboundHorseScreenOpenPacket(int i, int j, int k) {
		this.containerId = i;
		this.size = j;
		this.entityId = k;
	}

	private ClientboundHorseScreenOpenPacket(FriendlyByteBuf friendlyByteBuf) {
		this.containerId = friendlyByteBuf.readUnsignedByte();
		this.size = friendlyByteBuf.readVarInt();
		this.entityId = friendlyByteBuf.readInt();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.containerId);
		friendlyByteBuf.writeVarInt(this.size);
		friendlyByteBuf.writeInt(this.entityId);
	}

	@Override
	public PacketType<ClientboundHorseScreenOpenPacket> type() {
		return GamePacketTypes.CLIENTBOUND_HORSE_SCREEN_OPEN;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleHorseScreenOpen(this);
	}

	public int getContainerId() {
		return this.containerId;
	}

	public int getSize() {
		return this.size;
	}

	public int getEntityId() {
		return this.entityId;
	}
}
