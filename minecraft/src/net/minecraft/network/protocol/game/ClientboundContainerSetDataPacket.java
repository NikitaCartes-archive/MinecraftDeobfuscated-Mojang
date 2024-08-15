package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundContainerSetDataPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundContainerSetDataPacket> STREAM_CODEC = Packet.codec(
		ClientboundContainerSetDataPacket::write, ClientboundContainerSetDataPacket::new
	);
	private final int containerId;
	private final int id;
	private final int value;

	public ClientboundContainerSetDataPacket(int i, int j, int k) {
		this.containerId = i;
		this.id = j;
		this.value = k;
	}

	private ClientboundContainerSetDataPacket(FriendlyByteBuf friendlyByteBuf) {
		this.containerId = friendlyByteBuf.readContainerId();
		this.id = friendlyByteBuf.readShort();
		this.value = friendlyByteBuf.readShort();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeContainerId(this.containerId);
		friendlyByteBuf.writeShort(this.id);
		friendlyByteBuf.writeShort(this.value);
	}

	@Override
	public PacketType<ClientboundContainerSetDataPacket> type() {
		return GamePacketTypes.CLIENTBOUND_CONTAINER_SET_DATA;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleContainerSetData(this);
	}

	public int getContainerId() {
		return this.containerId;
	}

	public int getId() {
		return this.id;
	}

	public int getValue() {
		return this.value;
	}
}
