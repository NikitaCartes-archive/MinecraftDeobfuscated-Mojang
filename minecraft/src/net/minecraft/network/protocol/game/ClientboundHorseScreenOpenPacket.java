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
	private final int inventoryColumns;
	private final int entityId;

	public ClientboundHorseScreenOpenPacket(int i, int j, int k) {
		this.containerId = i;
		this.inventoryColumns = j;
		this.entityId = k;
	}

	private ClientboundHorseScreenOpenPacket(FriendlyByteBuf friendlyByteBuf) {
		this.containerId = friendlyByteBuf.readContainerId();
		this.inventoryColumns = friendlyByteBuf.readVarInt();
		this.entityId = friendlyByteBuf.readInt();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeContainerId(this.containerId);
		friendlyByteBuf.writeVarInt(this.inventoryColumns);
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

	public int getInventoryColumns() {
		return this.inventoryColumns;
	}

	public int getEntityId() {
		return this.entityId;
	}
}
