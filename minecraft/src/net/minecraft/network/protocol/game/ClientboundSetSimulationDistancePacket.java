package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundSetSimulationDistancePacket() implements Packet<ClientGamePacketListener> {
	private final int simulationDistance;

	public ClientboundSetSimulationDistancePacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readVarInt());
	}

	public ClientboundSetSimulationDistancePacket(int i) {
		this.simulationDistance = i;
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.simulationDistance);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetSimulationDistance(this);
	}
}
